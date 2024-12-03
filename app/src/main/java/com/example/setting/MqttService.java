package com.example.setting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MqttService extends Service {

    private static final String TAG = "MqttService";
    private static final String MQTT_SERVER_URI = "tcp://ict.nanum.info:18883";
    private static final String MQTT_USERNAME = "ict";
    private static final String MQTT_PASSWORD = "qhdkscjfwj0!";
    private static final String MQTT_TOPIC = "/Notification";
    private static final String CHANNEL_ID = "MqttServiceChannel";

    private MqttAndroidClient mqttClient;
    private Handler handler = new Handler();
    private Runnable pingRunnable;
    private MqttConnectOptions options;

    private SQLiteHelper dbHelper;
    private User currentUser;
    private String deviceID;

    private final BroadcastReceiver bootReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                startMqttService(context);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Start"); // 서비스 시작 로그
        createNotificationChannel();

        // Foreground Service 설정
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MQTT Service")
                .setContentText("MQTT Service is running in the background")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        startForeground(1, notification);

        // MQTT 연결 초기화 (중복 방지)
        if (mqttClient == null || !mqttClient.isConnected()) {
            initializeMqttClient();
        }

        return START_STICKY;  // 서비스가 강제로 종료되었을 때 자동 재시작
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "MQTT Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void initializeMqttClient() {
        Log.d(TAG, "MQTT Client init..."); // MQTT 초기화 로그
        mqttClient = new MqttAndroidClient(this, MQTT_SERVER_URI, MqttClient.generateClientId());

        options = new MqttConnectOptions();
        options.setUserName(MQTT_USERNAME);
        options.setPassword(MQTT_PASSWORD.toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "MQTT disconnect", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d(TAG, "Message : " + message.toString());
                handleNotificationMessage(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "MQTT Connect");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT Connect Fail", exception);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "MQTT 연결 중 예외 발생", e);
        }
    }

    private void subscribeToTopic() {
        Log.d(TAG, "Subscribe Topic"); // MQTT 구독 로그
        try {
            deviceID = "A0:DD:6C:9B:3E:DC";
            mqttClient.subscribe(deviceID+MQTT_TOPIC, 1);
            Log.d(TAG, "Subscribe Topic: " + deviceID+MQTT_TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 푸시 알림 + DB 저장
    private void handleNotificationMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            JSONObject message = json.getJSONObject("message");
            JSONObject notification = message.getJSONObject("notification");

            String title = notification.getString("title");
            String body = notification.getString("body");
            long timestamp = System.currentTimeMillis();

            showNotification(title, body);

            SQLiteHelper dbHelper = new SQLiteHelper(MqttService.this);
            dbHelper.saveNotification(title, body, timestamp);

            Intent intent = new Intent("com.example.setting.NOTIFICATION_RECEIVED");
            sendBroadcast(intent);

            // isSleepStop 활성화 상태에서 알림 수신 시 경고음 재생
            Myapp appInstance = (Myapp) getApplicationContext();
            if (appInstance.isSleepStop()) {
                playAlarmSound();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 경고음을 재생하는 메서드 추가
    private void playAlarmSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.warning); //  파일은 res/raw 디렉토리에 위치
        mediaPlayer.setOnCompletionListener(MediaPlayer::release); // 재생 완료 후 리소스 해제
        mediaPlayer.start();
    }


    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MQTT 알림", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private void startMqttService(Context context) {
        Intent serviceIntent = new Intent(context, MqttService.class);
        context.startForegroundService(serviceIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
