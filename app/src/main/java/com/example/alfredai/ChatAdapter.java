package com.example.alfredai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatMessage> messages;
    private Set<Integer> animatedPositions = new HashSet<>();
    private boolean shouldAnimate = true;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void updateMessages(List<ChatMessage> newMessages) {
        if (newMessages == null) {
            this.messages = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }

        int previousSize = this.messages.size();
        int newSize = newMessages.size();

        this.messages = newMessages;

        if (previousSize == 0 && newSize > 0) {
            // First time loading messages - no animation
            notifyItemRangeInserted(0, newSize);
        } else if (newSize > previousSize) {
            // New messages added - animate new items
            int insertedCount = newSize - previousSize;
            for (int i = 0; i < insertedCount; i++) {
                int position = previousSize + i;
                notifyItemInserted(position);
            }
        } else {
            // Messages updated or removed
            notifyDataSetChanged();
        }
    }

    // Method to disable animations (useful for initial load)
    public void disableAnimations() {
        shouldAnimate = false;
    }

    // Method to enable animations
    public void enableAnimations() {
        shouldAnimate = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 0 ? R.layout.item_user_message : R.layout.item_bot_message;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (messages != null && position < messages.size()) {
            ChatMessage message = messages.get(position);
            holder.messageText.setText(message.getText());

            // Apply animation only if it hasn't been animated yet and animations are enabled
            if (shouldAnimate && !animatedPositions.contains(position)) {
                // Add a small delay to ensure the view is properly laid out
                holder.itemView.post(() -> {
                    animateMessage(holder.itemView, message.isUser());
                });
                animatedPositions.add(position);
            } else {
                // Ensure non-animated items are fully visible
                holder.itemView.setAlpha(1f);
                holder.itemView.clearAnimation();
            }
        }
    }

    private void animateMessage(View view, boolean isUser) {
        // Clear any existing animations
        view.clearAnimation();

        // Choose animation based on message type
        int animationResource = isUser ? R.anim.righttoleft : R.anim.lefttoright;

        // Load and apply the animation
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), animationResource);

        // Add animation listener for additional effects
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Set initial state
                view.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Ensure view is fully visible after animation
                view.setAlpha(1f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not used
            }
        });

        view.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 0 : 1;
    }

    // Method to clear animation cache (useful when refreshing chat)
    public void clearAnimationCache() {
        animatedPositions.clear();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }
    }
}