package com.example.locolangbackend.project;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<Project> getAllProjects(@RequestHeader(value = "X-SESSION-ID", required = false) String sessionId) {
        return projectService.get(sessionId);
    }


    @PostMapping
    public ResponseEntity<ExecutionResult> execute(@RequestHeader(value = "X-SESSION-ID", required = false) String sessionId,
                                                   @RequestBody Project project) {
        return ResponseEntity.ok(projectService.execute(sessionId, project));
    }

    @PostMapping("/save")
    public ResponseEntity<ExecutionResult> save(@RequestHeader(value = "X-SESSION-ID", required = false) String sessionId,
                                                   @RequestBody Project project) {
        return ResponseEntity.ok(projectService.save(sessionId, project));
    }

}
