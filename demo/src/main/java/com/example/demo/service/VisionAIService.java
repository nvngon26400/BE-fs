package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VisionAIService {
    
    @Value("${vision.ai.api.key:}")
    private String apiKey;
    
    @Value("${vision.ai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public VisionAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public String analyzeAssetImage(MultipartFile imageFile) throws IOException {
        if (apiKey.isEmpty()) {
            log.warn("Vision AI API key not configured, returning mock data");
            return generateMockAnalysis();
        }
        
        try {
            // Encode image to base64
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            
            // Prepare the request payload
            Map<String, Object> requestBody = createVisionRequest(base64Image);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class);
            
            // Parse response
            return parseVisionResponse(response.getBody());
            
        } catch (Exception e) {
            log.error("Error calling Vision AI API", e);
            return generateMockAnalysis();
        }
    }
    
    private Map<String, Object> createVisionRequest(String base64Image) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "gpt-4-vision-preview");
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", createAssetAnalysisPrompt());
        
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        
        imageContent.put("image_url", imageUrl);
        
        message.put("content", new Object[]{content, imageContent});
        
        request.put("messages", new Object[]{message});
        request.put("max_tokens", 1000);
        
        return request;
    }
    
    private String createAssetAnalysisPrompt() {
        return """
            Analyze this asset image and extract the following information in JSON format:
            {
                "deviceNumber": "Generated device number (format: ASSET-YYYY-NNNNN)",
                "department": "Department name if visible",
                "barcode": "Barcode/QR code content if readable",
                "serialNumber": "Serial number if visible",
                "model": "Model number if visible",
                "manufacturer": "Manufacturer name if visible",
                "location": "Location description from image",
                "condition": "Physical condition (Good/Fair/Poor/Damaged)",
                "notes": "Additional observations"
            }
            
            If any information is not clearly visible, mark as "Not visible" or make reasonable assumptions.
            Generate a unique device number using format ASSET-YYYY-NNNNN where YYYY is current year and NNNNN is 5-digit sequence.
            """;
    }
    
    private String parseVisionResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode choices = jsonNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Vision AI response", e);
        }
        return generateMockAnalysis();
    }
    
    private String generateMockAnalysis() {
        return """
            {
                "deviceNumber": "ASSET-2025-00001",
                "department": "IT Department",
                "barcode": "123456789012",
                "serialNumber": "SN-ABC123456",
                "model": "Dell OptiPlex 7090",
                "manufacturer": "Dell Technologies",
                "location": "Office Floor 3, Desk A-15",
                "condition": "Good",
                "notes": "Mock data for demonstration. Computer appears to be in working condition."
            }
            """;
    }
    
    public Map<String, Object> parseAnalysisResult(String analysisResult) {
        try {
            return objectMapper.readValue(analysisResult, Map.class);
        } catch (Exception e) {
            log.error("Error parsing analysis result", e);
            Map<String, Object> mockResult = new HashMap<>();
            mockResult.put("deviceNumber", "ASSET-2025-00001");
            mockResult.put("department", "Unknown");
            mockResult.put("condition", "Unknown");
            return mockResult;
        }
    }
}
