package com.example.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordedVideoPagerAdapter extends RecyclerView.Adapter<RecordedVideoPagerAdapter.VideoPageViewHolder> {

    private List<List<RecordedVideo>> pages; // RecordedVideo 리스트로 변경
    private final IndoorFragment fragment; // IndoorFragment 참조

    public RecordedVideoPagerAdapter(List<List<RecordedVideo>> pages, IndoorFragment fragment) {
        this.pages = pages;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public VideoPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_layout, parent, false);
        return new VideoPageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoPageViewHolder holder, int position) {
        List<RecordedVideo> page = pages.get(position); // RecordedVideo로 변경
        holder.bind(page);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public void updateData(List<RecordedVideo> videos) {
        // 5개씩 페이지로 나누기
        pages = new ArrayList<>();
        for (int i = 0; i < videos.size(); i += 5) {
            pages.add(videos.subList(i, Math.min(i + 5, videos.size())));
        }
        notifyDataSetChanged();
    }

    // 비디오 리스트를 반환하는 메서드
    public List<RecordedVideo> getVideoList() {
        List<RecordedVideo> allVideos = new ArrayList<>();
        for (List<RecordedVideo> page : pages) {
            allVideos.addAll(page);
        }
        return allVideos;
    }

    class VideoPageViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerView;

        public VideoPageViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.page_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        public void bind(List<RecordedVideo> videos) {
            if (fragment != null && fragment.getActivity() != null) {
                List<RecordedVideo> selectedVideos = fragment.getSelectedVideos();
                RecordedVideoAdapter adapter = new RecordedVideoAdapter(videos, selectedVideos, fragment);
                recyclerView.setAdapter(adapter);
            }
        }
    }
}