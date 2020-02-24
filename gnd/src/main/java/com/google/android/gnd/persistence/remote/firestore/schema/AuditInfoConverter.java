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

import static com.google.android.gnd.persistence.remote.DataStoreException.checkNotNull;

import androidx.annotation.NonNull;
import com.google.android.gnd.model.AuditInfo;
import com.google.android.gnd.model.Mutation;
import com.google.android.gnd.model.User;
import com.google.android.gnd.persistence.remote.DataStoreException;
import com.google.firebase.Timestamp;
import java8.util.Optional;

/** Converts between Firestore nested objects and {@link AuditInfo} instances. */
class AuditInfoConverter {

  @NonNull
  static AuditInfo toAuditInfo(@NonNull AuditInfoNestedObject doc) throws DataStoreException {
    checkNotNull(doc.getClientTimeMillis(), "clientTimeMillis");
    return AuditInfo.builder()
        .setUser(UserConverter.toUser(doc.getUser()))
        .setClientTimeMillis(doc.getClientTimeMillis().toDate())
        .setServerTimeMillis(Optional.ofNullable(doc.getServerTimeMillis()).map(Timestamp::toDate))
        .build();
  }

  @NonNull
  static AuditInfoNestedObject fromMutationAndUser(Mutation mutation, User user) {
    return new AuditInfoNestedObject(
        UserConverter.toNestedObject(user), new Timestamp(mutation.getClientTimestamp()), null);
  }
}
