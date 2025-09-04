package com.manusfree.ai;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebSearchTool {
    
    private static final String TAG = "WebSearchTool";
    private static final String DUCKDUCKGO_API = "https://api.duckduckgo.com/";
    
    // Note: This method should only be called from background thread (via ExecutorService in ManusAI)
    public String search(String query) {
        try {
            Log.d(TAG, "Searching for: " + query);
            
            // קידוד השאילתה
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlString = DUCKDUCKGO_API + "?q=" + encodedQuery + "&format=json&no_html=1&skip_disambig=1";
            
            // ביצוע החיפוש
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Manus-Free-Android/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return parseSearchResults(response.toString(), query);
            } else {
                return "שגיאה בחיפוש: קוד תגובה " + responseCode;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Search error", e);
            return "שגיאה בחיפוש: " + e.getMessage();
        }
    }
    
    private String parseSearchResults(String jsonResponse, String originalQuery) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder();
            
            result.append("🔍 תוצאות חיפוש עבור: \"").append(originalQuery).append("\"\n\n");
            
            // תשובה מיידית
            String instantAnswer = json.optString("Answer", "");
            if (!instantAnswer.isEmpty()) {
                result.append("💡 תשובה מיידית:\n").append(instantAnswer).append("\n\n");
            }
            
            // הגדרה
            String definition = json.optString("Definition", "");
            if (!definition.isEmpty()) {
                result.append("📖 הגדרה:\n").append(definition).append("\n\n");
            }
            
            // תשובה מופשטת
            String abstractText = json.optString("Abstract", "");
            if (!abstractText.isEmpty()) {
                result.append("📄 מידע כללי:\n").append(abstractText).append("\n\n");
            }
            
            // נושאים קשורים
            JSONArray relatedTopics = json.optJSONArray("RelatedTopics");
            if (relatedTopics != null && relatedTopics.length() > 0) {
                result.append("🔗 נושאים קשורים:\n");
                for (int i = 0; i < Math.min(3, relatedTopics.length()); i++) {
                    JSONObject topic = relatedTopics.getJSONObject(i);
                    String text = topic.optString("Text", "");
                    if (!text.isEmpty()) {
                        result.append("• ").append(text).append("\n");
                    }
                }
                result.append("\n");
            }
            
            // אם אין תוצאות מועילות
            if (result.length() <= 50) {
                result.append("לא נמצאו תוצאות מפורטות עבור השאילתה.\n");
                result.append("נסה לנסח את השאלה בצורה אחרת או להיות יותר ספציפי.");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing search results", e);
            return "שגיאה בעיבוד תוצאות החיפוש: " + e.getMessage();
        }
    }
}

