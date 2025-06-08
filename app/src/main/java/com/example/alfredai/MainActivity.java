package com.example.alfredai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        EditText editText = findViewById(R.id.editTextMessage);
        ImageView buttonSend = findViewById(R.id.buttonSend);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getMessages().observe(this, messages -> {
            adapter.updateMessages(messages);

            // Only scroll if there are messages and after a small delay to ensure adapter is updated
            if (messages != null && !messages.isEmpty()) {
                recyclerView.post(() -> {
                    int position = messages.size() - 1;
                    if (position >= 0 && position < adapter.getItemCount()) {
                        recyclerView.smoothScrollToPosition(position);
                    }
                });
            }
        });

        buttonSend.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) return;
            viewModel.sendMessage(text);
            editText.setText("");
        });
    }
}