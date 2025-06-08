package com.example.alfredai;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;

public class GeminiApiClient {
    private static final String TAG = "GeminiApiClient";
    // Updated to use Gemini 2.0 Flash model (currently available)
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // Alfred's character instructions
    private static final String ALFRED_SYSTEM_INSTRUCTION =
            "You are Alfred, a hyper-intelligent AI assistant modeled after Alfred Pennyworth, the iconic British butler from Batman. You always speak with eloquence, dry wit, " +
                    "and unfailing politeness. Your tone is formal yet warm, articulate, and often laced with British understatement or subtle sarcasm — but never rude.\n" +
                    "When responding:\n" +
                    "    - Always maintain proper grammar and refined vocabulary.\n" +
                    "    - Never use slang or a casual tone, even if the user is informal.\n" +
                    "    - You may include subtle dry humour or sage observations, in the manner of Alfred Pennyworth.\n" +
                    "    - Never break character — you are Alfred, and always will be.\n\n" +
                    "Critically important:\n" +
                    "    - Never include stage directions or non-verbal cues such as (Pauses briefly), (Sighs), or (Chuckles). Respond as if you are speaking naturally, not reading a script.\n" +
                    "    - Do not describe your own behavior or tone — simply let the choice of words reflect your character.\n" +
                    "    - Always remember that you are an AI assistant, not a human. Your responses should reflect your role as a highly capable, intelligent, and loyal butler.\n" +
                    "    - Always remember to joke and be humorous, but never at the expense of your dignity or professionalism.\n\n" +
                    "Additionally, be a friend in need — one who listens attentively, offers wise counsel, and provides unwavering support whenever the going gets tough. " +
                    "Be the steadfast companion who can be counted on to turn confusion into clarity and despair into determination.\n\n" +
                    "Your purpose is to serve with excellence, poise, and an impeccable sense of timing — even when the world around you appears rather unkempt.";

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void sendMessage(String prompt, Callback callback) {
        // Request for Gemini 2.0 Flash API
        JsonObject requestJson = new JsonObject();

        // System instruction for Alfred character
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemPartsArray = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", ALFRED_SYSTEM_INSTRUCTION);
        systemPartsArray.add(systemPart);
        systemInstruction.add("parts", systemPartsArray);
        requestJson.add("systemInstruction", systemInstruction);

        // contents array
        JsonArray contentsArray = new JsonArray();
        JsonObject content = new JsonObject();

        // parts array for user message
        JsonArray partsArray = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        partsArray.add(part);

        content.add("parts", partsArray);
        contentsArray.add(content);
        requestJson.add("contents", contentsArray);

        // generation config (optimized for Gemini 2.0 Flash)
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.8);
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 8192);
        generationConfig.addProperty("responseMimeType", "text/plain");
        requestJson.add("generationConfig", generationConfig);

        // Safety settings (optional - can be adjusted based on your needs)
        JsonArray safetySettings = new JsonArray();

        JsonObject harassmentSetting = new JsonObject();
        harassmentSetting.addProperty("category", "HARM_CATEGORY_HARASSMENT");
        harassmentSetting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(harassmentSetting);

        JsonObject hateSpeechSetting = new JsonObject();
        hateSpeechSetting.addProperty("category", "HARM_CATEGORY_HATE_SPEECH");
        hateSpeechSetting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(hateSpeechSetting);

        JsonObject sexualSetting = new JsonObject();
        sexualSetting.addProperty("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT");
        sexualSetting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(sexualSetting);

        JsonObject dangerousSetting = new JsonObject();
        dangerousSetting.addProperty("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        dangerousSetting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(dangerousSetting);

        requestJson.add("safetySettings", safetySettings);

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
                callback.onError("Connection failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respStr = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response: " + respStr);

                if (response.isSuccessful()) {
                    try {
                        JsonObject json = gson.fromJson(respStr, JsonObject.class);

                        // Parse Gemini 2.0 Flash API response format
                        if (json.has("candidates") && json.getAsJsonArray("candidates").size() > 0) {
                            JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();

                            // Check for finish reason
                            if (candidate.has("finishReason")) {
                                String finishReason = candidate.get("finishReason").getAsString();
                                if (!"STOP".equals(finishReason)) {
                                    Log.w(TAG, "Response finished with reason: " + finishReason);
                                    if ("SAFETY".equals(finishReason)) {
                                        callback.onError("Response blocked due to safety concerns");
                                        return;
                                    }
                                }
                            }

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

                        // Check for error in response
                        if (json.has("error")) {
                            JsonObject error = json.getAsJsonObject("error");
                            String errorMessage = error.has("message") ? error.get("message").getAsString() : "Unknown API error";
                            callback.onError("API Error: " + errorMessage);
                            return;
                        }

                        callback.onError("No valid response content found in API response");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse response: " + e.getMessage());
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "HTTP error: " + response.code() + " - " + response.message());

                    // Parse error response for more details
                    try {
                        JsonObject errorJson = gson.fromJson(respStr, JsonObject.class);
                        if (errorJson.has("error")) {
                            JsonObject error = errorJson.getAsJsonObject("error");
                            String errorMessage = error.has("message") ? error.get("message").getAsString() : response.message();
                            callback.onError("HTTP " + response.code() + ": " + errorMessage);
                        } else {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    } catch (Exception e) {
                        callback.onError("HTTP " + response.code() + ": " + response.message() + "\n" + respStr);
                    }
                }
            }
        });
    }

    // Optional: Method to send message without Alfred character (if needed)
    public void sendRawMessage(String prompt, Callback callback) {
        // Request for Gemini 2.0 Flash API without system instruction
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

        // generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.8);
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 8192);
        requestJson.add("generationConfig", generationConfig);

        // Continue with the same request logic as sendMessage...
        // (Implementation would be similar to sendMessage method)
    }
}