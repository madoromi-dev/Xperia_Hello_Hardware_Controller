package com.madoromi.android.bright;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BrightMotionPlayer {
    // Tips: Eye LED と Motor は「Action」レイヤー,
    //       Neck LED は「LED」レイヤーとして並行して実行可能です!

    private static final String TAG = "BrightMotionPlayer";

    private BrightController mController;
    private Handler mActionHandler = new Handler(Looper.getMainLooper());
    private Handler mLedHandler = new Handler(Looper.getMainLooper());

    private Map<String, JSONArray> mMotionMap = new HashMap<>();
    private Map<String, Boolean> mLoopMap = new HashMap<>();
    private Map<String, String> mLayerMap = new HashMap<>();

    private String mCurrentActionName = "None";
    private String mCurrentLedName = "None";

    public BrightMotionPlayer(BrightController controller) {
        this.mController = controller;
    }

    // モーション JSON の読み込み
    public void loadMotions(Context context, String fileName) {
        try {
            Log.d(TAG, "Motion file loading");
            InputStream is = context.getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONArray motions = root.getJSONArray("motions");
            for (int i = 0; i < motions.length(); i++) {
                JSONObject m = motions.getJSONObject(i);
                String name = m.getString("name");
                mMotionMap.put(name, m.getJSONArray("commands"));
                mLoopMap.put(name, m.optBoolean("loop", false));
                mLayerMap.put(name, m.optString("layer", "action"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Motion file loading failed: ", e);
        }
    }

    // モーションを再生
    public void play(String name) {
        Log.d(TAG, "Playing motion file: " + name);
        JSONArray commands = mMotionMap.get(name);
        String layer = mLayerMap.get(name);
        if (commands == null || layer == null) return;

        // LED レイヤーかどうか
        boolean isLedLayer = layer.equals("led");
        Handler handler = isLedLayer ? mLedHandler : mActionHandler;

        // 再生中のレイヤーを停止
        handler.removeCallbacksAndMessages(null);

        // ステータス更新
        if (isLedLayer) mCurrentLedName = name;
        else mCurrentActionName = name;

        long maxTime = 0;
        for (int i = 0; i < commands.length(); i++) {
            try {
                JSONObject cmd = commands.getJSONObject(i);
                long time = cmd.getLong("time");
                maxTime = Math.max(maxTime, time);
                handler.postDelayed(() -> executeCommand(cmd), time);
            } catch (Exception e) {
                Log.e(TAG, "Motion command getting failed: ", e);
            }
        }

        // 終了後の処理
        if (mLoopMap.getOrDefault(name, false)) {
            // ループあり：再帰呼び出し
            Log.d(TAG, "Motion '" + name + "' finished, now looping!");
            handler.postDelayed(() -> play(name), maxTime + 50);
        } else {
            // ループなし：終了後にステータスをクリア
            Log.d(TAG, "Motion '" + name + "' finished!");
            handler.postDelayed(() -> {
                if (isLedLayer) mCurrentLedName = "None";
                else mCurrentActionName = "None";
            }, maxTime + 100);
        }
    }

    // モーション内のコマンド実行
    private void executeCommand(JSONObject cmd) {
        try {
            String type = cmd.getString("type");
            if (type.equals("motor")) mController.moveMotor(cmd.getInt("id"), (float) cmd.getDouble("value"));
            else if (type.equals("eye")) mController.setEyes(cmd.getInt("left"), cmd.getInt("right"));
            else if (type.equals("neck_led")) mController.setNeckColor(cmd.getString("value"));
        } catch (Exception e) {
            Log.e(TAG, "Motion command executing failed: ", e);
        }
    }

    // 実行中のモーション名を取得
    public String getCurrentActionName() { return mCurrentActionName; }
    public String getCurrentLedName() { return mCurrentLedName; }

    // モーションをすべて停止
    public void stopAll() {
        Log.d(TAG, "All motion stopping");
        mActionHandler.removeCallbacksAndMessages(null);
        mLedHandler.removeCallbacksAndMessages(null);
        mCurrentActionName = "None";
        mCurrentLedName = "None";
    }
}
