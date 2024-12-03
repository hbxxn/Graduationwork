package com.example.setting;

import android.os.Parcel;
import android.os.Parcelable;

// RecordedVideo 클래스
public class RecordedVideo implements Parcelable {
    private String time;          // 비디오 시간
    private String title;         // 비디오 제목
    private String videoUrl;      // 비디오 URL
    private boolean isSelected;    // 체크박스 선택 여부
    private String dirName;       // 비디오 디렉터리 이름

    // 생성자
    public RecordedVideo(String time, String title, String videoUrl, boolean isSelected, String dirName) {
        this.time = time;
        this.title = title;
        this.videoUrl = videoUrl;
        this.isSelected = isSelected;
        this.dirName = dirName;
    }

    // Parcelable 생성자
    protected RecordedVideo(Parcel in) {
        time = in.readString();
        title = in.readString();
        videoUrl = in.readString();
        isSelected = in.readByte() != 0;
        dirName = in.readString();
    }

    // Creator
    public static final Creator<RecordedVideo> CREATOR = new Creator<RecordedVideo>() {
        @Override
        public RecordedVideo createFromParcel(Parcel in) {
            return new RecordedVideo(in);
        }

        @Override
        public RecordedVideo[] newArray(int size) {
            return new RecordedVideo[size];
        }
    };

    // Getter & Setter
    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getDirName() {
        return dirName;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeString(title);
        dest.writeString(videoUrl);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(dirName);
    }
}
