package com.example.onlineteach.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentRegistrationBinding;
import com.example.onlineteach.dialog.LottieLoadingDialog;
import com.example.onlineteach.utils.ToastUtils;

public class RegistrationFragment extends Fragment {

    private RegistrationViewModel mViewModel;
    private FragmentRegistrationBinding binding;
    private LottieLoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
        loadingDialog = new LottieLoadingDialog(requireContext());

        // Observe Registration Result
        mViewModel.getRegistrationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    ToastUtils.showShortToast(getContext(), result.getMessage());
                    // Navigate back to Login
                    NavController navController = Navigation.findNavController(binding.getRoot());
                    navController.navigate(R.id.action_registrationFragment_to_loginFragment);
                } else {
                    ToastUtils.showShortToast(getContext(), result.getMessage());
                }
            }
        });

        // Observe Loading State
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingDialog.show();
            } else {
                loadingDialog.dismiss();
            }
        });

        // Register Button Click
        binding.buttonRegister.setOnClickListener(v -> {
            String username = binding.editTextUsername.getText().toString().trim();
            String studentId = binding.editTextStudentId.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            mViewModel.registerUser(username, studentId, password);
        });

        // Login Link Click
        binding.textViewLoginLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_registrationFragment_to_loginFragment);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}