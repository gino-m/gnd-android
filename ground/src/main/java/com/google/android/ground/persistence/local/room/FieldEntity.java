/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.ground.persistence.local.room;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.android.ground.model.form.Element;
import com.google.android.ground.model.form.Element.Type;
import com.google.android.ground.model.form.Field;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import java.util.List;

@AutoValue
@Entity(
    tableName = "field",
    foreignKeys =
        @ForeignKey(
            entity = FormEntity.class,
            parentColumns = "id",
            childColumns = "form_id",
            onDelete = ForeignKey.CASCADE),
    indices = {@Index("form_id")})
public abstract class FieldEntity {

  private static final String TAG = FieldEntity.class.getName();

  @CopyAnnotations
  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  public abstract String getId();

  @CopyAnnotations
  @NonNull
  @ColumnInfo(name = "element_type")
  public abstract ElementEntityType getElementType();

  @CopyAnnotations
  @NonNull
  @ColumnInfo(name = "field_type")
  public abstract FieldEntityType getFieldType();

  @CopyAnnotations
  @Nullable
  @ColumnInfo(name = "label")
  public abstract String getLabel();

  @CopyAnnotations
  @ColumnInfo(name = "is_required")
  public abstract boolean isRequired();

  @CopyAnnotations
  @Nullable
  @ColumnInfo(name = "form_id")
  public abstract String getFormId();

  public static FieldEntity fromField(String formId, Type elementType, Field field) {
    return FieldEntity.builder()
        .setId(field.getId())
        .setLabel(field.getLabel())
        .setRequired(field.isRequired())
        .setElementType(ElementEntityType.fromElementType(elementType))
        .setFieldType(FieldEntityType.fromFieldType(field.getType()))
        .setFormId(formId)
        .build();
  }

  public static Element toElement(FieldEntityAndRelations fieldEntityAndRelations) {
    return fieldEntityAndRelations.fieldEntity.getElementType().toElementType() == Type.FIELD
        ? Element.ofField(FieldEntity.toField(fieldEntityAndRelations))
        : Element.ofUnknown();
  }

  public static Field toField(FieldEntityAndRelations fieldEntityAndRelations) {
    FieldEntity fieldEntity = fieldEntityAndRelations.fieldEntity;
    Field.Builder fieldBuilder =
        Field.newBuilder()
            .setId(fieldEntity.getId())
            .setLabel(fieldEntity.getLabel())
            .setRequired(fieldEntity.isRequired())
            .setType(fieldEntity.getFieldType().toFieldType());

    List<MultipleChoiceEntity> multipleChoiceEntities =
        fieldEntityAndRelations.multipleChoiceEntities;

    if (!multipleChoiceEntities.isEmpty()) {
      if (multipleChoiceEntities.size() > 1) {
        Log.e(TAG, "More than 1 multiple choice found for field");
      }

      fieldBuilder.setMultipleChoice(
          MultipleChoiceEntity.toMultipleChoice(
              multipleChoiceEntities.get(0), fieldEntityAndRelations.optionEntities));
    }

    return fieldBuilder.build();
  }

  public static FieldEntity create(
      String id,
      ElementEntityType elementType,
      FieldEntityType fieldType,
      String label,
      boolean required,
      String formId) {
    return builder()
        .setId(id)
        .setElementType(elementType)
        .setFieldType(fieldType)
        .setLabel(label)
        .setRequired(required)
        .setFormId(formId)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_FieldEntity.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setElementType(ElementEntityType elementType);

    public abstract Builder setFieldType(FieldEntityType fieldType);

    public abstract Builder setLabel(String label);

    public abstract Builder setRequired(boolean required);

    public abstract Builder setFormId(String formId);

    public abstract FieldEntity build();
  }
}
