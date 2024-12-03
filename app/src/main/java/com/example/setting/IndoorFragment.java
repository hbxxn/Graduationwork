package com.example.setting;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.Toast;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IndoorFragment extends Fragment {

    private WebView cctvWebview;
    private ViewPager2 viewPager;
    private RecordedVideoPagerAdapter adapter;
    private List<RecordedVideo> selectedVideo; // 선택된 비디오 관리
    private User currentUser;
    private SQLiteHelper dbHelper;
    private String deviceID;
    private String defaultUrl;

    public IndoorFragment() {
        // 필수적으로 필요한 빈 생성자
        selectedVideo = new ArrayList<>();
    }

    // DB에서 deviceId 가져오기
    private String getDeviceIdFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                SQLiteHelper.TABLE_DEVICE,
                new String[]{SQLiteHelper.COLUMN_DEVICE_ID},
                SQLiteHelper.COLUMN_USER_ID + "=?",
                new String[]{currentUser.getUserId()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String deviceId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_DEVICE_ID));
            cursor.close();
            return deviceId;
        }

        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_indoor, container, false);

        Myapp app = (Myapp) getActivity().getApplication();
        currentUser = app.getCurrentUser();
        dbHelper = new SQLiteHelper(getContext());

        // deviceId 설정
        if (currentUser != null) {
            deviceID = getDeviceIdFromDB();
        }

        // WebView 설정
        cctvWebview = rootView.findViewById(R.id.cctvWebview);
        cctvWebview.getSettings().setJavaScriptEnabled(true);
        defaultUrl = "https://ict.nanum.info/video?deviceID=" + deviceID;
        cctvWebview.loadUrl(defaultUrl);
        cctvWebview.setWebChromeClient(new WebChromeClient());
        cctvWebview.setWebViewClient(new WebViewClient());

        // ViewPager2 및 어댑터 설정
        viewPager = rootView.findViewById(R.id.recorded_videos_view_pager);
        adapter = new RecordedVideoPagerAdapter(new ArrayList<>(), this); // 어댑터 초기화
        viewPager.setAdapter(adapter); // ViewPager2에 어댑터 설정

        // 서버에서 영상 리스트 가져오기
        new FetchVideosTask(this, adapter, deviceID).execute();

        // 질문 이미지 클릭 시 팝업 표시
        ImageView questionImageView = rootView.findViewById(R.id.question);
        questionImageView.setOnClickListener(v -> showPopup());

        // 삭제 버튼 클릭 처리
        ImageView rDelete = rootView.findViewById(R.id.r_delete);
        rDelete.setOnClickListener(v -> deleteSelectedVideos());

        return rootView;
    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.record_i, null);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    // 비디오 URL을 WebView에 로드하는 메서드
    public void loadVideoInWebView(String videoUrl) {
        if (cctvWebview != null) {
            cctvWebview.loadUrl(videoUrl);
            Log.d("loadVideoInWebView", "videoUrl: " + videoUrl);
        }
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public List<RecordedVideo> getSelectedVideos() {
        return selectedVideo; // 선택된 비디오 리스트 반환
    }

    // 선택된 비디오를 삭제하는 함수
    private void deleteSelectedVideos() {
        if (selectedVideo != null && !selectedVideo.isEmpty()) {
            for (RecordedVideo video : selectedVideo) {
                String deleteUrl = "https://ict.nanum.info/videoDel?deviceID=" + deviceID + "&dirName=" + video.getDirName();
                // API 호출 로직
                deleteVideoFromServer(deleteUrl, video);
            }

            selectedVideo.clear();
            adapter = new RecordedVideoPagerAdapter(new ArrayList<>(), this);
            viewPager.setAdapter(adapter);
            // 서버에서 영상 리스트 가져오기
            new FetchVideosTask(this, adapter, deviceID).execute();
            loadVideoInWebView(defaultUrl);
        }
    }

    private void showCustomToast(String message) {
        if (getActivity() != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View toastLayout = inflater.inflate(R.layout.custom_toast, null);

            TextView toastText = toastLayout.findViewById(R.id.toast_text);
            toastText.setText(message);

            Toast toast = new Toast(getActivity());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastLayout);
            toast.show();
        }
    }

    private void deleteVideoFromServer(String deleteUrl, RecordedVideo video) {
        new DeleteVideoTask().execute(deleteUrl);   // 비동기 DELETE 요청 실행
        showCustomToast("영상이 삭제되었습니다");         // Toast 메시지 표시
        adapter.getVideoList().remove(video);       // video 객체 삭제
        adapter.notifyDataSetChanged();             // 어댑터에 데이터 변경 알림
    }

    public void addSelectedVideo(RecordedVideo video) {
        if (!selectedVideo.contains(video)) {
            selectedVideo.add(video); // 선택된 비디오 추가
        }
    }

    public void removeSelectedVideo(RecordedVideo video) {
        selectedVideo.remove(video); // 선택 해제
    }
}