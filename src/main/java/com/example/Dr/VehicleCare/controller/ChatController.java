package com.example.Dr.VehicleCare.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.model.ChatResponse;
import com.example.Dr.VehicleCare.service.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ChatResponse askAI(@RequestBody Map<String, String> body) throws Exception {
        String message = body.get("message");
        return chatService.getAIResponse(message);
    }
}