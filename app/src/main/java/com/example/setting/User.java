package com.example.setting;

public class User {
    private String userId;
    private String deviceId; // final을 제거
    private double latitude;
    private double longitude;
    private String gu; // 구(동네) 정보
    private int tutorialPage; // 현재 튜토리얼 페이지
    private boolean isTutorialCompleted; // 튜토리얼 완료 여부
    private String password; // 비밀번호 필드 추가
    private String address; // 주소 필드 추가

    // 기본 생성자
    public User() {
        this.userId = "";
        this.deviceId = ""; // 기본값 설정 (빈 문자열)
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.gu = ""; // 기본값 설정
        this.tutorialPage = 0;
        this.isTutorialCompleted = false;
        this.password = ""; // 비밀번호 기본값 설정
        this.address = ""; // 주소 기본값 설정
    }

    // 모든 필드를 포함하는 생성자
    public User(String userId, String deviceId, double latitude, double longitude, String gu, int tutorialPage, boolean isTutorialCompleted, String password, String address) {
        this.userId = userId;
        this.deviceId = deviceId; // 생성자에서 deviceId를 설정
        this.latitude = latitude;
        this.longitude = longitude;
        this.gu = gu;
        this.tutorialPage = tutorialPage;
        this.isTutorialCompleted = isTutorialCompleted;
        this.password = password;
        this.address = address;
    }

    // Latitude와 Longitude를 double로 처리하는 생성자
    public User(String userId, String deviceId, double latitude, double longitude, String gu, String password, String address) {
        this(userId, deviceId, latitude, longitude, gu, 0, false, password, address); // 기본값 설정
    }

    public User(String userId, String password, double latitude, double longitude, String gu, String address) {
        this(userId, "", latitude, longitude, gu, 0, false, password, address); // 기본값 설정
    }

    public User(String userId, String password, double latitude, double longitude, String gu) {
        this(userId, "", latitude, longitude, gu, 0, false, password, ""); // 기본값 설정
    }

    // Getter and Setter methods
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { // Setter 추가
        this.deviceId = deviceId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGu() {
        return gu;
    }

    public void setGu(String gu) {
        this.gu = gu;
    }

    public int getTutorialPage() {
        return tutorialPage;
    }

    public void setTutorialPage(int tutorialPage) {
        this.tutorialPage = tutorialPage;
    }

    public boolean isTutorialCompleted() {
        return isTutorialCompleted;
    }

    public void setTutorialCompleted(boolean isTutorialCompleted) {
        this.isTutorialCompleted = isTutorialCompleted;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // 편리한 문자열 표현 추가 (디버깅이나 로그용)
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", gu='" + gu + '\'' +
                ", tutorialPage=" + tutorialPage +
                ", isTutorialCompleted=" + isTutorialCompleted +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
