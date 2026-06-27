import { useMemo, useRef } from "react";

const KEYWORDS = new Set([
  "import",
  "fun",
  "var",
  "return",
  "for",
  "if",
  "else",
  "null",
  "true",
  "false",
]);

const TYPES = new Set([
  "NULL",
  "NUMBER",
  "STRING",
  "BOOLEAN",
  "ARRAY",
  "STRUCTURE",
  "FUNCTION",
]);

const BUILTINS = new Set([
  "println",
  "readln",
  "size",
  "toNumber",
  "toString",
  "typeOf",
]);

const escapeHtml = (value) =>
  value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");

const isWhitespace = (value) => value === " " || value === "\t" || value === "\n" || value === "\r";

const nextNonWhitespace = (value, start) => {
  let idx = start;
  while (idx < value.length && isWhitespace(value[idx])) {
    idx += 1;
  }
  return idx;
};

const highlightLL = (value) => {
  let html = "";
  let i = 0;
  let expectFunctionName = false;
  while (i < value.length) {
    const ch = value[i];
    const next = value[i + 1];

    if (ch === "/" && next === "/") {
      let end = value.indexOf("\n", i);
      if (end === -1) end = value.length;
      const comment = value.slice(i, end);
      html += `<span class="token-comment">${escapeHtml(comment)}</span>`;
      i = end;
      continue;
    }

    if (ch === '"') {
      let end = i + 1;
      while (end < value.length) {
        if (value[end] === '"' && value[end - 1] !== "\\") {
          end += 1;
          break;
        }
        end += 1;
      }
      const str = value.slice(i, end);
      html += `<span class="token-string">${escapeHtml(str)}</span>`;
      i = end;
      continue;
    }

    if (/[0-9]/.test(ch)) {
      let end = i + 1;
      while (end < value.length && /[0-9.]/.test(value[end])) {
        end += 1;
      }
      const number = value.slice(i, end);
      html += `<span class="token-number">${escapeHtml(number)}</span>`;
      i = end;
      continue;
    }

    if (/[A-Za-z_]/.test(ch)) {
      let end = i + 1;
      while (end < value.length && /[A-Za-z0-9_]/.test(value[end])) {
        end += 1;
      }
      const word = value.slice(i, end);
      let tokenClass = "";
      if (expectFunctionName) {
        tokenClass = "token-function";
        expectFunctionName = false;
      } else if (KEYWORDS.has(word)) {
        tokenClass = "token-keyword";
        if (word === "fun") {
          expectFunctionName = true;
        }
      } else if (TYPES.has(word)) {
        tokenClass = "token-type";
      } else if (BUILTINS.has(word)) {
        tokenClass = "token-builtin";
      } else {
        const nextIdx = nextNonWhitespace(value, end);
        if (value[nextIdx] === "(") {
          tokenClass = "token-function";
        }
      }
      if (tokenClass) {
        html += `<span class="${tokenClass}">${escapeHtml(word)}</span>`;
      } else {
        html += escapeHtml(word);
      }
      i = end;
      continue;
    }

    html += escapeHtml(ch);
    i += 1;
  }
  return html;
};

const WorkingArea = ({ activeFile, projectName, onRun, onChangeText }) => {
  const lineNumbersRef = useRef(null);
  const highlightRef = useRef(null);
  const activeBuffer = activeFile?.content ?? "";

  const lineNumbers = useMemo(() => {
    const lineCount = Math.max(1, activeBuffer.split("\n").length);
    return Array.from({ length: lineCount }, (_, index) => index + 1);
  }, [activeBuffer]);

  const highlighted = useMemo(() => highlightLL(activeBuffer), [activeBuffer]);

  const handleChange = (event) => {
    const { value } = event.target;
    if (onChangeText) onChangeText(value);
  };

  const handleScroll = (event) => {
    if (lineNumbersRef.current) {
      lineNumbersRef.current.scrollTop = event.target.scrollTop;
    }
    if (highlightRef.current) {
      highlightRef.current.scrollTop = event.target.scrollTop;
      highlightRef.current.scrollLeft = event.target.scrollLeft;
    }
  };

  const handleKeyDown = (event) => {
    if (event.key !== "Tab") return;
    event.preventDefault();
    const { selectionStart, selectionEnd, value } = event.target;
    const nextValue =
      value.slice(0, selectionStart) + "\t" + value.slice(selectionEnd);
    // onUpdateBuffers((prev) => ({ ...prev, [activeFile]: nextValue }));
    requestAnimationFrame(() => {
      event.target.selectionStart = selectionStart + 1;
      event.target.selectionEnd = selectionStart + 1;
    });
  };

  return (
    <section className="flex flex-1 min-h-0 flex-col overflow-hidden rounded-2xl border border-[color:var(--emerald-500-20)] bg-[color:var(--panel-bg)]">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[color:var(--emerald-500-10)] px-5 py-3 text-x tracking-[0.3em] text-[color:var(--emerald-300-70)]">
        <span className="inline-flex items-center gap-2 text-xs uppercase tracking-[0.3em]">
          <span className="text-[color:var(--emerald-200)]">/</span>
          <span className="text-[color:var(--emerald-100)]">
            {projectName}
          </span>
          <span className="text-[color:var(--emerald-200)]">/</span>
          <span className="text-[color:var(--emerald-50)]">
            {activeFile?.fileName}
          </span>
        </span>
        <button
          type="button"
          onClick={onRun}
          className="flex items-center gap-2 rounded-lg border border-[color:var(--emerald-500-20)] bg-[color:var(--emerald-500-10)] px-5 py-2 text-[color:var(--emerald-100)]"
        >
          <span className="inline-flex h-4 w-4 items-center justify-center text-[color:var(--emerald-400)] text-lg leading-none">
            ▶
          </span>
          RUN
        </button>
      </div>
      <div className="flex-1 min-h-0 overflow-hidden px-5 py-4">
        <div className="flex h-full overflow-hidden rounded-xl border border-[color:var(--emerald-500-20)] bg-[color:var(--panel-bg-alt)]">
          <div
            ref={lineNumbersRef}
            className="w-14 shrink-0 overflow-hidden border-r border-[color:var(--emerald-500-10)] bg-[color:var(--panel-bg-strong)] text-right font-mono text-xs text-[color:var(--emerald-500-60)]"
            aria-hidden="true"
          >
            <div className="py-3 pr-3 leading-6">
              {lineNumbers.map((line) => (
                <div key={line}>{line}</div>
              ))}
            </div>
          </div>
          <div className="ll-editor flex-1 min-h-0 h-full">
            <pre
              ref={highlightRef}
              className="ll-highlight px-4 py-3 font-mono text-sm leading-6"
              aria-hidden="true"
              dangerouslySetInnerHTML={{ __html: highlighted + "\n" }}
            />
            <textarea
              value={activeBuffer}
              onChange={handleChange}
              onKeyDown={handleKeyDown}
              onScroll={handleScroll}
              spellCheck={false}
              className="relative z-10 h-full w-full resize-none bg-transparent px-4 py-3 font-mono text-sm leading-6 text-transparent caret-[color:var(--emerald-50-90)] outline-none focus-visible:border-[color:var(--emerald-500-60)] focus-visible:ring-2 focus-visible:ring-[color:var(--emerald-500-10)]"
              aria-label={`${activeFile?.fileName ?? "file"} editor`}
            />
          </div>
        </div>
      </div>
    </section>
  );
};

export default WorkingArea;
