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

package com.google.android.gnd.repository.local;

import static java8.util.J8Arrays.stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

/** Mutually exclusive entity states shared by Features and Records. */
public enum EntityState implements IntEnum {
  DELETED(-1),
  UNKNOWN(0),
  DEFAULT(1);

  private final int intValue;

  EntityState(int intValue) {
    this.intValue = intValue;
  }

  public int intValue() {
    return intValue;
  }

  @TypeConverter
  public static int fromEntityState(@Nullable EntityState value) {
    return IntEnum.fromIntEnum(value, UNKNOWN);
  }

  @NonNull
  @TypeConverter
  public static EntityState toEntityState(int intValue) {
    return IntEnum.toIntEnum(stream(values()), intValue, UNKNOWN);
  }
}
