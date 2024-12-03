package com.example.setting;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressUtil {

    // 위도와 경도로 동(구) 정보를 얻는 메서드
    public static LatLng getLatLngFromAddress(Context context, String addressStr) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 위도와 경도로 구(동) 정보를 얻는 메서드
    public static String getGuFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // 구, 동, 시, 면, 리 정보를 순차적으로 확인
                String subLocality = address.getSubLocality(); // 동
                String locality = address.getLocality(); // 구
                String adminArea = address.getAdminArea(); // 시
                String subAdminArea = address.getSubAdminArea(); // 군, 면
                String thoroughfare = address.getThoroughfare(); // 리

                // 동이 null이 아니면 반환
                if (subLocality != null && !subLocality.isEmpty()) {
                    return subLocality;
                }

                // 구가 null이 아니면 반환
                if (locality != null && !locality.isEmpty()) {
                    return locality;
                }

                // 시가 null이 아니면 반환
                if (adminArea != null && !adminArea.isEmpty()) {
                    return adminArea;
                }

                // 면/군이 null이 아니면 반환
                if (subAdminArea != null && !subAdminArea.isEmpty()) {
                    return subAdminArea;
                }

                // 리가 null이 아니면 반환
                if (thoroughfare != null && !thoroughfare.isEmpty()) {
                    return thoroughfare;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "N/A";
    }


    // 주소 문자열에서 동 단위 정보를 추출하는 메서드
    public static String extractDong(String data) {
        // 정규 표현식 예제 (동 단위 주소를 추출하기 위한 패턴)
        String pattern = "([가-힣]+동)";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(data);

        // 동 단위 정보 추출
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "동 정보 없음";
        }
    }

    // LatLng 클래스 정의
    public static class LatLng {
        public double latitude;
        public double longitude;

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
