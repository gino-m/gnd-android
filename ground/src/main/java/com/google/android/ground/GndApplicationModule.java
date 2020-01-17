/*
 * Copyright 2018 Google LLC
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

package com.google.android.ground;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.room.Room;
import androidx.work.WorkManager;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.ground.inject.ActivityScoped;
import com.google.android.ground.persistence.local.LocalDataStore;
import com.google.android.ground.persistence.local.room.FeatureDao;
import com.google.android.ground.persistence.local.room.FeatureMutationDao;
import com.google.android.ground.persistence.local.room.FieldDao;
import com.google.android.ground.persistence.local.room.FormDao;
import com.google.android.ground.persistence.local.room.LayerDao;
import com.google.android.ground.persistence.local.room.LocalDatabase;
import com.google.android.ground.persistence.local.room.MultipleChoiceDao;
import com.google.android.ground.persistence.local.room.ObservationDao;
import com.google.android.ground.persistence.local.room.ObservationMutationDao;
import com.google.android.ground.persistence.local.room.OfflineAreaDao;
import com.google.android.ground.persistence.local.room.OptionDao;
import com.google.android.ground.persistence.local.room.ProjectDao;
import com.google.android.ground.persistence.local.room.RoomLocalDataStore;
import com.google.android.ground.persistence.local.room.TileDao;
import com.google.android.ground.persistence.local.room.UserDao;
import com.google.android.ground.persistence.remote.RemoteDataStore;
import com.google.android.ground.persistence.remote.firestore.FirestoreDataStore;
import com.google.android.ground.persistence.remote.firestore.FirestoreUuidGenerator;
import com.google.android.ground.persistence.uuid.OfflineUuidGenerator;
import com.google.android.ground.ui.common.ViewModelModule;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import javax.inject.Singleton;

@Module(includes = {AndroidSupportInjectionModule.class, ViewModelModule.class})
abstract class GndApplicationModule {
  private static final String SHARED_PREFERENCES_NAME = "shared_prefs";

  /** Causes Dagger Android to generate a sub-component for the MainActivity. */
  @ActivityScoped
  @ContributesAndroidInjector(modules = MainActivityModule.class)
  abstract MainActivity mainActivityInjector();

  /** Provides the Room implementation of local data store. */
  @Binds
  @Singleton
  abstract LocalDataStore localDataStore(RoomLocalDataStore ds);

  /** Provides the Firestore implementation of remote data store. */
  @Binds
  @Singleton
  abstract RemoteDataStore remoteDataStore(FirestoreDataStore ds);

  /** Provides the Firestore implementation of offline unique id generation. */
  @Binds
  @Singleton
  abstract OfflineUuidGenerator offlineUuidGenerator(FirestoreUuidGenerator uuidGenerator);

  @Binds
  @Singleton
  abstract Application application(GndApplication app);

  @Binds
  abstract Context context(GndApplication application);

  @Provides
  @Singleton
  static GoogleApiAvailability googleApiAvailability() {
    return GoogleApiAvailability.getInstance();
  }

  @Provides
  @Singleton
  static WorkManager workManager() {
    return WorkManager.getInstance();
  }

  @Provides
  @Singleton
  static SharedPreferences sharedPreferences(GndApplication application) {
    return application
        .getApplicationContext()
        .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  @Provides
  static FirebaseFirestoreSettings firebaseFirestoreSettings() {
    return new FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(Config.FIRESTORE_PERSISTENCE_ENABLED)
        .build();
  }

  @Provides
  @Singleton
  static FirebaseFirestore firebaseFirestore(FirebaseFirestoreSettings settings) {
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    firestore.setFirestoreSettings(settings);
    FirebaseFirestore.setLoggingEnabled(Config.FIRESTORE_LOGGING_ENABLED);
    return firestore;
  }

  @Provides
  @Singleton
  static LocalDatabase localDatabase(Context context) {
    return Room.databaseBuilder(context, LocalDatabase.class, Config.DB_NAME)
        // TODO(#128): Disable before official release.
        .fallbackToDestructiveMigration()
        .build();
  }

  @Provides
  static FeatureDao featureDao(LocalDatabase localDatabase) {
    return localDatabase.featureDao();
  }

  @Provides
  static FeatureMutationDao featureMutationDao(LocalDatabase localDatabase) {
    return localDatabase.featureMutationDao();
  }

  @Provides
  static FieldDao fieldDao(LocalDatabase localDatabase) {
    return localDatabase.fieldDao();
  }

  @Provides
  static FormDao formDao(LocalDatabase localDatabase) {
    return localDatabase.formDao();
  }

  @Provides
  static LayerDao layerDao(LocalDatabase localDatabase) {
    return localDatabase.layerDao();
  }

  @Provides
  static MultipleChoiceDao multipleChoiceDao(LocalDatabase localDatabase) {
    return localDatabase.multipleChoiceDao();
  }

  @Provides
  static OptionDao optionDao(LocalDatabase localDatabase) {
    return localDatabase.optionDao();
  }

  @Provides
  static ProjectDao projectDao(LocalDatabase localDatabase) {
    return localDatabase.projectDao();
  }

  @Provides
  static ObservationDao observationDao(LocalDatabase localDatabase) {
    return localDatabase.observationDao();
  }

  @Provides
  static ObservationMutationDao observationMutationDao(LocalDatabase localDatabase) {
    return localDatabase.observationMutationDao();
  }

  @Provides
  static TileDao tileDao(LocalDatabase localDatabase) {
    return localDatabase.tileDao();
  }

  @Provides
  static OfflineAreaDao offlineAreaDao(LocalDatabase localDatabase) {
    return localDatabase.offlineAreaDao();
  }

  @Provides
  static UserDao userDao(LocalDatabase localDatabase) {
    return localDatabase.userDao();
  }
}
