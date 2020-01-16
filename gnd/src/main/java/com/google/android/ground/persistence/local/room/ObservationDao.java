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

import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;

@Dao
public interface ObservationDao extends BaseDao<ObservationEntity> {
  /** Returns the observation with the specified UUID, if found. */
  @Query("SELECT * FROM observation WHERE id = :observationId")
  Maybe<ObservationEntity> findById(String observationId);

  /**
   * Returns the list observations associated with the specified feature and form, ignoring deleted
   * observations (i.e., returns only observations with state = State.DEFAULT (1)).
   */
  @Query(
      "SELECT * FROM observation WHERE feature_id = :featureId AND form_id = :formId AND state = 1")
  Single<List<ObservationEntity>> findByFeatureId(String featureId, String formId);
}
