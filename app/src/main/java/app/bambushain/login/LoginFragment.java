package app.bambushain.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import app.bambushain.R;
import app.bambushain.api.BambooApi;
import app.bambushain.base.BindingFragment;
import app.bambushain.databinding.FragmentLoginBinding;
import app.bambushain.models.authentication.ForgotPasswordRequest;
import app.bambushain.models.authentication.LoginRequest;
import app.bambushain.models.authentication.TwoFactorRequest;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

@AndroidEntryPoint
public class LoginFragment extends BindingFragment<FragmentLoginBinding> {
    private static final String TAG = LoginFragment.class.getName();
    LoginViewModel viewModel;

    @Inject
    BambooApi bambooApi;

    @Inject
    public LoginFragment() {
    }

    @Override
    protected FragmentLoginBinding getViewBinding() {
        return FragmentLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.loginButton.setOnClickListener(v -> login());
        binding.loginForgotPassword.setOnClickListener(v -> forgotPassword());
    }

    private void showErrorSnackbar(@StringRes int message) {
        Snackbar
                .make(binding.layout, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.md_theme_error))
                .setTextColor(getColor(R.color.md_theme_onError))
                .show();
    }

    private void forgotPassword() {
        bambooApi
                .forgotPassword(new ForgotPasswordRequest(viewModel.email.getValue()))
                .subscribe(() -> {
                    Log.d(TAG, "onForgotPassword: Password reset email sent");
                    Toast.makeText(getContext(), R.string.success_forgot_password, Toast.LENGTH_LONG);
                }, ex -> {
                    Log.e(TAG, "onForgotPassword: Failed to send password reset email", ex);
                    showErrorSnackbar(R.string.error_forgot_password_failed);
                });
    }

    private void login() {
        Log.d(TAG, "onLogin: Perform login with email " + viewModel.email.getValue());
        if (Boolean.FALSE.equals(viewModel.twoFactorRequested.getValue())) {
            bambooApi
                    .requestTwoFactorCode(new TwoFactorRequest(viewModel.email.getValue(), viewModel.password.getValue()))
                    .subscribe(() -> {
                        Log.d(TAG, "onLogin: Email and password are valid enable 2fa");
                        viewModel.twoFactorRequested.setValue(true);
                    }, ex -> {
                        Log.e(TAG, "onLogin: Two factor request failed", ex);
                        showErrorSnackbar(R.string.error_login_failed_two_factor);
                    });
        } else {
            bambooApi
                    .login(new LoginRequest(viewModel.email.getValue(), viewModel.password.getValue(), viewModel.twoFactorCode.getValue()))
                    .subscribe(response -> {
                        Log.d(TAG, "onLogin: Login successful, redirect to main view");
                        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        sharedPrefs
                                .edit()
                                .putString(getString(app.bambushain.api.R.string.bambooAuthenticationToken), response.getToken())
                                .apply();
                        navigator.navigate(R.id.action_fragment_login_to_fragment_event_calendar);
                    }, ex -> {
                        Log.e(TAG, "onLogin: Login failed", ex);
                        showErrorSnackbar(R.string.error_login_failed_login);
                    });
        }
    }
}