package com.example.Dr.VehicleCare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.model.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatResponse getAIResponse(String userMessage) {
        try {
            // Build JSON body manually
            String jsonBody = "{\n" +
                    "  \"model\": \"" + model + "\",\n" +
                    "  \"messages\": [\n" +
                    "    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n" +
                    "    {\"role\": \"user\", \"content\": \"" + userMessage + "\"}\n" +
                    "  ],\n" +
                    "  \"max_tokens\": 500\n" +
                    "}";

            MediaType JSON = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(JSON, jsonBody);

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return new ChatResponse("OpenAI request failed. HTTP code: " + response.code());
            }

            String resultJson = response.body().string();
            JsonNode root = mapper.readTree(resultJson);

            String reply = root
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            return new ChatResponse(reply);

        } catch (Exception e) {
            return new ChatResponse("Error while contacting OpenAI: " + e.getMessage());
        }
    }
}
