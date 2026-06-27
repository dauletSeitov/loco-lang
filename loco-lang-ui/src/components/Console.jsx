const Console = ({ executionResult, isRunning, errorMessage }) => {
  const stdout = executionResult?.stdout ?? ""
  const stderr = executionResult?.stderr ?? ""
  const hasOutput = Boolean(stdout || stderr || errorMessage)
  return (
    <section className="h-[28%] min-h-[180px] rounded-2xl border border-[color:var(--emerald-500-20)] bg-[color:var(--console-bg)]">
      <div className="flex items-center justify-between border-b border-[color:var(--emerald-500-10)] px-5 py-3 text-xs uppercase tracking-[0.3em] text-[color:var(--emerald-300-70)]">
        <span>Console</span>
        <span className="text-[color:var(--emerald-300-60)]">
          {isRunning ? "running" : "idle"}
        </span>
      </div>
      <div className="h-full overflow-auto px-5 py-4 font-mono text-sm text-[color:var(--emerald-200)]">
        {!hasOutput ? (
          <p className="text-[color:var(--emerald-200-70)]">
            Run the project to see output here.
          </p>
        ) : (
          <div className="space-y-3">
            {stdout ? (
              <div>
                <pre className="whitespace-pre-wrap break-words text-[color:var(--emerald-100)]">
                  {stdout}
                </pre>
              </div>
            ) : null}
            {stderr ? (
              <div>
              
                <pre className="whitespace-pre-wrap break-words text-red-300">
                  {stderr}
                </pre>
              </div>
            ) : null}
            {errorMessage ? (
              <div>
                <pre className="whitespace-pre-wrap break-words text-red-300">
                  {errorMessage}
                </pre>
              </div>
            ) : null}
          </div>
        )}
      </div>
    </section>
  )
}

export default Console
