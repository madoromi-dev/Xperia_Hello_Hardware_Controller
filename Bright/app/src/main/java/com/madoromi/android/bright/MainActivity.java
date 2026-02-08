package com.madoromi.android.bright;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private BrightController mController;
    private BrightMotionPlayer mMotionPlayer;

    private TextView mStatusText, mMotorValueText, mLeftEyeValueText, mRightEyeValueText;
    private RadioGroup mMotorRadioGroup;
    private SeekBar mMotorSeekBar, mLeftEyeSeekBar, mRightEyeSeekBar;

    private int mCurrentMotorId = BrightController.MOTOR_TILT;
    private int mLeftEyeLevel = 0, mRightEyeLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mController = new BrightController();
        mMotionPlayer = new BrightMotionPlayer(mController);
        mMotionPlayer.loadMotions(this, "motions.json");

        initViews();
        setupListeners();
        resetUI();
    }

    private void initViews() {
        mStatusText = findViewById(R.id.statusText);
        mMotorValueText = findViewById(R.id.motorValueText);
        mLeftEyeValueText = findViewById(R.id.leftEyeText);
        mRightEyeValueText = findViewById(R.id.rightEyeText);
        mMotorRadioGroup = findViewById(R.id.motorRadioGroup);
        mMotorSeekBar = findViewById(R.id.motorSeekBar);
        mLeftEyeSeekBar = findViewById(R.id.leftEyeSeekBar);
        mRightEyeSeekBar = findViewById(R.id.rightEyeSeekBar);
    }

    private void setupListeners() {
        findViewById(R.id.btnConnect).setOnClickListener(v -> connect());
        findViewById(R.id.btnDisconnect).setOnClickListener(v -> disconnect());

        mMotorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioTilt) mCurrentMotorId = BrightController.MOTOR_TILT;
            else if (checkedId == R.id.radioPan) mCurrentMotorId = BrightController.MOTOR_PAN;
            else if (checkedId == R.id.radioBody) mCurrentMotorId = BrightController.MOTOR_BODY;
            updateMotorSeekBarRange(mCurrentMotorId);
        });

        mMotorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                float value = (float) (progress - (seekBar.getMax() / 2));
                mMotorValueText.setText(String.format("Value: %.1f", value));
                if (checkConnected()) mController.moveMotor(mCurrentMotorId, value);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {} @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        mLeftEyeSeekBar.setOnSeekBarChangeListener(new EyeListener(true));
        mRightEyeSeekBar.setOnSeekBarChangeListener(new EyeListener(false));

        // LED Buttons
        findViewById(R.id.btnLedRed).setOnClickListener(v -> { if(checkConnected()) mController.setNeckColor("FF0000"); });
        findViewById(R.id.btnLedGreen).setOnClickListener(v -> { if(checkConnected()) mController.setNeckColor("00FF00"); });
        findViewById(R.id.btnLedBlue).setOnClickListener(v -> { if(checkConnected()) mController.setNeckColor("0000FF"); });
        findViewById(R.id.btnLedWhite).setOnClickListener(v -> { if(checkConnected()) mController.setNeckColor("FFFFFF"); });
        findViewById(R.id.btnLedOff).setOnClickListener(v -> { if(checkConnected()) mController.setNeckColor("000000"); });

        // Motion Buttons
        findViewById(R.id.btnWink).setOnClickListener(v -> playMotion("wink"));
        findViewById(R.id.btnBlink).setOnClickListener(v -> playMotion("blink"));
        findViewById(R.id.btnListen).setOnClickListener(v -> playMotion("listening"));

        findViewById(R.id.btnStatus).setOnClickListener(v -> {
            String status = "Action: " + mMotionPlayer.getCurrentActionName() +
                    "\nLED: " + mMotionPlayer.getCurrentLedName();
            android.widget.Toast.makeText(this, status, android.widget.Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnStop).setOnClickListener(v -> mMotionPlayer.stopAll());

        findViewById(R.id.btnReset).setOnClickListener(v -> {
            if(checkConnected()) {
                mMotionPlayer.stopAll();
                mController.resetHardware();
                resetUI();
            }
        });
    }

    private void playMotion(String name) {
        if (checkConnected()) mMotionPlayer.play(name);
    }

    private void connect() {
        mStatusText.setText("STATUS: CONNECTING...");
        mStatusText.setTextColor(Color.YELLOW);
        new Thread(() -> {
            boolean success = mController.connect();
            runOnUiThread(() -> {
                mStatusText.setText(success ? "STATUS: CONNECTED" : "STATUS: FAILED");
                mStatusText.setTextColor(success ? Color.GREEN : Color.RED);
                if (success) resetUI();
            });
        }).start();
    }

    private void disconnect() {
        mController.disconnect();
        mStatusText.setText("STATUS: DISCONNECTED");
        mStatusText.setTextColor(Color.RED);
        resetUI();
    }

    private boolean checkConnected() { return mController != null && mController.isOpen(); }

    private void resetUI() {
        // モーターのデフォルト設定 (Tiltを選択状態にし、値を5.0に合わせる)
        mCurrentMotorId = BrightController.MOTOR_TILT;
        mMotorRadioGroup.check(R.id.radioTilt);
        updateMotorSeekBarRange(mCurrentMotorId);

        // Tiltの初期位置を 5.0f に設定 (レンジの中央はlimit分なので、limit + 5.0)
        float limitTilt = BrightController.LIMIT_TILT; // 8.0
        mMotorSeekBar.setProgress((int)(5.0f + limitTilt));
        mMotorValueText.setText("Value: 5.0");

        // 目のインジケータを 5 (全開) に設定
        mLeftEyeLevel = 5;
        mRightEyeLevel = 5;
        mLeftEyeSeekBar.setProgress(5);
        mRightEyeSeekBar.setProgress(5);
        updateEyeText();
    }

    private void updateMotorSeekBarRange(int motorId) {
        float limit = (motorId == 0) ? 8f : (motorId == 1) ? 10f : 24f;
        mMotorSeekBar.setMax((int)(limit * 2));
        mMotorSeekBar.setProgress((int)limit);
        mMotorValueText.setText("Value: 0.0");
    }

    private void updateEyeText() {
        mLeftEyeValueText.setText("Left: " + mLeftEyeLevel);
        mRightEyeValueText.setText("Right: " + mRightEyeLevel);
    }

    private class EyeListener implements SeekBar.OnSeekBarChangeListener {
        private boolean isLeft;
        EyeListener(boolean left) { this.isLeft = left; }
        @Override
        public void onProgressChanged(SeekBar s, int p, boolean u) {
            if (isLeft) mLeftEyeLevel = p; else mRightEyeLevel = p;
            updateEyeText();
            if (u && checkConnected()) mController.setEyes(mLeftEyeLevel, mRightEyeLevel);
        }
        @Override public void onStartTrackingTouch(SeekBar s) {} @Override public void onStopTrackingTouch(SeekBar s) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMotionPlayer != null) mMotionPlayer.stopAll();
        if (mController != null) mController.disconnect();
    }
}
