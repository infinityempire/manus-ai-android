package com.manusfree.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class ManusAI {

    // Remove OpenAI provider.  The app now supports only Anthropic (Claude) and Gemini.
    public enum Provider { ANTHROPIC, GEMINI }
    private static final String TAG = "ManusAI";

    private final OkHttpClient http = new OkHttpClient();
    private final Provider provider;
    private final Context context;

    public interface Callback { void onResult(String text); void onError(String message, @Nullable Throwable t); }

    public ManusAI(Context context, Provider provider) { 
        this.context = context;
        this.provider = provider; 
    }

    /**
     * Retrieve the persistent system instruction stored in SharedPreferences.
     * If none is saved, returns an empty string.  This instruction allows
     * customizing the assistant's persona on a per-user basis.
     */
    private String getSystemInstruction() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("manus_settings", Context.MODE_PRIVATE);
            String sys = prefs.getString("system_instruction", "");
            return (sys != null) ? sys : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Append a line to the application log file.  This method writes to
     * internal storage and is useful for debugging API requests and responses.
     * Errors during logging are silently ignored.
     *
     * @param data the line to append
     */
    private void logToFile(String data) {
        try {
            java.io.File dir = context.getExternalFilesDir(null);
            if (dir == null) {
                dir = context.getFilesDir();
            }
            java.io.File logFile = new java.io.File(dir, "manus_ai_log.txt");
            java.io.FileWriter fw = new java.io.FileWriter(logFile, true);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            String timestamp = sdf.format(new java.util.Date());
            fw.write(timestamp + " " + data + "\n");
            fw.close();
        } catch (Exception e) {
            // Ignore logging errors
        }
    }
    
    private String getApiKey(String keyType) {
        SharedPreferences prefs = context.getSharedPreferences("manus_settings", Context.MODE_PRIVATE);
        String savedKey = prefs.getString(keyType, "");
        if (savedKey != null && !savedKey.isEmpty()) {
            return savedKey;
        }
        
        // Fallback to BuildConfig if SharedPreferences is empty.  We only support
        // Anthropic and Gemini keys; OpenAI keys are no longer used.
        if ("anthropic_api_key".equals(keyType)) {
            return BuildConfig.ANTHROPIC_API_KEY;
        }
        if ("gemini_api_key".equals(keyType)) {
            return BuildConfig.GEMINI_API_KEY;
        }
        return "";
    }

    public void ask(String userText, Callback cb) {
        if (userText == null || userText.trim().isEmpty()) { cb.onError("Empty prompt", null); return; }
        try {
            // Route calls based on provider.  When provider is GEMINI or unspecified,
            // default to Gemini.  OpenAI is no longer supported.
            if (provider == Provider.ANTHROPIC) {
                callClaude(userText, cb);
            } else {
                // Provider.GEMINI or any other value falls back to Gemini.
                callGemini(userText, cb);
            }
        } catch (Exception e) {
            cb.onError("Client error", e);
        }
    }

    public void healthCheck(Callback cb) {
        try {
            if (provider == Provider.GEMINI) {
                String key = getApiKey("gemini_api_key");
                if (key == null || key.isEmpty()) {
                    cb.onError("❌ No Gemini API key found.", null);
                    return;
                }
                try {
                    // Build a minimal request body to validate the key.  We send a single
                    // 'ping' message to the API.  If the API key is valid and the
                    // endpoint is reachable, the request will succeed; otherwise the
                    // response code will indicate an error (401/403/404).
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
                    Request req = new Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + key)
                            .addHeader("Content-Type", "application/json")
                            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                            .build();

                    // Log the health check request
                    logToFile("Gemini HEALTH REQUEST: " + body.toString());
                    http.newCall(req).enqueue(new okhttp3.Callback() {
                        @Override public void onFailure(Call call, IOException e) {
                            // Log failure
                            logToFile("Gemini HEALTH ERROR: " + e.getMessage());
                            cb.onError("Network error", e);
                        }
                        @Override public void onResponse(Call call, Response resp) throws IOException {
                            if (!resp.isSuccessful()) {
                                // Log error response
                                logToFile("Gemini HEALTH RESPONSE CODE: " + resp.code());
                                cb.onError("HTTP " + resp.code(), null);
                                return;
                            }
                            // Log success
                            logToFile("Gemini HEALTH RESPONSE: OK");
                            cb.onResult("OK");
                        }
                    });
                } catch (Exception e) {
                    cb.onError("Client error", e);
                }
                return;
            } else if (provider == Provider.ANTHROPIC) {
                // For Anthropic, simply verify that the API key is present.  We could also
                // perform a ping request to the Anthropic API, but returning OK when the key exists
                // suffices for now.
                String key = BuildConfig.ANTHROPIC_API_KEY;
                if (key == null || key.isEmpty()) {
                    cb.onError("Missing ANTHROPIC_API_KEY", null);
                    return;
                }
                cb.onResult("OK");
            }
        } catch (Exception e) { cb.onError("Client error", e); }
    }

    private void callOpenAI(String userText, Callback cb) throws Exception {
        String key = getApiKey("openai_api_key");
        if (key == null || key.isEmpty()) { cb.onError("❌ No OpenAI API key found. Please add one in Settings.", null); return; }

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4o-mini"); // עדכן למודל שבחשבון שלך
        JSONArray messages = new JSONArray();
        // Insert custom system instruction, if any; otherwise use default.
        String systemInstr = getSystemInstruction();
        if (systemInstr != null && !systemInstr.trim().isEmpty()) {
            messages.put(new JSONObject().put("role", "system").put("content", systemInstr));
        } else {
            messages.put(new JSONObject().put("role","system").put("content","You are Manus. Answer in Hebrew if user speaks Hebrew. Be grounded."));
        }
        // User message
        messages.put(new JSONObject().put("role","user").put("content", userText));
        body.put("messages", messages);
        body.put("temperature", 0.3);

        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + key)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        // Log the request body
        logToFile("OpenAI REQUEST: " + body.toString());

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError("Network error", e); }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) { cb.onError("HTTP " + resp.code(), null); return; }
                String json = resp.body().string();
                // Log the raw response
                logToFile("OpenAI RESPONSE: " + json);
                try {
                    JSONObject j = new JSONObject(json);
                    String answer = j.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content", "");
                    if (answer.isEmpty()) answer = "(אין תשובה מהמודל)";
                    cb.onResult(answer);
                } catch (Exception e) { cb.onError("Parse error", e); }
            }
        });
    }

    private void callGemini(String userText, Callback cb) throws Exception {
        String key = getApiKey("gemini_api_key");
        if (key == null || key.isEmpty()) { cb.onError("❌ No Gemini API key found.", null); return; }

        JSONObject body = new JSONObject();
        JSONArray contents = new JSONArray();
        // Include a system instruction if one was configured by the user.  The
        // system instruction is added as the first content entry so that the
        // model treats it as a role/identity specification.
        String systemInstr = getSystemInstruction();
        if (systemInstr != null && !systemInstr.trim().isEmpty()) {
            JSONObject sysContent = new JSONObject();
            JSONArray sysParts = new JSONArray();
            JSONObject sysPart = new JSONObject();
            sysPart.put("text", systemInstr);
            sysParts.put(sysPart);
            sysContent.put("parts", sysParts);
            contents.put(sysContent);
        }
        // Add the user message
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", userText);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        body.put("contents", contents);

        //
        // As of mid‑2026 the v1beta API no longer serves the legacy
        // `gemini-1.5-flash` model and returns HTTP 404.  Use the more
        // widely available `gemini-pro` model instead.  See the official
        // Gemini API documentation for a complete list of supported
        // model names.  The endpoint format is:
        //   https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={API_KEY}
        //
        // By switching to `gemini-pro` we avoid the 404 errors users
        // reported when calling the obsolete model.
        Request req = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + key)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        // Log the request body
        logToFile("Gemini REQUEST: " + body.toString());

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError("Network error", e); }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) { cb.onError("HTTP " + resp.code(), null); return; }
                String json = resp.body().string();
                // Log the raw response
                logToFile("Gemini RESPONSE: " + json);
                try {
                    JSONObject j = new JSONObject(json);
                    String answer = j.getJSONArray("candidates").getJSONObject(0)
                            .getJSONObject("content").getJSONArray("parts").getJSONObject(0).optString("text", "");
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

        // Log the request body
        logToFile("Claude REQUEST: " + body.toString());

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onError("Network error", e); }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) { cb.onError("HTTP " + resp.code(), null); return; }
                String json = resp.body().string();
                // Log the raw response
                logToFile("Claude RESPONSE: " + json);
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
