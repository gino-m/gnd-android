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

package com.google.android.gnd.system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gnd.inject.PerActivity;
import com.google.android.gnd.model.Point;

import javax.inject.Inject;

import java8.util.function.Consumer;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.google.android.gms.location.LocationServices.getSettingsClient;

@PerActivity
public class LocationManager {
  public static final int REQUEST_CHECK_SETTINGS = 0x200;
  private static final long UPDATE_INTERVAL = 5 * 1000 /* 5 sec */;
  private static final long FASTEST_INTERVAL = 250;
  private static final LocationRequest LOCATION_REQUEST =
      new LocationRequest()
          .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
          .setInterval(UPDATE_INTERVAL)
          .setFastestInterval(FASTEST_INTERVAL);
  private static final LocationSettingsRequest LOCATION_SETTINGS_REQUEST =
      new LocationSettingsRequest.Builder().addLocationRequest(LOCATION_REQUEST).build();
  private static final String TAG = LocationManager.class.getSimpleName();
  private final Activity activity;
  private final PermissionManager permissionManager;
  private LocationCallback locationCallback;

  @Inject
  public LocationManager(Activity context, PermissionManager permissionManager) {
    this.activity = context;
    this.permissionManager = permissionManager;
  }

  private static Point toPoint(Location location) {
    return Point.newBuilder()
        .setLatitude(location.getLatitude())
        .setLongitude(location.getLongitude())
        .build();
  }

  public void checkLocationSettings(Runnable onSuccess, Consumer<LocationFailureReason> onFailure) {
    getSettingsClient(activity)
        .checkLocationSettings(LOCATION_SETTINGS_REQUEST)
        .addOnSuccessListener((v) -> onSuccess.run())
        .addOnFailureListener(e -> handleLocationSettingsFailure(e, onFailure));
  }

  /** Must check fine-grained location permission and location settings before calling this! */
  @SuppressLint("MissingPermission")
  public void requestLocationUpdates(
      Runnable onSuccess,
      Consumer<LocationFailureReason> onFailure,
      Consumer<Point> onLocationUpdate) {
    LocationCallback callback = new LocationCallbackImpl(onLocationUpdate);
    getFusedLocationProviderClient(activity)
        .requestLocationUpdates(LOCATION_REQUEST, callback, Looper.myLooper())
        .addOnSuccessListener(
            v -> {
              locationCallback = callback;
              onSuccess.run();
            })
        .addOnFailureListener(e -> this.handleRequestLocationUpdatesFailure(e, onFailure));
  }

  private void handleLocationSettingsFailure(
      Throwable exception, Consumer<LocationFailureReason> onFailure) {
    if (!(exception instanceof ApiException)) {
      Log.e(TAG, "Unexpected error: " + exception);
      onFailure.accept(LocationFailureReason.UNEXPECTED_ERROR);
      return;
    }
    switch (((ApiException) exception).getStatusCode()) {
      case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
        handleResolvableLocationSettingsFailure(exception);
        break;
      case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
        onFailure.accept(LocationFailureReason.SETTINGS_CHANGE_UNAVAILABLE);
        break;
      default:
        Log.e(TAG, "Unexpected error: " + exception);
        onFailure.accept(LocationFailureReason.UNEXPECTED_ERROR);
    }
  }

  private void handleResolvableLocationSettingsFailure(Throwable exception) {
    // Location settings are not satisfied. But could be fixed by showing the
    // user a dialog.
    try {
      ResolvableApiException resolvable = (ResolvableApiException) exception;
      // The result of this action is received by {@link MainActivity#onActivityResult}.
      // TODO: Settings resolution is tightly bound to Activity.. move there?
      resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
    } catch (IntentSender.SendIntentException e) {
      // Should be an impossible since we don't use intents..
    } catch (ClassCastException e) {
      // Should be an impossible.
      Log.w(TAG, e);
    }
  }

  private void handleRequestLocationUpdatesFailure(
      Exception e, Consumer<LocationFailureReason> onFailure) {
    Log.w(TAG, "Location updates request failed", e);
    onFailure.accept(LocationFailureReason.LOCATION_UPDATES_REQUEST_FAILED);
  }

  public void removeLocationUpdates() {
    if (locationCallback != null) {
      getFusedLocationProviderClient(activity).removeLocationUpdates(locationCallback);
      locationCallback = null;
    }
  }

  @SuppressLint("MissingPermission")
  public void requestLastLocation(Consumer<Point> onSuccess) {
    getFusedLocationProviderClient(activity)
        .getLastLocation()
        .addOnSuccessListener(
            l -> {
              if (l != null) {
                onSuccess.accept(toPoint(l));
              }
            });
  }

  public enum LocationFailureReason {
    UNEXPECTED_ERROR,
    LOCATION_UPDATES_REQUEST_FAILED,
    SETTINGS_CHANGE_FAILED,
    SETTINGS_CHANGE_UNAVAILABLE
  }

  private class LocationCallbackImpl extends LocationCallback {
    private final Consumer<Point> onLocationUpdate;

    public LocationCallbackImpl(Consumer<Point> onLocationUpdate) {
      this.onLocationUpdate = onLocationUpdate;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
      Location lastLocation = locationResult.getLastLocation();
      onLocationUpdate.accept(toPoint(lastLocation));
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
      // TODO: Show warning when location no longer available.
    }
  }
}