const API_BASE = '/api'

export type RagSource = {
  sourceName: string
  documentId: string
  excerpt: string
}

export type RagResponse = {
  answer: string
  sources: RagSource[]
}

export type IngestResponse = {
  chunksIngested: number
  filename: string
}

export async function queryRag(query: string, topK = 10): Promise<RagResponse> {
  const res = await fetch(`${API_BASE}/query`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, topK }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error((err as { error?: string }).error || res.statusText)
  }
  return res.json()
}

export type SeedResponse = {
  filesIngested: number
  totalChunks: number
  details: Array<{ filename: string; chunksIngested?: number; error?: string }>
}

export async function ingestDocument(file: File): Promise<IngestResponse> {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(`${API_BASE}/documents/ingest`, {
    method: 'POST',
    body: form,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error((err as { error?: string }).error || res.statusText)
  }
  return res.json()
}

export async function seedDatabase(): Promise<SeedResponse> {
  const res = await fetch(`${API_BASE}/documents/seed`, { method: 'POST' })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error((err as { error?: string }).error || res.statusText)
  }
  return res.json()
}
