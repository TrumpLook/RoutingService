const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8081";

export async function buildRoute(payload) {
  const response = await fetch(`${API_BASE_URL}/route`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(`Route request failed with status ${response.status}`);
  }

  return response.json();
}

export async function getActiveEvents() {
  const response = await fetch(`${API_BASE_URL}/events/active`);

  if (!response.ok) {
    throw new Error(`Events request failed with status ${response.status}`);
  }

  return response.json();
}
