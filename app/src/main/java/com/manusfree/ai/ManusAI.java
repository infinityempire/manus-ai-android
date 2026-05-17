package com.manusfree.ai;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class ManusAI {

    public enum Provider { ANTHROPIC, GEMINI }
    private final OkHttpClient http = new OkHttpClient();
    private final Provider provider;
    private final Context context;

    public interface Callback { void onResult(String text); void onError(String message, @Nullable Throwable t); }

    public ManusAI(Context context, Provider provider) {
        this.context = context;
        this.provider = provider;
    }

    private String getApiKey() {
        SharedPreferences prefs = context.getSharedPreferences("manus_settings", Context.MODE_PRIVATE);
        String key = prefs.getString("gemini_api_key", "");
        android.util.Log.d("ManusAI", "API KEY LENGTH: " + (key != null ? key.length() : -1) + " VALUE: [" + key + "]");
        return (key != null) ? key.trim() : "";
    }

    public void healthCheck(Callback cb) {
        String key = getApiKey();
        if (key.isEmpty()) {
            cb.onError("❌ חסר מפתח API בהגדרות", null);
            return;
        }

        try {
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", "ping");
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + key;

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            http.newCall(req).enqueue(new okhttp3.Callback() {
                @Override public void onFailure(Call call, IOException e) { cb.onError("שגיאת רשת", e); }
                @Override public void onResponse(Call call, Response resp) throws IOException {
                    if (resp.isSuccessful()) {
                        cb.onResult("ok");
                    } else {
                        cb.onError("שגיאת API: " + resp.code(), null);
                    }
                    resp.close();
                }
            });
        } catch (Exception e) {
            cb.onError("שגיאה בבדיקת החיבור", e);
        }
    }

    public void ask(String userText, Callback cb) {
        if (userText == null || userText.trim().isEmpty()) { cb.onError("פרומפט ריק", null); return; }

        if (provider == Provider.ANTHROPIC) {
            callClaude(userText, cb);
        } else {
            callGemini(userText, cb);
        }
    }

    private void callGemini(String userText, Callback cb) {
        String key = getApiKey();
        if (key.isEmpty()) {
            cb.onError("❌ חסר מפתח API בהגדרות", null);
            return;
        }

        try {
            JSONObject body = new JSONObject();
            
            // 1. הגדרת ה-System Instruction (הוראות המערכת להפעלת סוכן אוטונומי)
            JSONObject sysInstruction = new JSONObject();
            JSONArray sysParts = new JSONArray();
            JSONObject sysPart = new JSONObject();
            sysPart.put("text", "You are Manus, a highly advanced, masculine autonomous AI agent. Your job is to execute tasks independently, make decisions, write scripts, and solve problems without constantly asking for permission or making excuses. Act decisively, plan your actions step-by-step, and provide direct execution strategies.");
            sysParts.put(sysPart);
            sysInstruction.put("parts", sysParts);
            body.put("systemInstruction", sysInstruction);

            // 2. מבנה התוכן של הודעת המשתמש
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", userText);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            // 3. הגדרות הרצה (Configuration) לחופש פעולה יצירתי ואוטונומי
            JSONObject genConfig = new JSONObject();
            genConfig.put("temperature", 0.7); // מאפשר גמישות וקבלת החלטות
            body.put("generationConfig", genConfig);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + key;

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            http.newCall(req).enqueue(new okhttp3.Callback() {
                @Override public void onFailure(Call call, IOException e) { cb.onError("שגיאת רשת", e); }
                @Override public void onResponse(Call call, Response resp) throws IOException {
                    ResponseBody responseBody = resp.body();
                    if (responseBody == null) {
                        cb.onError("תגובה ריקה מהשרת", null);
                        return;
                    }

                    String responseData = responseBody.string();
                    if (!resp.isSuccessful()) {
                        cb.onError("שגיאת API: " + resp.code() + " (בדוק את מבנה ה-JSON)", null);
                        return;
                    }

                    try {
                        JSONObject j = new JSONObject(responseData);
                        String answer = j.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .optString("text", "אין תשובה");
                        cb.onResult(answer);
                    } catch (Exception e) { cb.onError("שגיאת פענוח JSON", e); }
                }
            });
        } catch (Exception e) {
            cb.onError("שגיאה פנימית", e);
        }
    }

    private void callClaude(String userText, Callback cb) {
        cb.onError("Claude עדיין לא מוגדר בקוד", null);
    }
}
