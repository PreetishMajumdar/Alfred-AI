package com.example.alfredai;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
    private boolean isFirstLoad = true;
    private TextView topHeader;
    private ImageView settingsIcon;
    private EditText editTextMessage;
    private ImageView buttonSend;

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

        initializeViews();
        applyInitialAnimations();
        setupRecyclerView();
        setupViewModel();
        setupSendButton();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        topHeader = findViewById(R.id.top_header);
        settingsIcon = findViewById(R.id.settings_icon);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
    }

    private void applyInitialAnimations() {
        // top-to-down animation for header elements
        Animation topToDown = AnimationUtils.loadAnimation(this, R.anim.toptodown);
        topHeader.startAnimation(topToDown);
        settingsIcon.startAnimation(topToDown);

        // down-to-top animation for input elements
        Animation downToTop = AnimationUtils.loadAnimation(this, R.anim.downtotop);
        editTextMessage.startAnimation(downToTop);
        buttonSend.startAnimation(downToTop);

        // fade-in animation for recyclerView
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.downtotop);
        recyclerView.startAnimation(fadeIn);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        adapter.disableAnimations();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getMessages().observe(this, messages -> {
            if (messages != null) {
                if (isFirstLoad && !messages.isEmpty()) {
                    isFirstLoad = false;
                    recyclerView.postDelayed(() -> {
                        adapter.enableAnimations();
                    }, 100);
                }
                adapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerView.postDelayed(() -> {
                        int position = messages.size() - 1;
                        if (position >= 0 && position < adapter.getItemCount()) {
                            recyclerView.smoothScrollToPosition(position);
                        }
                    }, 150);
                }
            }
        });
    }

    private void setupSendButton() {
        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            adapter.enableAnimations();
            viewModel.sendMessage(text);
            editTextMessage.setText("");
        });

        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                buttonSend.performClick();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.enableAnimations();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.disableAnimations();
        }
    }

    public void refreshChat() {
        if (adapter != null) {
            adapter.clearAnimationCache();
            adapter.enableAnimations();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}