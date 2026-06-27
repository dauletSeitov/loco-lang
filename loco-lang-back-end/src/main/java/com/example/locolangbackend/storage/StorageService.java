package com.example.locolangbackend.storage;

import com.example.locolangbackend.project.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorageService {
    private final Map<String, List<Project>> sessions;

    public StorageService(@Value("${session.max-sessions:1000}") int maxSessions) {
        this.sessions = Collections.synchronizedMap(new LinkedHashMap<String, List<Project>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<Project>> eldest) {
                return size() > maxSessions;
            }
        });
    }

    public void addSession(String sessionId) {
        sessions.put(sessionId, Collections.emptyList());
    }

    public List<Project> getBySession(String session) {
        return sessions.get(session);
    }

    public void put(String sessionId, List<Project> projects) {
        sessions.put(sessionId, projects);
    }

    public void put(String sessionId, Project projects) {

        sessions.get(sessionId).stream().findAny(it->it.df = "");

        sessions.put(sessionId, projects);
    }
}
