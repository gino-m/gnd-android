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

package com.google.android.ground.ui.home.featuresheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import com.google.android.ground.R;
import com.google.android.ground.databinding.ObservationListFragBinding;
import com.google.android.ground.model.feature.Feature;
import com.google.android.ground.model.form.Form;
import com.google.android.ground.model.observation.Observation;
import com.google.android.ground.ui.common.AbstractFragment;
import com.google.android.ground.ui.common.Navigator;
import com.google.android.ground.ui.home.HomeScreenViewModel;
import java8.util.Optional;
import javax.inject.Inject;

public class ObservationListFragment extends AbstractFragment {

  @Inject Navigator navigator;
  private ObservationListAdapter observationListAdapter;
  private ObservationListViewModel viewModel;
  private FeatureSheetViewModel featureSheetViewModel;
  private HomeScreenViewModel homeScreenViewModel;

  @BindView(R.id.observation_list_container)
  RecyclerView recyclerView;

  static ObservationListFragment newInstance() {
    return new ObservationListFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    observationListAdapter = new ObservationListAdapter(viewModelFactory);
    super.onCreate(savedInstanceState);
    viewModel = getViewModel(ObservationListViewModel.class);
    featureSheetViewModel = getViewModel(FeatureSheetViewModel.class);
    homeScreenViewModel = getViewModel(HomeScreenViewModel.class);

    observationListAdapter.getItemClicks().observe(this, this::onItemClick);
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    ObservationListFragBinding binding =
        ObservationListFragBinding.inflate(inflater, container, false);
    binding.setViewModel(viewModel);
    binding.setLifecycleOwner(this);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(observationListAdapter);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel.getObservations().observe(this, observationListAdapter::update);
    featureSheetViewModel.getSelectedForm().observe(this, this::onFormChange);
  }

  private void onItemClick(Observation observation) {
    navigator.showObservationDetails(
        observation.getProject().getId(), observation.getFeature().getId(), observation.getId());
  }

  private void onFormChange(Optional<Form> form) {
    observationListAdapter.clear();
    // TODO: Use fragment args, load form and feature if not present.
    Optional<Feature> feature = featureSheetViewModel.getSelectedFeature().getValue();
    if (!form.isPresent() || !feature.isPresent()) {
      // TODO: Report error.
      return;
    }
    homeScreenViewModel.onFormChange(form.get());
    viewModel.loadObservationList(feature.get(), form.get());
  }
}
