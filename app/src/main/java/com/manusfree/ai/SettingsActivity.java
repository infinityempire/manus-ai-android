package com.manusfree.ai;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    private EditText apiKeyInput;
    private Button saveButton;
    private Button testButton;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize views
        apiKeyInput = findViewById(R.id.api_key_input);
        saveButton = findViewById(R.id.save_button);
        testButton = findViewById(R.id.test_button);
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences("manus_settings", MODE_PRIVATE);
        
        // Load existing API key
        String existingKey = prefs.getString("openai_api_key", "");
        if (!existingKey.isEmpty()) {
            // Show only last 4 characters for security
            String maskedKey = "sk-..." + existingKey.substring(Math.max(0, existingKey.length() - 4));
            apiKeyInput.setHint("Current: " + maskedKey);
        }
        
        // Save button click
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveApiKey();
            }
        });
        
        // Test button click
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testApiKey();
            }
        });
    }
    
    private void saveApiKey() {
        String apiKey = apiKeyInput.getText().toString().trim();
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!apiKey.startsWith("sk-")) {
            Toast.makeText(this, "Invalid API key format. Should start with 'sk-'", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save API key
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("openai_api_key", apiKey);
        editor.apply();
        
        Toast.makeText(this, "✅ API Key saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Clear input for security
        apiKeyInput.setText("");
        String maskedKey = "sk-..." + apiKey.substring(Math.max(0, apiKey.length() - 4));
        apiKeyInput.setHint("Current: " + maskedKey);
    }
    
    private void testApiKey() {
        String apiKey = prefs.getString("openai_api_key", "");
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "❌ No API key found. Please save one first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Simple validation
        if (apiKey.startsWith("sk-") && apiKey.length() > 20) {
            Toast.makeText(this, "✅ API Key format looks good!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ API Key format seems invalid", Toast.LENGTH_SHORT).show();
        }
    }
}

