# InsightStream – Enterprise RAG Platform

A **Retrieval-Augmented Generation (RAG)** platform for querying internal documents using LLMs, with **hybrid search** (BM25 + semantic + RRF) and **config-driven agents** (model and provider switchable via YAML).

## Features

- **Hybrid search**: BM25 (Lucene) + semantic (Chroma DB) with **Reciprocal Rank Fusion (RRF)**
- **Config-driven agents**: `application.yml` defines teams, agents (provider + model), and tasks; change model name to switch LLM
- **Document ingestion**: PDF and plain text; chunking with configurable size/overlap
- **Modular, OOP backend**: Spring Boot 3.x, clear separation of config, document, embedding, vector, agent, and RAG layers
- **Frontend**: React (TypeScript), Tailwind CSS, TanStack Query

## Tech Stack

| Layer      | Technology |
|-----------|------------|
| Backend   | Spring Boot 3.x, Spring AI (Ollama/OpenAI) |
| Vector DB | Chroma DB (HTTP API) |
| Embeddings| Spring AI EmbeddingModel (Ollama `nomic-embed-text` or OpenAI) |
| BM25      | Apache Lucene (in-memory) |
| Frontend  | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query |

## Prerequisites

- **Java 21**
- **Node.js 18+** (for frontend)
- **Ollama** (default for chat + embeddings): [ollama.ai](https://ollama.ai)  
  - Pull models: `ollama pull llama3.2:latest` and `ollama pull nomic-embed-text:latest`
- **Chroma DB** (vector store):  
  `docker run -it --rm -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0`

## Quick Start

### 1. Start Chroma DB

**Option A – Docker** (if you have Docker):

```bash
docker run -it --rm -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0
```

**Option B – Without Docker** (Python 3.9+ required):

```bash
pip install chromadb
chroma run --path ./chroma_data --port 8000
```

Leave the Chroma process running in that terminal.

### 2. Start Ollama (if using local LLMs)

```bash
ollama serve
ollama pull llama3.2:latest
ollama pull nomic-embed-text:latest
```

### 3. Backend

```bash
cd backend
mvn spring-boot:run
```

Optional env vars:

- `CHROMA_URL` – Chroma base URL (default: `http://localhost:8000`)
- `OLLAMA_BASE_URL` – Ollama API URL (default: `http://localhost:11434`)

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

**First time:** Go to **Ingest** → click **Seed database** to load bundled open-source data (public domain excerpts, sample policies). Then use **Query** to ask questions.

You can also upload your own PDF/TXT files in the Ingest tab.

## Open-Source Seed Data

The backend ships with seed data in `backend/src/main/resources/seed-data/`:

| File | Description |
|------|-------------|
| `art-of-war-excerpt.txt` | Sun Tzu, *The Art of War* (Project Gutenberg, public domain) |
| `meditations-excerpt.txt` | Marcus Aurelius, *Meditations* (Project Gutenberg, public domain) |
| `remote-work-policy.txt` | Sample internal remote work policy |
| `engineering-handbook.txt` | Sample engineering standards |

**To ingest:** In the UI, go to **Ingest** and click **Seed database**, or call:

```bash
curl -X POST http://localhost:8080/api/documents/seed
```

Response: `{ "filesIngested": 5, "totalChunks": N, "details": [...] }`.

To add your own files: use the upload area on the Ingest tab, or:

```bash
curl -X POST http://localhost:8080/api/documents/ingest -F "file=@/path/to/document.pdf"
```

## Configuration (Agents & Models)

Agents and models are driven by **`backend/src/main/resources/application.yml`**.

### Global parameters

```yaml
insightstream:
  global-parameters:
    chunk-size: 1000
    chunk-overlap: 200
    embedding-provider: ollama
    embedding-model: nomic-embed-text:latest
    query-agent-provider: ollama
    query-agent-model: llama3.2:latest
```

### Team and agent (model by name)

Under `insightstream.team-configuration` you define teams, each with **agents** (provider + model + system prompt) and **tasks** (user prompt template):

```yaml
  team-configuration:
    QueryTeam:
      description: "Handles user queries over the document corpus using RAG."
      agents:
        query_agent:
          max-tokens: 4096
          model: llama3.2:latest   # change to switch model
          provider: ollama        # ollama | openai
          temperature: 0.3
          system-prompt: |
            You are a helpful enterprise assistant. Answer using ONLY the provided context.
      tasks:
        answer:
          user-prompt: |
            Context from internal documents:
            {context}
            User question: {query}
            Provide a clear answer based only on the context above.
```

Changing **`model`** (and optionally **`provider`**) in the agent config switches the LLM used for that agent. The app uses the default Spring AI `ChatModel` bean (Ollama or OpenAI from `spring.ai.*`); for multiple models you can extend the code to select a model by name from a registry.

## API

| Method | Path | Description |
|--------|------|-------------|
| POST   | `/api/documents/seed`  | Ingest bundled open-source seed data from classpath |
| POST   | `/api/documents/ingest` | Upload a file (multipart); ingest and index chunks |
| POST   | `/api/query`           | RAG query: `{"query": "...", "topK": 10}` → `{ "answer", "sources" }` |

## Project Structure

```
InsightStream/
├── backend/                    # Spring Boot 3.x
│   ├── config/                 # GlobalParameters, TeamConfiguration, Chroma, HybridSearch
│   ├── document/               # Parsers (PDF, TXT), Chunker, IngestionService
│   ├── embedding/              # EmbeddingProvider, SpringAiEmbeddingProvider
│   ├── vector/                 # ChromaApiClient, Bm25Index, SemanticSearch, HybridSearch (RRF)
│   ├── agent/                  # ChatAgentRunner (config-driven prompts)
│   ├── rag/                    # RagQueryService (retrieve + generate)
│   └── api/                    # DocumentController, QueryController, CORS
├── frontend/                   # React + Vite + TypeScript + Tailwind + TanStack Query
│   └── src/
│       ├── api/                # client (queryRag, ingestDocument)
│       ├── components/        # QueryBox, ResponsePanel, UploadBox
│       └── App.tsx
└── README.md
```

## RBAC & Data Privacy (Future)

The current version focuses on RAG and config-driven agents. For production:

- Add authentication (e.g. OAuth2 / JWT) and **RBAC** so documents and queries are scoped by role.
- Ensure Chroma and any caches store only data the user is allowed to see.
- Optionally keep embeddings and vector search behind an internal API and never expose raw chunks to the client beyond what the answer cites.

## License

MIT (or your choice).
