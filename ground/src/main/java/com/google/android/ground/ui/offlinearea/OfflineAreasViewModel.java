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

package com.google.android.ground.ui.offlinearea;

import com.google.android.ground.ui.common.AbstractViewModel;
import com.google.android.ground.ui.common.Navigator;
import javax.inject.Inject;

/**
 * View model for the offline area manager fragment. Handles the current list of downloaded areas.
 */
public class OfflineAreasViewModel extends AbstractViewModel {

  private final Navigator navigator;
  // TODO: Implement the ViewModel

  @Inject
  OfflineAreasViewModel(Navigator navigator) {
    this.navigator = navigator;
  }

  public void showOfflineAreaSelector() {
    navigator.showOfflineAreaSelector();
  }
}
