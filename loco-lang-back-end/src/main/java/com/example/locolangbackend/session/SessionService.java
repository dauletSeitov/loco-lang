package com.example.locolangbackend.session;

import com.example.locolangbackend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final StorageService storageService;

    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        storageService.addSession(sessionId);
        return sessionId;
    }
}
