/*
 * Copyright 2021 Google LLC
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

package com.google.android.gnd.ui.terms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.google.android.gnd.databinding.FragmentTermsServiceBinding;
import com.google.android.gnd.ui.common.AbstractFragment;
import com.google.android.gnd.ui.common.BackPressListener;

public class TermsOfServiceFragment extends AbstractFragment implements BackPressListener {

  private TermsOfServiceViewModel viewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.viewModel = getViewModel(TermsOfServiceViewModel.class);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    FragmentTermsServiceBinding binding = FragmentTermsServiceBinding
        .inflate(inflater, container, false);
    binding.setViewModel(viewModel);
    binding.setLifecycleOwner(this);
    return binding.getRoot();
  }

  @Override
  public boolean onBack() {
    getActivity().finish();
    return false;
  }
}
