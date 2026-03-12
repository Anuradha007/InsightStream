import { useState, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { ingestDocument, seedDatabase } from '../api/client'

export function UploadBox() {
  const [file, setFile] = useState<File | null>(null)
  const [drag, setDrag] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const ingestMutation = useMutation({
    mutationFn: (f: File) => ingestDocument(f),
    onSuccess: () => {
      setFile(null)
      if (inputRef.current) inputRef.current.value = ''
    },
  })

  const seedMutation = useMutation({
    mutationFn: seedDatabase,
  })

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setDrag(false)
    const f = e.dataTransfer.files[0]
    if (f) setFile(f)
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0]
    if (f) setFile(f)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (file) ingestMutation.mutate(file)
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="rounded-xl border border-surface-border bg-surface-light/30 p-4">
        <h3 className="text-sm font-medium text-slate-300 mb-2">Seed with open-source data</h3>
        <p className="text-slate-400 text-sm mb-3">
          Load bundled documents (public domain excerpts, sample policies) into the RAG store. Do this once to try queries without uploading files.
        </p>
        <button
          type="button"
          onClick={() => seedMutation.mutate()}
          disabled={seedMutation.isPending}
          className="rounded-lg bg-brand-600 px-4 py-2.5 font-medium text-white hover:bg-brand-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {seedMutation.isPending ? 'Seeding…' : 'Seed database'}
        </button>
        {seedMutation.isSuccess && (
          <p className="mt-3 text-emerald-400 text-sm">
            Ingested {seedMutation.data.filesIngested} files, {seedMutation.data.totalChunks} chunks total.
          </p>
        )}
        {seedMutation.isError && (
          <p className="mt-3 text-red-400 text-sm">
            {seedMutation.error instanceof Error ? seedMutation.error.message : 'Seed failed'}
          </p>
        )}
      </section>

      <section>
        <h3 className="text-sm font-medium text-slate-300 mb-2">Or upload your own files</h3>
      <p className="text-slate-400 text-sm mb-3">
        Upload PDF or TXT files. They will be chunked, embedded, and indexed for hybrid (BM25 + semantic) search.
      </p>
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div
          onDragOver={(e) => { e.preventDefault(); setDrag(true) }}
          onDragLeave={() => setDrag(false)}
          onDrop={handleDrop}
          className={`rounded-xl border-2 border-dashed p-8 text-center transition-colors ${
            drag ? 'border-brand-500 bg-brand-500/10' : 'border-surface-border bg-surface-light/30'
          }`}
        >
          <input
            ref={inputRef}
            type="file"
            accept=".pdf,.txt,application/pdf,text/plain"
            onChange={handleChange}
            className="hidden"
          />
          <button
            type="button"
            onClick={() => inputRef.current?.click()}
            className="text-brand-400 hover:text-brand-300 font-medium"
          >
            Choose file
          </button>
          {file && (
            <p className="mt-2 text-slate-300 text-sm">
              {file.name} ({(file.size / 1024).toFixed(1)} KB)
            </p>
          )}
        </div>
        <button
          type="submit"
          disabled={!file || ingestMutation.isPending}
          className="rounded-lg bg-brand-600 px-4 py-2.5 font-medium text-white hover:bg-brand-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors self-end"
        >
          {ingestMutation.isPending ? 'Ingesting…' : 'Ingest'}
        </button>
      </form>
      {ingestMutation.isSuccess && (
        <div className="rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 px-4 py-2 text-sm">
          Ingested {ingestMutation.data.chunksIngested} chunks from {ingestMutation.data.filename}.
        </div>
      )}
      {ingestMutation.isError && (
        <div className="rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 px-4 py-2 text-sm">
          {ingestMutation.error instanceof Error ? ingestMutation.error.message : 'Upload failed'}
        </div>
      )}
      </section>
    </div>
  )
}
