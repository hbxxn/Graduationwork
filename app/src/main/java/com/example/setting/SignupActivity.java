package com.example.setting;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.setting.databinding.ActivitySignupBinding;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]{6,}$";
    private static final int DRAWABLE_END = 2;

    private EditText editId, editPassword, editPasswordCheck, editAddress;
    private Button registerButton, backButton, idCheckButton;
    private SQLiteHelper sqLiteHelper;
    private ActivitySignupBinding binding;
    private boolean isIdChecked = false;
    private boolean isPasswordVisible = false;
    private boolean isPasswordCheckVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sqLiteHelper = new SQLiteHelper(this);

        initViews();
        setListeners();
    }

    private void initViews() {
        editId = binding.editId;
        editPassword = binding.editPasswd;
        editPasswordCheck = binding.editPasswdCheck;
        editAddress = binding.editAdr;
        registerButton = binding.registerBtn2;
        backButton = binding.registerBack;
        idCheckButton = binding.idCheck;

        editAddress.setFocusable(false); // 주소 EditText를 비활성화 상태로 설정
    }

    private void setListeners() {
        idCheckButton.setOnClickListener(v -> checkIdAvailability());
        registerButton.setOnClickListener(v -> registerUser());
        editAddress.setOnClickListener(v -> openSearchActivity());
        backButton.setOnClickListener(v -> navigateToLogin());

        // 비밀번호 가시성 토글 리스너
        editPassword.setOnTouchListener((v, event) -> handlePasswordVisibilityToggle(v, event, editPassword));
        editPasswordCheck.setOnTouchListener((v, event) -> handlePasswordVisibilityToggle(v, event, editPasswordCheck));

        // 비밀번호 유효성 검사
        TextWatcher passwordWatcher = new PasswordTextWatcher();
        editPassword.addTextChangedListener(passwordWatcher);
        editPasswordCheck.addTextChangedListener(passwordWatcher);
    }

    // 비밀번호 가시성 토글
    private boolean handlePasswordVisibilityToggle(View v, MotionEvent event, EditText passwordField) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Drawable endDrawable = passwordField.getCompoundDrawables()[DRAWABLE_END];
            if (endDrawable != null && event.getRawX() >= (passwordField.getRight() - endDrawable.getBounds().width())) {
                togglePasswordVisibility(passwordField);
                return true;
            }
        }
        return false;
    }

    // 비밀번호 가시성 설정
    private void togglePasswordVisibility(EditText passwordField) {
        boolean isVisible = passwordField == editPassword ? (isPasswordVisible = !isPasswordVisible) : (isPasswordCheckVisible = !isPasswordCheckVisible);
        int inputType = isVisible ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        passwordField.setInputType(inputType);

        Drawable visibilityIcon = ContextCompat.getDrawable(this, isVisible ? R.drawable.passwd_eyes_on : R.drawable.passwd_eyes_off);
        Drawable startDrawable = getStartDrawableForField(passwordField.getId());

        passwordField.setCompoundDrawablesWithIntrinsicBounds(startDrawable, null, visibilityIcon, null);
        passwordField.setSelection(passwordField.getText().length());
    }

    // EditText의 시작 드로어블 설정
    private Drawable getStartDrawableForField(int fieldId) {
        if (fieldId == R.id.edit_passwd) {
            return ContextCompat.getDrawable(this, R.drawable.custom_ic_https);
        } else if (fieldId == R.id.edit_passwd_check) {
            return ContextCompat.getDrawable(this, R.drawable.baseline_check_circle_24_no);
        }
        return null;
    }

    // 비밀번호와 확인 비밀번호 유효성 검사
    private void validatePasswords() {
        String password = editPassword.getText().toString();
        String passwordCheck = editPasswordCheck.getText().toString();

        if (password.equals(passwordCheck)) {
            setValidationDrawable(editPasswordCheck, R.drawable.baseline_check_circle_24, R.drawable.rectangle);
        } else {
            setValidationDrawable(editPasswordCheck, R.drawable.baseline_check_circle_24_no, R.drawable.rectangle_error);
        }
    }

    // 유효성 검사에 따른 드로어블 및 배경 설정
    private void setValidationDrawable(EditText editText, int drawableId, int backgroundResId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        editText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, getEndDrawableForField(editText), null);
        editText.setBackgroundResource(backgroundResId);
    }

    // 비밀번호 확인 필드의 끝 드로어블 설정
    private Drawable getEndDrawableForField(EditText editText) {
        return ContextCompat.getDrawable(this, isPasswordCheckVisible ? R.drawable.passwd_eyes_on : R.drawable.passwd_eyes_off);
    }

    // ID 중복 확인
    private void checkIdAvailability() {
        String userId = editId.getText().toString();
        if (userId.isEmpty()) {
            showToast("ID를 입력하세요.");
        } else if (!isEnglishLettersOnly(userId)) {
            showToast("ID는 영문만 입력할 수 있습니다.");
            isIdChecked = false;
        } else {
            try {
                isIdChecked = !sqLiteHelper.checkId(userId);
                showToast(isIdChecked ? "사용 가능한 ID입니다." : "이미 존재하는 ID입니다.");
            } catch (Exception e) {
                showToast("ID 중복 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
                e.printStackTrace();
            }
        }
    }

    // 사용자 등록
    private void registerUser() {
        String userId = editId.getText().toString();
        String userPassword = editPassword.getText().toString();
        String address = editAddress.getText().toString();

        if (userId.isEmpty() || userPassword.isEmpty() || address.isEmpty()) {
            showToast("모든 필드를 입력해주세요.");
        } else if (!isIdChecked) {
            showToast("ID 중복 확인을 해주세요.");
        } else if (!isPasswordValid(userPassword)) {
            showToast("비밀번호는 6자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.");
        } else {
            boolean result = sqLiteHelper.addMember(userId, userPassword, address);
            if (result) {
                Myapp myApp = (Myapp) getApplicationContext();
                double latitude = 0; // 위치 정보 필요 시 구현
                double longitude = 0;
                User newUser = new User(userId, "deviceId", latitude, longitude, address);
                myApp.setCurrentUser(newUser);

                PreferenceManager.saveUserId(SignupActivity.this, userId);
                PreferenceManager.saveUserPassword(SignupActivity.this, userPassword);

                showToast("회원가입이 완료되었습니다.");
                navigateToLogin();
            } else {
                showToast("회원가입에 실패했습니다. 정보를 확인해주세요.");
            }
        }
    }

    // 검색 액티비티 열기
    private void openSearchActivity() {
        Intent intent = new Intent(SignupActivity.this, SearchActivity.class);
        getSearchResult.launch(intent);
    }

    // 로그인 액티비티로 이동
    private void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // ID가 영문으로만 이루어져 있는지 확인
    private boolean isEnglishLettersOnly(String id) {
        return id.matches("[a-zA-Z]+");
    }

    // 비밀번호 유효성 검사
    private boolean isPasswordValid(String password) {
        return Pattern.matches(PASSWORD_PATTERN, password);
    }

    // 검색 결과를 받기 위한 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String data = result.getData().getStringExtra("data");
                    if (data != null) {
                        editAddress.setText(data);
                    } else {
                        showToast("주소 정보를 가져오는 데 실패했습니다.");
                    }
                }
            }
    );

    // 토스트 메시지 출력
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 비밀번호 유효성 검사 TextWatcher 클래스
    private class PasswordTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            validatePasswords();
        }
    }
}
