package com.microsoft.cognitiveservices.speech.samples.sdkdemo;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatAPI {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static List<JsonNode> messages = new ArrayList<>();

    public static String getChatDeployment() {
        return "";
    }

    public static JsonNode getLLMResponse(List<JsonNode> messages, List<JsonNode> tools) throws IOException {
        int i = 20;
        List<JsonNode> messagesAi = messages.subList(Math.max(messages.size() - i, 0), messages.size());

        while (messagesAi.get(0).has("role") && "tool".equals(messagesAi.get(0).get("role").asText())) {
            i++;
            messagesAi = messages.subList(Math.max(messages.size() - i, 0), messages.size());
        }

        //String deploymentModel = getChatDeployment();
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode messagesArray = objectMapper.createArrayNode();
        messagesArray.addAll(messagesAi);
        Log.i("ChatAPI","messages: "+messagesArray);
        //requestBody.put("engine", deploymentModel);
        requestBody.set("messages", messagesArray);
        requestBody.put("temperature", 0.6);
        requestBody.put("max_tokens", 500);
        //requestBody.set("tools", objectMapper.valueToTree(tools));
        //requestBody.put("tool_choice", "auto");
        requestBody.put("stream", false);

        URL url = new URL("https://xxx.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-15-preview"); // Replace with actual API endpoint
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("api-key", "xxx"); // Replace with actual API key
        connection.setDoOutput(true);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(objectMapper.writeValueAsString(requestBody));
            writer.flush();
            Log.i("ChatAPI","Write");
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
                Log.i("ChatAPI","response: "+line);
            }
        }

        return objectMapper.readTree(responseBuilder.toString()).get("choices").get(0).get("message");
    }

    public static JsonNode runConversation(List<JsonNode> messages, List<JsonNode> tools) throws IOException {
        JsonNode responseMessage = getLLMResponse(messages, tools);

        if (responseMessage.has("tool_calls")) {
            ArrayNode toolCalls = (ArrayNode) responseMessage.get("tool_calls");

            messages.add(responseMessage);

            for (JsonNode toolCall : toolCalls) {
                System.out.println("⏳Call internal function...");
                String functionName = toolCall.get("function").get("name").asText();
                System.out.println("⏳Call " + functionName + "...");

                Map<String, Object> functionArgs = objectMapper.convertValue(toolCall.get("function").get("arguments"), Map.class);

                System.out.println("⏳Call params: " + functionArgs);

                Object functionResponse = callFunctionByName(functionName, functionArgs);
                System.out.println("⏳Call internal function done!");
                System.out.println("执行结果：");
                System.out.println(functionResponse);
                System.out.println("===================================");

                ObjectNode functionResponseNode = objectMapper.createObjectNode();
                functionResponseNode.put("tool_call_id", toolCall.get("id").asText());
                functionResponseNode.put("role", "tool");
                functionResponseNode.put("name", functionName);
                functionResponseNode.put("content", functionResponse.toString());

                messages.add(functionResponseNode);
            }

            return runConversation(messages, tools);
        } else {
            return responseMessage;
        }
    }

    public static String generateText(String prompt) throws IOException {
        Log.i("ChatAPI", "prompt: " + prompt);
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        List<JsonNode> tools = getTools();
        JsonNode response = runConversation(messages, tools);
        String r = response.get("content").asText();
        Log.i("ChatAPI", "r: " + r);
        ObjectNode assistantMessage = objectMapper.createObjectNode();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", r);
        messages.add(assistantMessage);
        return r;
    }

    private static List<JsonNode> getTools() {
        // Mock implementation of getting tools, replace with actual logic
        return new ArrayList<>();
    }

    private static Object callFunctionByName(String functionName, Map<String, Object> functionArgs) {
        switch (functionName) {
            case "exampleFunction":
                return exampleFunction(functionArgs);
            default:
                throw new IllegalArgumentException("Unknown function: " + functionName);
        }
    }

    private static Object exampleFunction(Map<String, Object> functionArgs) {
        return "Example function response";
    }
}
