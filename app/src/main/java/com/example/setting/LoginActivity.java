package com.example.setting;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.setting.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sqLiteHelper = new SQLiteHelper(this);

        setupSharedPreferences();
        setupListeners();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("remember_me", false);
        CheckBox rememberMeCheckBox = binding.rememberMeCheckbox;
        rememberMeCheckBox.setChecked(isRemembered);

        if (isRemembered) {
            String savedUserId = PreferenceManager.getSavedUserId(this);
            String savedPassword = PreferenceManager.getSavedUserPassword(this);

            binding.editId.setText(savedUserId);
            binding.editPasswd.setText(savedPassword);
        }
    }


    private void setupListeners() {
        binding.loginBtn.setOnClickListener(view -> attemptLogin());
        binding.registerBtn.setOnClickListener(view -> navigateToSignup());
    }

    private void attemptLogin() {
        String userId = binding.editId.getText().toString();
        String userPassword = binding.editPasswd.getText().toString();

        if (userId.isEmpty() || userPassword.isEmpty()) {
            showToast("아이디와 비밀번호를 확인해주세요!");
        } else {
            if (sqLiteHelper.checkIdPassword(userId, userPassword)) {
                handleSuccessfulLogin(userId, userPassword);
            } else {
                showToast("로그인 실패: 잘못된 아이디 또는 비밀번호입니다.");
            }
        }
    }

    private void handleSuccessfulLogin(String userId, String userPassword) {
        boolean rememberMeChecked = binding.rememberMeCheckbox.isChecked();
        PreferenceManager.setRememberMe(this, rememberMeChecked);

        if (rememberMeChecked) {
            // Myapp에 저장된 User 정보 사용
            Myapp myApp = (Myapp) getApplicationContext();
            User currentUser = new User(userId, "deviceId", 0.0, 0.0, userPassword, ""); // deviceId 등 필요한 정보 추가
            myApp.setCurrentUser(currentUser); // 로그인 후 Myapp에 User 저장

            // 아이디와 비밀번호를 Preference에 저장
            PreferenceManager.saveUserId(this, userId);
            PreferenceManager.saveUserPassword(this, userPassword);
        } else {
            PreferenceManager.clearSavedUserCredentials(this);
        }

        Myapp myApp = (Myapp) getApplicationContext();
        User currentUser = myApp.getCurrentUser(); // Myapp에서 User 정보 가져옴

        boolean tutorialCompleted = PreferenceManager.isTutorialCompleted(this, userId);
        Intent intent = tutorialCompleted ?
                new Intent(getApplicationContext(), main_hadanbar.class) :
                new Intent(getApplicationContext(), NavigationActivity.class);

        startActivity(intent);
        finish();
    }

    private void navigateToSignup() {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
