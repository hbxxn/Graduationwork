package com.example.setting;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class setting_customerRevise extends AppCompatActivity {
    private SQLiteHelper dbHelper;
    private EditText address; // 주소를 표시할 텍스트뷰
    private String tempAddress; // 임시 주소 저장을 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_customer_revise);

        // 데이터베이스 헬퍼 초기화
        dbHelper = new SQLiteHelper(this);

        // XML 레이아웃에서 뷰를 참조
        address = findViewById(R.id.address_search); // 주소를 표시할 텍스트뷰 참조
        address.setFocusable(false); // 주소 EditText를 비활성화 상태로 설정

        // 사용자 아이디를 TextView에서 가져와서 설정
        TextView idTextView = findViewById(R.id.id);
        String userId = PreferenceManager.getSavedUserId(this); // 저장된 사용자 아이디 가져오기
        idTextView.setText(userId); // 아이디 설정

        // 기존 주소를 가져와서 설정
        String existingAddress = dbHelper.getAddressByUserId(userId);
        if (existingAddress != null) {
            address.setText(existingAddress);
            tempAddress = existingAddress; // tempAddress에 기존 주소를 저장
        }

        // 주소 검색 버튼 클릭 시 SearchActivity로 이동
        address.setOnClickListener(v -> {
            Intent intent = new Intent(setting_customerRevise.this, SearchActivity.class);
            startActivityForResult(intent, 1000);  // SearchActivity를 호출, 결과를 받기 위해 startActivityForResult 사용
        });

        // 뒤로 가기 버튼 설정
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish()); // 현재 액티비티를 종료하고 이전 프래그먼트로 돌아감

        // 비밀번호 입력 필드 참조
        EditText password = findViewById(R.id.password);
        EditText passwordCheck = findViewById(R.id.password_check);

        // 수정 버튼 클릭 시
        ImageView reviseButton = findViewById(R.id.reviseButton);
        reviseButton.setOnClickListener(v -> {
            String passwordText = password.getText().toString();
            String passwordCheckText = passwordCheck.getText().toString();

            // 주소에서 "구" 추출
            String gu = getGuFromAddress(tempAddress); // tempAddress 사용

            // 비밀번호 유효성 검사 및 업데이트 로직
            if (!isPasswordValid(passwordText)) {
                Toast.makeText(this, "비밀번호는 최소 6자리 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.", Toast.LENGTH_SHORT).show();
            } else if (!passwordText.equals(passwordCheckText)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else if (gu == null || gu.equals("구 정보 없음")) {
                Toast.makeText(this, "유효한 '구' 정보를 추출할 수 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 회원 정보 업데이트 메서드 호출
                boolean isUpdated = dbHelper.updateMember(userId, passwordText, tempAddress); // tempAddress 사용

                // "구" 업데이트
                boolean isGuUpdated = dbHelper.updateGu(userId, gu);

                if (isUpdated && isGuUpdated) {
                    Toast.makeText(this, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();  // 수정 완료 후 액티비티 종료
                } else {
                    showCustomToast("아이디를 확인해주세요.");
                }
            }
        });

        // 회원 탈퇴 버튼 클릭 시
        ImageView memberDeleteButton = findViewById(R.id.memberDelete);
        memberDeleteButton.setOnClickListener(v -> showDeletePopup());
    }

    // 주소 검색 결과를 처리하는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            if (data != null) {
                tempAddress = data.getStringExtra("data");  // "address" 대신 "data"로 변경
                address.setText(tempAddress);  // 받은 주소 데이터를 address 텍스트뷰에 설정
            }
        }
    }

    // AddressUtil 클래스를 이용해 주소에서 "구" 추출
    private String getGuFromAddress(String addressStr) {
        AddressUtil.LatLng latLng = AddressUtil.getLatLngFromAddress(this, addressStr);
        if (latLng != null) {
            return AddressUtil.getGuFromCoordinates(this, latLng.latitude, latLng.longitude);
        }
        return "구 정보 없음";
    }

    // 비밀번호 유효성을 검사하는 메서드
    private boolean isPasswordValid(String password) {
        // 비밀번호가 최소 8자리 이상이고 영문, 숫자, 특수문자(!와 _)를 반드시 포함하는지 확인
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!_])[A-Za-z\\d!_]{8,}$";
        return Pattern.matches(passwordPattern, password);
    }

    // 사용자 정의 Toast 메시지를 표시하는 메서드
    private void showCustomToast(String message) {
        // 사용자 정의 레이아웃 인플레이트
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

        // 레이아웃에서 ImageView와 TextView를 가져와 설정
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
        TextView toastText = layout.findViewById(R.id.toast_text);
        toastText.setText(message);
        toastIcon.setImageResource(R.drawable.p_logo); // 사용자 정의 아이콘 설정

        // Toast 생성 및 표시
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // 회원 탈퇴 확인 팝업을 표시하는 메서드
    private void showDeletePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.delete_choice_, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button confirmDeleteButton = dialogView.findViewById(R.id.yes);
        Button cancelButton = dialogView.findViewById(R.id.no);

        confirmDeleteButton.setOnClickListener(v -> {
            // 사용자 아이디와 비밀번호 입력 필드 가져오기
            TextView idField = findViewById(R.id.id);
            String userId = idField.getText().toString(); // ID
            EditText passwordField = findViewById(R.id.password);
            String userPassword = passwordField.getText().toString(); // 비밀번호
            EditText passwordCheckField = findViewById(R.id.password_check);
            String passwordCheck = passwordCheckField.getText().toString(); // 비밀번호 확인

            // 비밀번호 확인
            if (!userPassword.equals(passwordCheck)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 회원 삭제 처리
                boolean isDeleted = dbHelper.deleteMember(userId, userPassword);

                if (isDeleted) {
                    // 로그인 관련 정보 초기화
                    SharedPreferences loginPrefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor loginEditor = loginPrefs.edit();
                    loginEditor.clear(); // 모든 저장된 값 초기화 (로그인 유지 설정 포함)
                    loginEditor.apply();

                    // 튜토리얼 관련 정보 초기화
                    SharedPreferences tutorialPrefs = getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor tutorialEditor = tutorialPrefs.edit();
                    tutorialEditor.clear(); // 튜토리얼 관련 정보 초기화
                    tutorialEditor.apply();

                    Toast.makeText(this, "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    // LoginActivity로 이동
                    Intent intent = new Intent(setting_customerRevise.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "아이디나 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            dialog.dismiss(); // 다이얼로그 닫기
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // 다이얼로그 닫기

        dialog.show();
    }
}
