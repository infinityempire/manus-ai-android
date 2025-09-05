package com.manusfree.ai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ManusAI manusAI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        initializeAI();
        
        // הודעת ברוכים הבאים
        addMessage("שלום! אני Manus-Free, העוזר הדיגיטלי שלך. איך אוכל לעזור לך היום?", false);
    }
    
    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        
        // בדיקת null כדי למנוע קריסות
        if (chatRecyclerView == null || messageEditText == null || sendButton == null) {
            Toast.makeText(this, "שגיאה בטעינת הממשק", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }
    
    private void setupClickListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        
        // שליחה עם Enter
        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                sendMessage();
                return true;
            }
        });
    }
    
    private void initializeAI() {
        try {
            // יצירת AI אמיתי עם OpenAI
            manusAI = new ManusAI(this, ManusAI.Provider.OPENAI);
            
            // בדיקת בריאות מיידית
            manusAI.healthCheck(new ManusAI.Callback() {
                @Override
                public void onResult(String ok) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addMessage("✅ מחובר למנוע AI (Ready)", false);
                        }
                    });
                }
                
                @Override
                public void onError(String msg, Throwable t) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addMessage("⚠️ " + msg + " — תקן מפתח/אינטרנט לפני שימוש", false);
                        }
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing AI", e);
            addMessage("⚠️ שגיאה באתחול AI: " + e.getMessage(), false);
        }
    }
    
    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // הוספת הודעת המשתמש
        addMessage(message, true);
        messageEditText.setText("");
        
        // בדיקה אם AI זמין
        if (manusAI == null) {
            addMessage("המערכת לא זמינה כרגע. אנא נסה שוב מאוחר יותר.", false);
            return;
        }
        
        // הצגת אינדיקטור טעינה
        addMessage("מעבד...", false);
        
        // שליחה ל-AI אמיתי
        try {
            manusAI.ask(message, new ManusAI.Callback() {
                @Override
                public void onResult(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // הסרת אינדיקטור הטעינה
                            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                                chatMessages.remove(chatMessages.size() - 1);
                                chatAdapter.notifyItemRemoved(chatMessages.size());
                            }
                            
                            // הוספת תשובת ה-AI האמיתי
                            addMessage(response, false);
                        }
                    });
                }
                
                @Override
                public void onError(String error, Throwable t) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // הסרת אינדיקטור הטעינה
                            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                                chatMessages.remove(chatMessages.size() - 1);
                                chatAdapter.notifyItemRemoved(chatMessages.size());
                            }
                            
                            addMessage("⚠️ " + error, false);
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error sending message", e);
            // הסרת אינדיקטור הטעינה
            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                chatMessages.remove(chatMessages.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessages.size());
            }
            addMessage("⚠️ שגיאה בשליחת ההודעה: " + e.getMessage(), false);
        }
    }
    
    private void addMessage(String message, boolean isUser) {
        try {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            
            if (chatMessages == null || chatAdapter == null || chatRecyclerView == null) {
                Log.e("MainActivity", "Chat components not initialized");
                return;
            }
            
            ChatMessage chatMessage = new ChatMessage(message, isUser, System.currentTimeMillis());
            chatMessages.add(chatMessage);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        } catch (Exception e) {
            Log.e("MainActivity", "Error adding message", e);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ManusAI cleanup handled automatically
    }
}

