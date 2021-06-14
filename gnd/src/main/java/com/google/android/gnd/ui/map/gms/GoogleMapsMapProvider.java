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

package com.google.android.gnd.ui.map.gms;

import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gnd.rx.annotations.Hot;
import com.google.android.gnd.ui.MarkerIconFactory;
import com.google.android.gnd.ui.map.MapAdapter;
import com.google.android.gnd.ui.map.MapProvider;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

/** Ground map adapter implementation for Google Maps API. */
public class GoogleMapsMapProvider implements MapProvider {

  private final MarkerIconFactory markerIconFactory;
  @Hot private final SingleSubject<MapAdapter> map = SingleSubject.create();

  @SuppressWarnings("NullAway.Init")
  private GoogleMapsFragment fragment;

  @Inject
  public GoogleMapsMapProvider(MarkerIconFactory markerIconFactory) {
    this.markerIconFactory = markerIconFactory;
  }

  @Override
  public void restore(Fragment fragment) {
    init((GoogleMapsFragment) fragment);
  }

  private void init(GoogleMapsFragment fragment) {
    if (this.fragment != null) {
      throw new IllegalStateException("Map fragment already initialized");
    }
    this.fragment = fragment;
    createMapAsync();
  }

  private void createMapAsync() {
    ((GoogleMapsFragment) getFragment())
        .getMapAsync(
            googleMap ->
                map.onSuccess(
                    new GoogleMapsMapAdapter(googleMap, fragment.getContext(), markerIconFactory)));
  }

  @Override
  public Fragment getFragment() {
    if (fragment == null) {
      init(new GoogleMapsFragment());
    }
    return fragment;
  }

  @Override
  public Single<MapAdapter> getMapAdapter() {
    return map;
  }

  @Override
  public int getMapType() {
    return map.getValue().getMapType();
  }

  @Override
  public void setMapType(int mapType) {
    map.getValue().setMapType(mapType);
  }

  @Override
  public ImmutableMap<Integer, String> getMapTypes() {
    // TODO(#711): Allow user to select language and use here.
    Map<Integer, String> map = new HashMap<>();
    map.put(GoogleMap.MAP_TYPE_NORMAL, "Normal");
    map.put(GoogleMap.MAP_TYPE_SATELLITE, "Satellite");
    map.put(GoogleMap.MAP_TYPE_TERRAIN, "Terrain");
    map.put(GoogleMap.MAP_TYPE_HYBRID, "Hybrid");
    return ImmutableMap.copyOf(map);
  }
}
