package com.manusfree.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    
    private List<ChatMessage> chatMessages;
    private SimpleDateFormat timeFormat;
    
    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    
    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView userMessageTextView;
        private TextView userTimeTextView;
        private TextView aiMessageTextView;
        private TextView aiTimeTextView;
        private View userMessageContainer;
        private View aiMessageContainer;
        
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageTextView = itemView.findViewById(R.id.userMessageTextView);
            userTimeTextView = itemView.findViewById(R.id.userTimeTextView);
            aiMessageTextView = itemView.findViewById(R.id.aiMessageTextView);
            aiTimeTextView = itemView.findViewById(R.id.aiTimeTextView);
            userMessageContainer = itemView.findViewById(R.id.userMessageContainer);
            aiMessageContainer = itemView.findViewById(R.id.aiMessageContainer);
        }
        
        public void bind(ChatMessage message) {
            if (message == null) {
                return;
            }
            
            try {
                String timeText = timeFormat.format(new Date(message.getTimestamp()));
                
                if (message.isUser()) {
                    userMessageContainer.setVisibility(View.VISIBLE);
                    aiMessageContainer.setVisibility(View.GONE);
                    if (userMessageTextView != null) {
                        userMessageTextView.setText(message.getMessage());
                    }
                    if (userTimeTextView != null) {
                        userTimeTextView.setText(timeText);
                    }
                } else {
                    userMessageContainer.setVisibility(View.GONE);
                    aiMessageContainer.setVisibility(View.VISIBLE);
                    if (aiMessageTextView != null) {
                        aiMessageTextView.setText(message.getMessage());
                    }
                    if (aiTimeTextView != null) {
                        aiTimeTextView.setText(timeText);
                    }
                }
            } catch (Exception e) {
                // Log error but don't crash
                android.util.Log.e("ChatAdapter", "Error binding message", e);
            }
        }
    }
}

