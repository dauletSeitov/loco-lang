const API_BASE = "http://localhost:8080/api"
const SESSION_KEY = "loco_session_id"
const SESSION_HEADER = "X-Session-Id"
const APP_ID_HEADER = "X-App-Id"

const readStoredSession = () => {
  try {
    const stored = window.localStorage.getItem(SESSION_KEY)
    if (stored) return stored
  } catch (error) {
    console.warn("Session storage unavailable", error)
  }
  return null
}

const generateAppId = () => {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `app_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
}

const persistSession = (id) => {
  try {
    window.localStorage.setItem(SESSION_KEY, id)
  } catch (error) {
    console.warn("Failed to persist session", error)
  }
}

const fetchSession = async () => {
  const stored = readStoredSession()
  if (stored) return stored
  const response = await fetch(`${API_BASE}/session`, {
    method: "POST",
    headers: {
      [APP_ID_HEADER]: generateAppId(),
    },
  })
  if (!response.ok) {
    throw new Error(`Session request failed: ${response.status}`)
  }
  const raw = await response.text()
  const nextId = raw.trim()
  if (!nextId) {
    throw new Error("Session response missing session id")
  }
  persistSession(nextId)
  return nextId
}

export const getProjects = async () => {
  const id = await fetchSession()
  const response = await fetch(`${API_BASE}/projects`, {
    headers: {
      [SESSION_HEADER]: id,
      [APP_ID_HEADER]: generateAppId(),
    },
  })
  if (!response.ok) {
    throw new Error(`Projects request failed: ${response.status}`)
  }
  return response.json()
}

export const executeProject = async (project) => {
  const id = await fetchSession()
  const response = await fetch(`${API_BASE}/projects`, {
    method: "POST",
    headers: {
      [SESSION_HEADER]: id,
      [APP_ID_HEADER]: generateAppId(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(project ?? {}),
  })
  if (!response.ok) {
    throw new Error(`Execution request failed: ${response.status}`)
  }
  return response.json()
}
