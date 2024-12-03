package com.example.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.Nullable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Myapp myApp; // 전역 상태 관리 클래스

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        ImageView sCustomer = view.findViewById(R.id.s_customer);
        sCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), setting_customerRevise.class);
            startActivity(intent);
        });

        ImageView sHelp = view.findViewById(R.id.s_help);
        sHelp.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), setting_help.class);
            startActivity(intent);
        });

        ImageView sVersion = view.findViewById(R.id.s_version);
        sVersion.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), setting_version.class);
            startActivity(intent);
        });

        TextView logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            // SharedPreferences 초기화
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // 모든 저장된 값 초기화
            editor.apply();

            // 전역 상태 초기화
            if (myApp != null) {
                myApp.setCurrentUser(null); // 전역 사용자 정보 초기화
            }

            // Toast 메시지 표시
            Toast.makeText(getContext(), "로그아웃 됐습니다.", Toast.LENGTH_SHORT).show();

            // LoginActivity로 이동하면서 EditText와 CheckBox 초기화
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

}

