import { useEffect, useState } from "react";
import Sidebar from "./components/Sidebar";
import EditorPane from "./components/EditorPane";
import { executeProject, getProjects } from "./services/projects";

const PROJECTS_STORAGE_KEY = "loco_saved_projects_v1";

const loadSavedProjects = () => {
  try {
    const raw = window.localStorage.getItem(PROJECTS_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : null;
  } catch (error) {
    console.warn("Failed to read saved projects", error);
    return null;
  }
};

const persistProjects = (projects) => {
  try {
    window.localStorage.setItem(
      PROJECTS_STORAGE_KEY,
      JSON.stringify(projects),
    );
  } catch (error) {
    console.warn("Failed to save projects", error);
  }
};

function App() {
  const [projects, setProjects] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [activeProject, setActiveProject] = useState({
    index: 0,
    fileName: "main.ll",
  });

  useEffect(() => {
    let isMounted = true;
    getProjects()
      .then((data) => {
        if (!isMounted) return;
        const stored = loadSavedProjects();
        setProjects(stored ?? (Array.isArray(data) ? data : []));
        setErrorMessage("");
      })
      .catch((error) => {
        console.error("Failed to load projects", error);
        if (!isMounted) return;
        setErrorMessage(
          error?.message ?? "Something went wrong while loading projects."
        );
      });
    return () => {
      isMounted = false;
    };
  }, []);

  const currentProject = projects[activeProject.index];
  const currentFile = currentProject?.content?.find(
    (it) => it.fileName === activeProject.fileName,
  );

  const handleUpdateFile = (nextContent) => {
    setProjects((prev) =>
      prev.map((project, index) => {
        if (index !== activeProject.index) return project;
        const nextFiles = project.content.map((file) =>
          file.fileName === activeProject.fileName
            ? { ...file, content: nextContent }
            : file,
        );
        return { ...project, content: nextFiles };
      }),
    );
  };

  const handleSaveProject = (projectIndex) => {
    const project = projects[projectIndex];
    if (!project) return;
    persistProjects(projects);
  };

  const handleCreateFile = (projectIndex) => {
    setProjects((prev) => {
      const project = prev[projectIndex];
      if (!project) return prev;
      const existingNames = new Set(
        (project.content ?? []).map((file) => file.fileName),
      );
      let counter = 1;
      let nextName = "new_file.ll";
      while (existingNames.has(nextName)) {
        counter += 1;
        nextName = `new_file_${counter}.ll`;
      }
      const nextFile = { fileName: nextName, content: "" };
      const nextProject = {
        ...project,
        content: [...(project.content ?? []), nextFile],
      };
      const nextProjects = prev.map((item, idx) =>
        idx === projectIndex ? nextProject : item,
      );
      setActiveProject({ index: projectIndex, fileName: nextName });
      return nextProjects;
    });
  };

  const handleDeleteFile = (projectIndex, fileName) => {
    setProjects((prev) => {
      const project = prev[projectIndex];
      if (!project) return prev;
      const nextFiles = (project.content ?? []).filter(
        (file) => file.fileName !== fileName,
      );
      const nextProject = { ...project, content: nextFiles };
      const nextProjects = prev.map((item, idx) =>
        idx === projectIndex ? nextProject : item,
      );
      if (
        activeProject.index === projectIndex &&
        activeProject.fileName === fileName
      ) {
        const fallback = nextFiles[0]?.fileName ?? "";
        setActiveProject({ index: projectIndex, fileName: fallback });
      }
      return nextProjects;
    });
  };

  return (
    <div className="app-shell h-screen overflow-hidden">
      <div className="flex h-full min-h-0 gap-4 px-0 py-6 overflow-hidden">
        <Sidebar
          onSelectFile={setActiveProject}
          projects={projects}
          activeProject={activeProject}
          onSaveProject={handleSaveProject}
          onCreateFile={handleCreateFile}
          onDeleteFile={handleDeleteFile}
        />
        <main className="flex flex-1 min-h-0 flex-col gap-4">
          <EditorPane
            projectName={currentProject?.name ?? "project"}
            activeFile={currentFile}
            project={currentProject}
            onRunProject={executeProject}
            onChangeFile={handleUpdateFile}
          />
          {errorMessage ? (
            <div className="rounded-xl border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-100">
              {errorMessage}
            </div>
          ) : null}
        </main>
      </div>
    </div>
  );
}

export default App;
