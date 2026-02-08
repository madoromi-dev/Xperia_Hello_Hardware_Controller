package com.madoromi.android.bright;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;
import tp.xmaihh.serialport.stick.SpecifiedStickPackageHelper;

public class BrightController extends SerialHelper {
    private static final String TAG = "BrightController";

    // --- 接続設定 ---
    private static final String PORT_PATH = "/dev/ttyHS1";
    private static final int BAUD_RATE = 115200;

    // --- モーターID ---
    public static final int MOTOR_TILT = 0; // 首の上下
    public static final int MOTOR_PAN  = 1; // 首の左右
    public static final int MOTOR_BODY = 2; // 本体の回転

    // --- 制御パラメータ ---
    private static final int STEP_MULTIPLIER = 1280;
    private static final String DEFAULT_SPEED = "1F40";

    // --- 可動域リミット (Float) ---
    public static final float LIMIT_TILT = 8.0f;   // 上下: -8.0 ~ 8.0
    public static final float LIMIT_PAN  = 10.0f;  // 左右: -10.0 ~ 10.0
    public static final float LIMIT_BODY = 24.0f;  // 回転: -24.0 ~ 24.0

    public BrightController() {
        super(PORT_PATH, BAUD_RATE);
    }

    // ハードウェア接続 (返り値: 成功/失敗)
    public boolean connect() {
        // すでにポートの準備が整っている
        if (isOpen()) {
            Log.d(TAG, "Serial port is already opened!");
            execRootCommand("stty -F " + PORT_PATH + " flush");
            return true;
        }
        // 接続開始
        try {
            Log.d(TAG, "Initializing connection...");
            // SELinux を Permissive に変更
            if (!execRootCommand("setenforce 0")) {
                Log.d(TAG, "Failed to set SELinux Setting!");
                return false;
            }
            Thread.sleep(500);
            // ポートのアクセス権限を開放
            if (!execRootCommand("chmod 777 " + PORT_PATH)) {
                Log.d(TAG, "Failed to open serial port!");
                return false;
            }
            // 純正アプリが起動していた場合は強制終了
            Log.d(TAG, "Killing stock app...");
            execRootCommand("am force-stop com.sonymobile.agt.s47627d7d7a");
            // データパケット設定 (断片化防止)
            SpecifiedStickPackageHelper stickHelper = new SpecifiedStickPackageHelper(
                    ":".getBytes(StandardCharsets.UTF_8),
                    "\r\n".getBytes(StandardCharsets.UTF_8)
            );
            setStickPackageHelper(stickHelper);
            // ポートオープン
            open();
            if (isOpen()) {
                Log.d(TAG, "Serial port opened successfully.");
                // 初回接続時にハードウェアをリセット
                resetHardware();
                return true;
            } else {
                Log.e(TAG, "Failed to open serial port.");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Connection failed: ", e);
            return false;
        }
    }

    // ハードウェア切断
    public void disconnect() {
        Log.d(TAG, "Disconnecting...");
        if (isOpen()) {
            // ポートを閉じる
            close();
        }
        // SELinux セキュリティ設定を復元
        execRootCommand("setenforce 1");
        Log.d(TAG, "Disconnected and Security Restored. Bye!");
    }

    // ハードウェアの初期化 (スタンバイ状態へ)
    public void resetHardware() {
        setMotorEnabled(MOTOR_TILT, true);
        setMotorEnabled(MOTOR_PAN, true);
        setMotorEnabled(MOTOR_BODY, true);

        // 下向き(0.0)から、正面〜やや上向き(5.0)をデフォルトに
        moveMotor(MOTOR_TILT, 5.0f);
        moveMotor(MOTOR_PAN, 0f);
        moveMotor(MOTOR_BODY, 0f);

        // 消灯から白色点灯へ
        setNeckColor("FFFFFF");

        // 目を閉じた状態から全開(5, 5)へ
        setEyes(5, 5);
    }

    // モーターの有効・無効切り替え
    public void setMotorEnabled(int motorId, boolean enabled) {
        String command = String.format(":MCX%d%d\r\n", motorId, enabled ? 1 : 0);
        sendPacket(command);
    }

    // モーター制御 (ID, 回転度合)
    public void moveMotor(int motorId, float angle) {
        if (!isOpen()) return;
        // 指定された角度を可動域内に制限
        float clampedAngle = clampAngle(motorId, angle);
        // 角度をハードウェア制御値に変換
        int position = (int) (clampedAngle * STEP_MULTIPLIER);
        // コマンド生成 (2の補数表現 & 符号: 0=正, F=負)
        String sign = (position >= 0) ? "0" : "F";
        String hexPos = String.format("%04X", position & 0xFFFF);
        String command = String.format(":P2P%d%s%s%s\r\n", motorId, sign, hexPos, DEFAULT_SPEED);
        // 送信
        sendPacket(command);
    }

    // 首 LED 制御 (HEX)
    public void setNeckColor(String hexColor) {
        if (!isOpen()) return;
        String cleanHex = hexColor.replace("#", "").toUpperCase();
        if (!cleanHex.matches("[0-9A-F]{6}")) return;
        String command = ":LED2" + cleanHex + "\r\n";
        // 送信
        sendPacket(command);
    }

    // 目 LED 制御 (開眼具合 0-5 × 左右)
    public void setEyes(int leftLevel, int rightLevel) {
        if (!isOpen()) return;
        // レベル(0-5)をビットマスク(0x00-0x1F)に変換
        int leftPattern = levelToBitmask(leftLevel);
        int rightPattern = levelToBitmask(rightLevel);
        String left = String.format("%02X", leftPattern);
        String right = String.format("%02X", rightPattern);
        String command = ":LED4" + left + right + "\r\n";
        // 送信
        sendPacket(command);
    }

    // 送信
    private void sendPacket(String command) {
        if (!isOpen()) return;
        send(command.getBytes(StandardCharsets.UTF_8));
        Log.d(TAG, "TX: " + command.trim());
    }

    // 受信
    @Override
    protected void onDataReceived(ComBean comBean) {
        String data = new String(comBean.bRec, StandardCharsets.UTF_8).trim();
        Log.d(TAG, "RX: " + data);
    }

    // 開眼具合をビットマスクに変換
    private int levelToBitmask(int level) {
        if (level <= 0) return 0x00;
        if (level >= 5) return 0x1F;
        return (1 << level) - 1;
    }

    // モーター制御は可動域内での動作になるようにする
    private float clampAngle(int motorId, float value) {
        float limit = 0f;
        switch (motorId) {
            case MOTOR_TILT: limit = LIMIT_TILT; break;
            case MOTOR_PAN:  limit = LIMIT_PAN; break;
            case MOTOR_BODY: limit = LIMIT_BODY; break;
        }
        return Math.max(-limit, Math.min(limit, value));
    }

    // スーパーユーザーでのコマンド実行
    private boolean execRootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            Log.e(TAG, "Root command failed: " + command, e);
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                Log.e(TAG, "Root command finalize failed: " + command, e);
            }
        }
    }
}
