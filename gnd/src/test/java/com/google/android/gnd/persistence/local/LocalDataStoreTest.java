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

package com.google.android.gnd.persistence.local;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gnd.TestApplication;
import com.google.android.gnd.inject.DaggerTestComponent;
import com.google.android.gnd.model.Mutation;
import com.google.android.gnd.model.Project;
import com.google.android.gnd.model.User;
import com.google.android.gnd.model.basemap.OfflineArea;
import com.google.android.gnd.model.basemap.tile.Tile;
import com.google.android.gnd.model.basemap.tile.Tile.State;
import com.google.android.gnd.model.feature.Feature;
import com.google.android.gnd.model.feature.FeatureMutation;
import com.google.android.gnd.model.feature.Point;
import com.google.android.gnd.model.form.Element;
import com.google.android.gnd.model.form.Field;
import com.google.android.gnd.model.form.Field.Type;
import com.google.android.gnd.model.form.Form;
import com.google.android.gnd.model.form.MultipleChoice;
import com.google.android.gnd.model.form.MultipleChoice.Cardinality;
import com.google.android.gnd.model.form.Option;
import com.google.android.gnd.model.layer.Layer;
import com.google.android.gnd.model.layer.Style;
import com.google.android.gnd.model.observation.Observation;
import com.google.android.gnd.model.observation.ObservationMutation;
import com.google.android.gnd.model.observation.ResponseDelta;
import com.google.android.gnd.model.observation.ResponseMap;
import com.google.android.gnd.model.observation.TextResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.subscribers.TestSubscriber;
import java.util.AbstractCollection;
import java.util.Date;
import java8.util.Optional;
import javax.inject.Inject;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class LocalDataStoreTest {

  private static final User TEST_USER =
      User.builder().setId("user id").setEmail("user@gmail.com").setDisplayName("user 1").build();

  private static final MultipleChoice TEST_MULTIPLE_CHOICE =
      MultipleChoice.newBuilder()
          .setCardinality(Cardinality.SELECT_ONE)
          .setOptions(
              ImmutableList.of(
                  Option.newBuilder().setCode("a").setLabel("Name").build(),
                  Option.newBuilder().setCode("b").setLabel("Age").build()))
          .build();

  private static final Field TEST_FIELD =
      Field.newBuilder()
          .setId("field id")
          .setLabel("field label")
          .setRequired(false)
          .setType(Type.MULTIPLE_CHOICE)
          .setMultipleChoice(TEST_MULTIPLE_CHOICE)
          .build();

  private static final Form TEST_FORM =
      Form.newBuilder()
          .setId("form id")
          .setElements(ImmutableList.of(Element.ofField(TEST_FIELD)))
          .build();

  private static final Layer TEST_LAYER =
      Layer.newBuilder()
          .setId("layer id")
          .setItemLabel("item label")
          .setListHeading("heading title")
          .setDefaultStyle(Style.builder().setColor("000").build())
          .setForm(TEST_FORM)
          .build();

  private static final Project TEST_PROJECT =
      Project.newBuilder()
          .setId("project id")
          .setTitle("project 1")
          .setDescription("foo description")
          .putLayer("layer id", TEST_LAYER)
          .build();

  private static final Point TEST_POINT =
      Point.newBuilder().setLatitude(110.0).setLongitude(-23.1).build();

  private static final Point TEST_POINT_2 =
      Point.newBuilder().setLatitude(51.0).setLongitude(44.0).build();

  private static final FeatureMutation TEST_FEATURE_MUTATION =
      createTestFeatureMutation(TEST_POINT);

  private static final ObservationMutation TEST_OBSERVATION_MUTATION =
      ObservationMutation.builder()
          .setId(1L)
          .setObservationId("observation id")
          .setType(Mutation.Type.CREATE)
          .setProjectId("project id")
          .setFeatureId("feature id")
          .setLayerId("layer id")
          .setFormId("form id")
          .setUserId("user id")
          .setResponseDeltas(
              ImmutableList.of(
                  ResponseDelta.builder()
                      .setFieldId("field id")
                      .setNewResponse(TextResponse.fromString("response for field id"))
                      .build()))
          .setClientTimestamp(new Date())
          .build();

  private static final Tile TEST_PENDING_TILE =
      Tile.newBuilder()
          .setId("id_1")
          .setState(State.PENDING)
          .setPath("some_path 1")
          .setUrl("some_url 1")
          .build();

  private static final Tile TEST_DOWNLOADED_TILE =
      Tile.newBuilder()
          .setId("id_2")
          .setState(State.DOWNLOADED)
          .setPath("some_path 2")
          .setUrl("some_url 2")
          .build();

  private static final Tile TEST_FAILED_TILE =
      Tile.newBuilder()
          .setId("id_3")
          .setState(State.FAILED)
          .setPath("some_path 3")
          .setUrl("some_url 3")
          .build();

  private static final OfflineArea TEST_OFFLINE_AREA =
      OfflineArea.newBuilder()
          .setId("id_1")
          .setBounds(LatLngBounds.builder().include(new LatLng(0.0, 0.0)).build())
          .setState(OfflineArea.State.PENDING)
          .build();

  // This rule makes sure that Room executes all the database operations instantly.
  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  @Inject LocalDataStore localDataStore;

  private static FeatureMutation createTestFeatureMutation(Point point) {
    return FeatureMutation.builder()
        .setId(1L)
        .setFeatureId("feature id")
        .setType(Mutation.Type.CREATE)
        .setUserId("user id")
        .setProjectId("project id")
        .setLayerId("layer id")
        .setNewLocation(Optional.ofNullable(point))
        .setClientTimestamp(new Date())
        .build();
  }

  private static void assertEquivalent(ObservationMutation mutation, Observation observation) {
    assertThat(mutation.getObservationId()).isEqualTo(observation.getId());
    assertThat(mutation.getFeatureId()).isEqualTo(observation.getFeature().getId());
    assertThat(mutation.getFormId()).isEqualTo(observation.getForm().getId());
    assertThat(mutation.getProjectId()).isEqualTo(observation.getProject().getId());
    assertThat(mutation.getUserId()).isEqualTo(observation.getLastModified().getUser().getId());
    assertThat(mutation.getUserId()).isEqualTo(observation.getCreated().getUser().getId());
    MatcherAssert.assertThat(
        ResponseMap.builder().applyDeltas(mutation.getResponseDeltas()).build(),
        samePropertyValuesAs(observation.getResponses()));
  }

  @Before
  public void setUp() {
    DaggerTestComponent.create().inject(this);
  }

  @Test
  public void testInsertAndGetProjects() {
    localDataStore.insertOrUpdateProject(TEST_PROJECT).test().assertComplete();
    localDataStore.getProjects().test().assertValue(ImmutableList.of(TEST_PROJECT));
  }

  @Test
  public void testGetProjectById() {
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.getProjectById("project id").test().assertValue(TEST_PROJECT);
  }

  @Test
  public void testDeleteProject() {
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.deleteProject(TEST_PROJECT).test().assertComplete();
    localDataStore.getProjects().test().assertValue(AbstractCollection::isEmpty);
  }

  @Test
  public void testInsertAndGetUser() {
    localDataStore.insertOrUpdateUser(TEST_USER).test().assertComplete();
    localDataStore.getUser("user id").test().assertValue(TEST_USER);
  }

  @Test
  public void testApplyAndEnqueue_featureMutation() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();

    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).test().assertComplete();

    // assert that mutation is saved to local database
    localDataStore
        .getPendingMutations("feature id")
        .test()
        .assertValue(ImmutableList.of(TEST_FEATURE_MUTATION));

    localDataStore
        .getFeature(TEST_PROJECT, "feature id")
        .test()
        .assertValue(feature -> feature.getPoint().equals(TEST_POINT));
  }

  @Test
  public void testGetFeaturesOnceAndStream() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();

    TestSubscriber<ImmutableSet<Feature>> subscriber =
        localDataStore.getFeaturesOnceAndStream(TEST_PROJECT).test();

    subscriber.assertValue(ImmutableSet.of());

    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).blockingAwait();

    Feature feature = localDataStore.getFeature(TEST_PROJECT, "feature id").blockingGet();

    subscriber.assertValueSet(ImmutableSet.of(ImmutableSet.of(), ImmutableSet.of(feature)));
  }

  @Test
  public void testUpdateMutations() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).blockingAwait();

    FeatureMutation mutation = createTestFeatureMutation(TEST_POINT_2);
    localDataStore.updateMutations(ImmutableList.of(mutation)).test().assertComplete();
    localDataStore
        .getPendingMutations(TEST_FEATURE_MUTATION.getFeatureId())
        .test()
        .assertValue(ImmutableList.of(mutation));
  }

  @Test
  public void testRemovePendingMutation() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).blockingAwait();

    localDataStore
        .removePendingMutations(ImmutableList.of(TEST_FEATURE_MUTATION))
        .test()
        .assertComplete();

    localDataStore
        .getPendingMutations("feature id")
        .test()
        .assertValue(AbstractCollection::isEmpty);
  }

  @Test
  public void testMergeFeature() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).blockingAwait();

    Feature feature = localDataStore.getFeature(TEST_PROJECT, "feature id").blockingGet();
    feature = feature.toBuilder().setPoint(TEST_POINT_2).build();
    localDataStore.mergeFeature(feature).test().assertComplete();

    localDataStore
        .getFeature(TEST_PROJECT, "feature id")
        .test()
        .assertValue(newFeature -> newFeature.getPoint().equals(TEST_POINT_2));
  }

  @Test
  public void testApplyAndEnqueue_observationMutation() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).blockingAwait();

    localDataStore.applyAndEnqueue(TEST_OBSERVATION_MUTATION).test().assertComplete();

    localDataStore
        .getPendingMutations("feature id")
        .test()
        .assertValue(ImmutableList.of(TEST_FEATURE_MUTATION, TEST_OBSERVATION_MUTATION));

    Feature feature = localDataStore.getFeature(TEST_PROJECT, "feature id").blockingGet();
    Observation observation =
        localDataStore.getObservation(feature, "observation id").blockingGet();
    assertEquivalent(TEST_OBSERVATION_MUTATION, observation);

    // now update the inserted observation with new responses
    ImmutableList<ResponseDelta> deltas =
        ImmutableList.of(
            ResponseDelta.builder()
                .setFieldId("really new field")
                .setNewResponse(TextResponse.fromString("value for the really new field"))
                .build());

    ObservationMutation mutation =
        TEST_OBSERVATION_MUTATION
            .toBuilder()
            .setId(2L)
            .setResponseDeltas(deltas)
            .setType(Mutation.Type.UPDATE)
            .build();
    localDataStore.applyAndEnqueue(mutation).test().assertComplete();

    localDataStore
        .getPendingMutations("feature id")
        .test()
        .assertValue(ImmutableList.of(TEST_FEATURE_MUTATION, TEST_OBSERVATION_MUTATION, mutation));

    // check if the observation was updated in the local database
    observation = localDataStore.getObservation(feature, "observation id").blockingGet();
    assertEquivalent(mutation, observation);

    // also test that getObservations returns the same observation as well
    ImmutableList<Observation> observations =
        localDataStore.getObservations(feature, "form id").blockingGet();
    assertThat(observations).hasSize(1);
    assertEquivalent(mutation, observations.get(0));
  }

  @Test
  public void testMergeObservation() {
    localDataStore.insertOrUpdateUser(TEST_USER).blockingAwait();
    localDataStore.insertOrUpdateProject(TEST_PROJECT).blockingAwait();
    localDataStore.applyAndEnqueue(TEST_FEATURE_MUTATION).blockingAwait();
    localDataStore.applyAndEnqueue(TEST_OBSERVATION_MUTATION).blockingAwait();
    Feature feature = localDataStore.getFeature(TEST_PROJECT, "feature id").blockingGet();

    ResponseMap responseMap =
        ResponseMap.builder()
            .putResponse("foo field", TextResponse.fromString("foo value").get())
            .build();

    Observation observation =
        localDataStore
            .getObservation(feature, "observation id")
            .blockingGet()
            .toBuilder()
            .setResponses(responseMap)
            .build();

    localDataStore.mergeObservation(observation).test().assertComplete();

    ResponseMap responses =
        localDataStore
            .getObservation(feature, observation.getId())
            .test()
            .values()
            .get(0)
            .getResponses();
    assertThat("foo value").isEqualTo(responses.getResponse("foo field").get().toString());
  }

  @Test
  public void testInsertTile() {
    localDataStore.insertOrUpdateTile(TEST_PENDING_TILE).test().assertComplete();
  }

  @Test
  public void testGetTile() {
    localDataStore.insertOrUpdateTile(TEST_PENDING_TILE).blockingAwait();
    localDataStore.getTile("id_1").test().assertValueCount(1).assertValue(TEST_PENDING_TILE);
  }

  @Test
  public void testGetTilesOnceAndStream() {
    TestSubscriber<ImmutableSet<Tile>> subscriber = localDataStore.getTilesOnceAndStream().test();

    subscriber.assertValue(ImmutableSet.of());

    localDataStore.insertOrUpdateTile(TEST_DOWNLOADED_TILE).blockingAwait();
    localDataStore.insertOrUpdateTile(TEST_PENDING_TILE).blockingAwait();

    subscriber.assertValueSet(
        ImmutableSet.of(
            ImmutableSet.of(),
            ImmutableSet.of(TEST_DOWNLOADED_TILE),
            ImmutableSet.of(TEST_DOWNLOADED_TILE, TEST_PENDING_TILE)));
  }

  @Test
  public void testGetPendingTile() {
    localDataStore.insertOrUpdateTile(TEST_DOWNLOADED_TILE).blockingAwait();
    localDataStore.insertOrUpdateTile(TEST_FAILED_TILE).blockingAwait();
    localDataStore.insertOrUpdateTile(TEST_PENDING_TILE).blockingAwait();
    localDataStore.getPendingTiles().test().assertValue(ImmutableList.of(TEST_PENDING_TILE));
  }

  @Test
  public void testInsertOfflineArea() {
    localDataStore.insertOrUpdateOfflineArea(TEST_OFFLINE_AREA).test().assertComplete();
  }

  @Test
  public void testGetOfflineAreas() {
    localDataStore.insertOrUpdateOfflineArea(TEST_OFFLINE_AREA).blockingAwait();
    localDataStore
        .getOfflineAreasOnceAndStream()
        .test()
        .assertValue(ImmutableList.of(TEST_OFFLINE_AREA));
  }
}
