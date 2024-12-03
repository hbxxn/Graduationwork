package com.example.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordedVideoAdapter extends RecyclerView.Adapter<RecordedVideoAdapter.VideoViewHolder> {

    private List<RecordedVideo> videoList;     // 전체 비디오 리스트
    private final List<RecordedVideo> selectedVideos; // 선택된 비디오 리스트
    private final IndoorFragment fragment;      // Fragment 참조

    public RecordedVideoAdapter(List<RecordedVideo> videoList, List<RecordedVideo> selectedVideos, IndoorFragment fragment) {
        this.videoList = new ArrayList<>(videoList);
        this.selectedVideos = selectedVideos;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recorded_video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        RecordedVideo videoItem = videoList.get(position);
        holder.bind(videoItem, fragment); // fragment를 전달
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {

        private final TextView recordedTime;
        private final CheckBox videoCheckbox;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            recordedTime = itemView.findViewById(R.id.recorded_time);
            videoCheckbox = itemView.findViewById(R.id.video_checkbox);
        }

        void bind(RecordedVideo videoItem, IndoorFragment fragment) { // fragment를 인자로 추가
            recordedTime.setText(videoItem.getTitle());
            videoCheckbox.setOnCheckedChangeListener(null);
            videoCheckbox.setChecked(videoItem.isSelected());

            videoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                videoItem.setSelected(isChecked);
                if (isChecked) {
                    fragment.addSelectedVideo(videoItem);  // 선택된 비디오 추가
                } else {
                    fragment.removeSelectedVideo(videoItem); // 선택 해제
                }
            });

            itemView.setOnClickListener(v -> {
                String selectedVideoUrl = videoItem.getVideoUrl();
                fragment.loadVideoInWebView(selectedVideoUrl);  // WebView에 비디오 로드
            });
        }
    }

    public void updateData(List<RecordedVideo> newData) {
        videoList.clear(); // 기존 리스트 비우기
        videoList.addAll(newData); // 새로운 데이터를 추가
        notifyDataSetChanged(); // UI 업데이트
    }

    public List<RecordedVideo> getVideoList() {
        return videoList; // 비디오 리스트 반환
    }
}