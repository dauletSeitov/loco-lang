const Sidebar = ({
  projects,
  onSelectFile,
  activeProject,
  onSaveProject,
  onCreateFile,
  onDeleteFile,
}) => {
  return (
    <aside className="w-[25%] min-w-[180px] rounded-2xl border border-emerald-500/20 bg-[#0e1316]/90 p-5 shadow-[0_0_30px_-12px_rgba(39,227,137,0.35)]">
      <div className="mb-6">
        <h1 className="mt-2 text-2xl font-semibold text-slate-100">
          Loco Lang
        </h1>
      </div>

      <div className="mt-4 space-y-4 text-sm text-slate-300">
        {projects.map((project, index) => (
          <div className="rounded-lg border border-emerald-500/20 bg-[#12181c] px-3 py-2">
            <div className="flex items-center justify-between gap-2">
              <p className="text-[11px] uppercase tracking-[0.25em] text-emerald-200">
                {project.name}
              </p>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => onCreateFile?.(index)}
                  className="rounded-md border border-emerald-500/20 bg-emerald-500/10 px-2 py-1 text-xl uppercase tracking-[0.25em] text-emerald-100"
                >
                   <i class="fa-solid fa-file-circle-plus"></i>
                </button>
                <button
                  type="button"
                  onClick={() => onSaveProject?.(index)}
                  className="rounded-md border border-emerald-500/20 bg-emerald-500/10 px-2 py-1 text-xl uppercase tracking-[0.25em] text-emerald-100"
                >
                  <i className="fa-regular fa-floppy-disk"></i>
                </button>
              </div>
            </div>
            <ul className="mt-2 space-y-2 ">
              {project.content.map((file) => (
                <li
                  onClick={() =>
                    onSelectFile({ index: index, fileName: file.fileName })
                  }
                  key={`current-${file.fileName}`}
                  className={`rounded-lg border px-3 py-2 ${
                    activeProject?.index === index &&
                    activeProject?.fileName === file.fileName
                      ? "border-emerald-400/70 bg-emerald-400/10"
                      : "border-emerald-500/20 bg-[#12181c]"
                  }`}
                >
                  <div className="flex items-center justify-between text-emerald-100">
                    <span>{file.fileName}</span>
                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        onDeleteFile?.(index, file.fileName);
                      }}
                      className="rounded border border-red-500/30 bg-red-500/10 px-2 py-1 text-red-200"
                      aria-label={`Delete ${file.fileName}`}
                    >
                      <i className="fa-solid fa-x h-3 w-3" aria-hidden="true" />

                      

                     
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </aside>
  );
};

export default Sidebar;
