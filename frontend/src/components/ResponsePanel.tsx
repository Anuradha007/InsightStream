import type { RagResponse, RagSource } from '../api/client'

function SourceCard({ source }: { source: RagSource }) {
  return (
    <div className="rounded-lg border border-surface-border bg-surface-light/50 p-3 text-sm">
      <div className="font-medium text-brand-400 mb-1">{source.sourceName}</div>
      <p className="text-slate-400 line-clamp-3">{source.excerpt}</p>
    </div>
  )
}

type Props = { response: RagResponse | null }

export function ResponsePanel({ response }: Props) {
  const answer = response?.answer ?? null
  const sources = response?.sources ?? []

  if (response === null) {
    return (
      <div className="rounded-xl border border-surface-border bg-surface-light/30 p-8 text-center text-slate-500">
        Run a query to see the RAG answer and sources here.
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      <section>
        <h2 className="text-sm font-medium text-slate-400 mb-2">Answer</h2>
        <div className="rounded-xl border border-surface-border bg-surface-light/30 p-5 prose-answer">
          {answer}
        </div>
      </section>
      {sources.length > 0 && (
        <section>
          <h2 className="text-sm font-medium text-slate-400 mb-2">Sources</h2>
          <div className="grid gap-2 sm:grid-cols-2">
            {sources.map((s, i) => (
              <SourceCard key={`${s.documentId}-${i}`} source={s} />
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
