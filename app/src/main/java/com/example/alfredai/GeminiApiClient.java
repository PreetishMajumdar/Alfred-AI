package com.example.alfredai;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;

public class GeminiApiClient {
    private static final String TAG = "GeminiApiClient";
    // Correct Gemini API endpoint
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void sendMessage(String prompt, Callback callback) {
        // Request for Gemini API
        JsonObject requestJson = new JsonObject();

        // contents array
        JsonArray contentsArray = new JsonArray();
        JsonObject content = new JsonObject();

        // parts array
        JsonArray partsArray = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        partsArray.add(part);

        content.add("parts", partsArray);
        contentsArray.add(content);
        requestJson.add("contents", contentsArray);

        // generation config (optional but recommended)
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.9);
        generationConfig.addProperty("topK", 1);
        generationConfig.addProperty("topP", 1);
        generationConfig.addProperty("maxOutputTokens", 2048);
        requestJson.add("generationConfig", generationConfig);

        Log.d(TAG, "Request JSON: " + requestJson.toString());

        RequestBody body = RequestBody.create(requestJson.toString(), MediaType.parse("application/json"));
        HttpUrl url = HttpUrl.parse(ENDPOINT).newBuilder()
                .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respStr = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response: " + respStr);

                if (response.isSuccessful()) {
                    try {
                        JsonObject json = gson.fromJson(respStr, JsonObject.class);

                        // Gemini API response format
                        if (json.has("candidates") && json.getAsJsonArray("candidates").size() > 0) {
                            JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
                            if (candidate.has("content")) {
                                JsonObject content = candidate.getAsJsonObject("content");
                                if (content.has("parts") && content.getAsJsonArray("parts").size() > 0) {
                                    JsonObject part = content.getAsJsonArray("parts").get(0).getAsJsonObject();
                                    if (part.has("text")) {
                                        String reply = part.get("text").getAsString();
                                        callback.onSuccess(reply);
                                        return;
                                    }
                                }
                            }
                        }
                        callback.onError("No valid response content found");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse response: " + e.getMessage());
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "HTTP error: " + response.code() + " - " + response.message());
                    callback.onError("HTTP " + response.code() + ": " + response.message() + "\n" + respStr);
                }
            }
        });
    }
}