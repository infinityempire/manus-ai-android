package com.manusfree.ai;

import android.util.Log;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class ManusAI {

    public enum Provider { OPENAI, ANTHROPIC }
    private static final String TAG = "ManusAI";

    private final OkHttpClient http = new OkHttpClient();
    private final Provider provider;

    public interface Callback { void onResult(String text); void onError(String message, @Nullable Throwable t); }

    public ManusAI(Provider provider) { this.provider = provider; }

    public void ask(String userText, Callback cb) {
        if (userText == null || userText.trim().isEmpty()) { cb.onError("Empty prompt", null); return; }
        try { if (provider == Provider.ANTHROPIC) callClaude(userText, cb); else callOpenAI(userText, cb); }
        catch (Exception e) { cb.onError("Client error", e); }
    }

    public void healthCheck(Callback cb) {
        try {
            if (provider == Provider.OPENAI) {
                String key = BuildConfig.OPENAI_API_KEY;
                if (key == null || key.isEmpty()) { cb.onError("Missing OPENAI_API_KEY", null); return; }
                Request req = new Request.Builder()
                        .url("https://api.openai.com/v1/models")
                        .addHeader("Authorization", "Bearer " + key)
                        .get().build();
                http.newCall(req).enqueue(new okhttp3.Callback() {
                    @Override public void onFailure(Call call, IOException e) { cb.onError("Network error", e); }
                    @Override public void onResponse(Call call, Response resp) throws IOException {
                        if (!resp.isSuccessful()) { cb.onError("HTTP " + resp.code(), null); return; }
                        cb.onResult("OK");
                    }
                });
            } else {
                String key = BuildConfig.ANTHROPIC_API_KEY;
                if (key == null || key.isEmpty()) { cb.onError("Missing ANTHROPIC_API_KEY", null); return; }
                cb.onResult("OK"); // או ping לשרת משלך
            }
        } catch (Exception e) { cb.onError("Client error", e); }
    }

    private void callOpenAI(String userText, Callback cb) throws Exception {
        String key = BuildConfig.OPENAI_API_KEY;
        if (key == null || key.isEmpty()) { cb.onError("Missing OPENAI_API_KEY", null); return; }

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4o-mini"); // עדכן למודל שבחשבון שלך
        JSONArray messages = new JSONArray()
                .put(new JSONObject().put("role","system").put("content","You are Manus. Answer in Hebrew if user speaks Hebrew. Be grounded."))
                .put(new JSONObject().put("role","user").put("content", userText));
        body.put("messages", messages);
        body.put("temperature", 0.3);

        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + key)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError("Network error", e); }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) { cb.onError("HTTP " + resp.code(), null); return; }
                String json = resp.body().string();
                try {
                    JSONObject j = new JSONObject(json);
                    String answer = j.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content", "");
                    if (answer.isEmpty()) answer = "(אין תשובה מהמודל)";
                    cb.onResult(answer);
                } catch (Exception e) { cb.onError("Parse error", e); }
            }
        });
    }

    private void callClaude(String userText, Callback cb) throws Exception {
        String key = BuildConfig.ANTHROPIC_API_KEY;
        if (key == null || key.isEmpty()) { cb.onError("Missing ANTHROPIC_API_KEY", null); return; }

        JSONObject body = new JSONObject();
        body.put("model", "claude-3-haiku-20240307"); // עדכן למודל שלך
        body.put("max_tokens", 512);
        body.put("messages", new JSONArray().put(new JSONObject().put("role","user").put("content", userText)));

        Request req = new Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", key)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError("Network error", e); }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) { cb.onError("HTTP " + resp.code(), null); return; }
                String json = resp.body().string();
                try {
                    JSONArray content = new JSONObject(json).optJSONArray("content");
                    String answer = (content != null && content.length() > 0) ? content.getJSONObject(0).optString("text","") : "";
                    if (answer.isEmpty()) answer = "(אין תשובה מהמודל)";
                    cb.onResult(answer);
                } catch (Exception e) { cb.onError("Parse error", e); }
            }
        });
    }
}

