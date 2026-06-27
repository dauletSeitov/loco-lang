import { useState } from "react";
import Console from "./Console";
import WorkingArea from "./WorkingArea";

const EditorPane = ({
  projectName,
  activeFile,
  project,
  onRunProject,
  onChangeFile,
}) => {
  const [executionResult, setExecutionResult] = useState(null);
  const [executionError, setExecutionError] = useState("");
  const [isRunning, setIsRunning] = useState(false);

  const handleRun = async () => {
    if (!project || !onRunProject) return;
    setIsRunning(true);
    setExecutionError("");
    try {
      const result = await onRunProject(project);
      setExecutionResult(result ?? null);
    } catch (error) {
      console.error("Failed to execute project", error);
      setExecutionResult(null);
      setExecutionError(
        error?.message ?? "Something went wrong while running the project.",
      );
    } finally {
      setIsRunning(false);
    }
  };

  return (
    <>
      <WorkingArea
        projectName={projectName}
        activeFile={activeFile}
        onRun={handleRun}
        onChangeText={onChangeFile}
      />
      <Console
        executionResult={executionResult}
        errorMessage={executionError}
        isRunning={isRunning}
      />
    </>
  );
};

export default EditorPane;
