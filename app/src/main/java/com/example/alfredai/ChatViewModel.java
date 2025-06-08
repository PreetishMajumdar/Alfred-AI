package com.example.alfredai;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final GeminiApiClient apiClient = new GeminiApiClient();

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public void sendMessage(String text) {
        // Add user message
        List<ChatMessage> current = new ArrayList<>(messages.getValue()); // Create new list
        current.add(new ChatMessage(text, true));
        messages.postValue(current);

        // Call API
        apiClient.sendMessage(text, new GeminiApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                List<ChatMessage> updated = new ArrayList<>(messages.getValue()); // Create new list
                updated.add(new ChatMessage(response, false));
                messages.postValue(updated);
            }

            @Override
            public void onError(String error) {
                List<ChatMessage> updated = new ArrayList<>(messages.getValue()); // Create new list
                updated.add(new ChatMessage("Error: " + error, false));
                messages.postValue(updated);
            }
        });
    }
}
