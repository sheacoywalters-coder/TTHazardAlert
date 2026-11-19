package com.example.tthazardalert;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

// Timer logic modelled on the lecturer's TimerDemo.
public class SendAlertActivity extends AppCompatActivity {

    private EditText secondsInput, messageInput;
    private Spinner regionSpinner, typeSpinner, severitySpinner;
    private TextView timerText, statusText;
    private CountDownTimer timer;
    private ToneGenerator toneGenerator;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_alert);

        secondsInput  = findViewById(R.id.secondsInput);
        messageInput  = findViewById(R.id.messageInput);
        regionSpinner = findViewById(R.id.regionSpinner);
        typeSpinner   = findViewById(R.id.typeSpinner);
        timerText     = findViewById(R.id.timerText);
        statusText    = findViewById(R.id.statusText);
        severitySpinner = findViewById(R.id.severitySpinner);

        // ToneGenerator produces a sound without needing an audio file.
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        DataStore.get().ensureLoaded(this);
        updateDisplay(0);
    }

    public void sendAlert(View view) {
        if (running) return;

        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SEND_SMS permission not granted", Toast.LENGTH_LONG).show();
            return;
        }

        long seconds;
        try {
            seconds = Long.parseLong(secondsInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid number of seconds", Toast.LENGTH_SHORT).show();
            return;
        }
        if (seconds < 1 || seconds > 3600) {
            Toast.makeText(this, "Enter 1 to 3600 seconds", Toast.LENGTH_SHORT).show();
            return;
        }

        final String region = regionSpinner.getSelectedItem().toString();
        String type = typeSpinner.getSelectedItem().toString();
        String severity = severitySpinner.getSelectedItem().toString();
        String msg = messageInput.getText().toString().trim();
        if (msg.isEmpty()) {
            Toast.makeText(this, "Type an alert message", Toast.LENGTH_SHORT).show();
            return;
        }

        DataStore ds = DataStore.get();
        ds.alerts.add(new Alert(region, type, severity, msg));

        int sent = 0;
        for (Subscriber s : ds.subscribers) {
            if (s.getRegion().equalsIgnoreCase(region)) {
                s.setSafe(false);
                SmsHelper.send(this, s.getPhone(),
                        "ALERT (" + severity + " " + type + ") " + region + ": " + msg + " Reply SAFE.");
                sent++;
            }
        }
        ds.save(this);

        if (sent == 0) {
            Toast.makeText(this, "No subscribers in " + region, Toast.LENGTH_LONG).show();
        }

        running = true;
        statusText.setText("Alert sent to " + sent + ". Check-in open...");

        timer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateDisplay(millisUntilFinished);
                statusText.setText(countSafe(region) + " of " + countRegion(region) + " safe");
            }

            @Override
            public void onFinish() {
                timer = null;
                running = false;
                updateDisplay(0);
                statusText.setText("CHECK-IN CLOSED. Alarm sounding.");
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500);

                for (Subscriber s : DataStore.get().subscribers) {
                    if (s.getRegion().equalsIgnoreCase(region) && !s.isSafe()) {
                        SmsHelper.send(SendAlertActivity.this, s.getPhone(),
                                "No check-in received. Reply SAFE or call 999.");
                    }
                }
            }
        }.start();
    }

    public void stopTimer(View view) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        toneGenerator.stopTone();
        statusText.setText("Stopped");
    }

    public void goBack(View view) { finish(); }

    private int countRegion(String region) {
        int n = 0;
        for (Subscriber s : DataStore.get().subscribers)
            if (s.getRegion().equalsIgnoreCase(region)) n++;
        return n;
    }

    private int countSafe(String region) {
        int n = 0;
        for (Subscriber s : DataStore.get().subscribers)
            if (s.getRegion().equalsIgnoreCase(region) && s.isSafe()) n++;
        return n;
    }

    private void updateDisplay(long millis) {
        long totalSeconds = (millis + 999) / 1000;
        timerText.setText(String.format(Locale.US, "%02d:%02d",
                totalSeconds / 60, totalSeconds % 60));
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        if (toneGenerator != null) {
            toneGenerator.stopTone();
            toneGenerator.release();
        }
        super.onDestroy();
    }
}
