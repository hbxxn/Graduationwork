package com.example.setting;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchSensorDataTask extends AsyncTask<String, Void, JSONObject> {
    private final Listener listener;
    private static final int TIMEOUT = 20000; // 20초
    private final Context context;
    private MediaPlayer mediaPlayer;

    public interface Listener {
        void onDataFetched(JSONObject sensorData);
        void onDataFetchFailed();
    }

    public FetchSensorDataTask(Context context, Listener listener) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String deviceId = params[0];
        String urlString = "https://ict.nanum.info/sensor?deviceID=" + deviceId;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setRequestMethod("GET");

            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONArray jsonArray = new JSONArray(result.toString());
            return jsonArray.getJSONObject(0);
        } catch (Exception e) {
            Log.e("FetchSensorDataTask", "Error fetching sensor data", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e("FetchSensorDataTask", "Error closing reader", e);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(JSONObject sensorData) {
        if (sensorData != null) {
            listener.onDataFetched(sensorData);
            checkForAlarm(sensorData);
        } else {
            listener.onDataFetchFailed();
        }
    }

    private void checkForAlarm(JSONObject sensorData) {
        try {
            boolean intruderDetected = sensorData.optBoolean("intruder", false);

            if (intruderDetected && !Myapp.getInstance().isSleepStop()) {
                playAlarmSound();
            }
        } catch (Exception e) {
            Log.e("FetchSensorDataTask", "Error checking alarm condition", e);
        }
    }

    private void playAlarmSound() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, AudioManager.FLAG_ALLOW_RINGER_MODES);
            }

            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.warning); // 파일명 확인
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(false);
                mediaPlayer.setOnCompletionListener(mp -> stopAlarmSound());
            }

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e("FetchSensorDataTask", "Error playing alarm sound", e);
        }
    }


    public void stopAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
