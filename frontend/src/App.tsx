import { useState } from 'react'
import type { RagResponse } from './api/client'
import { QueryBox } from './components/QueryBox'
import { UploadBox } from './components/UploadBox'
import { ResponsePanel } from './components/ResponsePanel'

export default function App() {
  const [activeTab, setActiveTab] = useState<'query' | 'upload'>('query')
  const [response, setResponse] = useState<RagResponse | null>(null)

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-surface-border bg-surface-light/80 backdrop-blur">
        <div className="max-w-4xl mx-auto px-4 py-4 flex items-center justify-between">
          <h1 className="text-xl font-semibold text-white tracking-tight">
            InsightStream
          </h1>
          <span className="text-slate-400 text-sm">Enterprise RAG</span>
        </div>
        <nav className="max-w-4xl mx-auto px-4 flex gap-1 border-t border-surface-border/50">
          <button
            type="button"
            onClick={() => setActiveTab('query')}
            className={`px-4 py-2.5 text-sm font-medium rounded-t transition-colors ${
              activeTab === 'query'
                ? 'bg-surface text-brand-400'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Query
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('upload')}
            className={`px-4 py-2.5 text-sm font-medium rounded-t transition-colors ${
              activeTab === 'upload'
                ? 'bg-surface text-brand-400'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Ingest
          </button>
        </nav>
      </header>

      <main className="flex-1 max-w-4xl w-full mx-auto px-4 py-6 flex flex-col gap-6">
        {activeTab === 'query' && (
          <>
            <QueryBox onResponse={setResponse} />
            <ResponsePanel response={response} />
          </>
        )}
        {activeTab === 'upload' && <UploadBox />}
      </main>
    </div>
  )
}
