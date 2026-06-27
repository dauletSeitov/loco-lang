package com.example.locolangbackend.project;

import analyze.ExpressionAnalyzer;
import analyze.Library;
import analyze.SourceFile;
import com.example.locolangbackend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.locolangbackend.utils.CollectionUtils.concat;
import static com.example.locolangbackend.utils.ExceptionUtils.unchecked;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final StorageService storageService;

    private static final Map<String, String> projectNameToLocationMap = Map.of(
            "Binary search", "binary-search",
            "Basics", "basics",
            "Errors and edge cases", "errors-and-edge-cases",
            "Functions", "functions",
            "List operations", "list-operations",
            "String operations", "string-operations",
            "Structure map operations", "structure map operations"
    );

    public List<Project> get(String sessionId) {
        var projects = storageService.getBySession(sessionId);
        if (projects == null || projects.isEmpty()) {
            var list = getDefault();
            storageService.put(sessionId, list);
            return list;
        }

        return projects;
    }


    private List<Project> getDefault() {
        Library defaultLibraries = ExpressionAnalyzer.getDefaultLibraries();
        var std = defaultLibraries.libraries().stream().map(it -> new File(it.fileName(), it.content())).toList();

        var stdProject = new Project("Standard library", std);
        var restDefaultProjects = projectNameToLocationMap.entrySet().stream()
                .map(it -> new Project(it.getKey(), readAllFiles(it.getValue())))
                .toList();
        return concat(stdProject, restDefaultProjects);
    }

    public List<File> readAllFiles(String projectPath) {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = unchecked(() -> resolver.getResources("classpath:examples/" + projectPath + "/*"));
        return Arrays.stream(resources)
                .map(it ->
                        unchecked(() -> new File(it.getFilename(), new String(it.getInputStream().readAllBytes()))))
                .toList();
    }

    public ExecutionResult execute(String sessionId, Project project) {

        var ok = ExpressionAnalyzer.execute(project.content().stream()
                .map(it -> new SourceFile(it.fileName(), it.content()))
                .toList());


        return new ExecutionResult(ok.stdout, ok.stderr);
    }

    public ExecutionResult save(String sessionId, Project project) {
        storageService.put(sessionId, project);
        return null;
    }
}
