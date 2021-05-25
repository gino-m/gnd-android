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

package com.google.android.gnd.ui.home.mapcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.gnd.R;
import com.google.android.gnd.ui.home.BottomSheetDependentBehavior;
import com.google.android.gnd.ui.home.BottomSheetMetrics;

public class MapContainerLayoutBehavior extends BottomSheetDependentBehavior<FrameLayout> {

  public MapContainerLayoutBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onBottomSheetChanged(
      CoordinatorLayout parent, FrameLayout mapContainerLayout, BottomSheetMetrics metrics) {
    if (metrics.getPeekHeight() <= 0) {
      return;
    }
    View map = mapContainerLayout.findViewById(R.id.map);
    View mapControls = mapContainerLayout.findViewById(R.id.map_controls);
    if (map == null || mapControls == null) {
      // View already destroyed.
      return;
    }

    // always keep the map centered based on the visible portion of map (excluding status bar)
    if (metrics.getVisibleHeight() >= metrics.getExpandedOffset()) {
      int translationY = -(metrics.getVisibleHeight() - metrics.getExpandedOffset()) / 2;
      map.setTranslationY(translationY);
    } else {
      map.setTranslationY(0);
    }

    float hideRatio = 1.0f - metrics.getRevealRatio();
    mapControls.setAlpha(hideRatio);
  }
}
