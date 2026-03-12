import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import type { RagResponse } from '../api/client'
import { queryRag } from '../api/client'

type Props = { onResponse: (r: RagResponse) => void }

export function QueryBox({ onResponse }: Props) {
  const [text, setText] = useState('')

  const mutation = useMutation({
    mutationFn: (q: string) => queryRag(q, 10),
    onSuccess: onResponse,
    onError: (err: Error) => onResponse({ answer: `Error: ${err.message}`, sources: [] }),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = text.trim()
    if (!trimmed) return
    mutation.mutate(trimmed)
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3">
      <label htmlFor="query" className="text-sm font-medium text-slate-400">
        Ask a question over your documents
      </label>
      <div className="flex gap-2">
        <input
          id="query"
          type="text"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="e.g. What is our policy on remote work?"
          className="flex-1 rounded-lg border border-surface-border bg-surface-light px-4 py-3 text-slate-100 placeholder-slate-500 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          disabled={mutation.isPending}
        />
        <button
          type="submit"
          disabled={mutation.isPending || !text.trim()}
          className="rounded-lg bg-brand-600 px-5 py-3 font-medium text-white hover:bg-brand-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {mutation.isPending ? 'Searching…' : 'Ask'}
        </button>
      </div>
    </form>
  )
}
