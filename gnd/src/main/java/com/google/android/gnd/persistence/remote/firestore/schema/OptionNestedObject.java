/*
 * Copyright 2020 Google LLC
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

package com.google.android.gnd.persistence.remote.firestore.schema;

import androidx.annotation.Nullable;
import java.util.Map;

/** Firestore representation of multiple choice question options. */
class OptionNestedObject {
  @Nullable private String code;
  @Nullable private Map<String, String> labels;

  OptionNestedObject(@Nullable String code, @Nullable Map<String, String> labels) {
    this.code = code;
    this.labels = labels;
  }

  @Nullable
  String getCode() {
    return code;
  }

  @Nullable
  Map<String, String> getLabels() {
    return labels;
  }
}