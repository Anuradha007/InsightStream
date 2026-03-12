package com.insightstream.vector;

import com.insightstream.document.DocumentChunk;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory BM25 index over document chunks using Apache Lucene.
 * Used for keyword-based retrieval in hybrid search.
 */
@Component
public class Bm25Index {

    private static final Logger log = LoggerFactory.getLogger(Bm25Index.class);

    private static final String FIELD_ID = "id";
    private static final String FIELD_TEXT = "text";

    private final Directory directory = new ByteBuffersDirectory();
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private IndexWriter writer;
    private IndexSearcher searcher;
    private DirectoryReader reader;

    public Bm25Index() {
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(directory, config);
            reader = DirectoryReader.open(writer);
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create BM25 index", e);
        }
    }

    public synchronized void addChunks(List<DocumentChunk> chunks) throws IOException {
        for (DocumentChunk c : chunks) {
            Document doc = new Document();
            doc.add(new StringField(FIELD_ID, c.getId(), Field.Store.YES));
            doc.add(new TextField(FIELD_TEXT, c.getText(), Field.Store.YES));
            writer.addDocument(doc);
        }
        writer.commit();
        refreshReader();
    }

    public synchronized void clear() throws IOException {
        writer.deleteAll();
        writer.commit();
        refreshReader();
    }

    /**
     * Search by query string; returns chunk ids and their BM25 scores (for RRF).
     */
    public List<Bm25Result> search(String queryString, int topK) {
        if (queryString == null || queryString.isBlank()) return List.of();
        try {
            QueryParser parser = new QueryParser(FIELD_TEXT, analyzer);
            parser.setDefaultOperator(QueryParser.Operator.OR);
            var query = parser.parse(QueryParser.escape(queryString));
            TopDocs topDocs = searcher.search(query, topK);
            List<Bm25Result> results = new ArrayList<>();
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                results.add(new Bm25Result(doc.get(FIELD_ID), sd.score, doc.get(FIELD_TEXT)));
            }
            return results;
        } catch (Exception e) {
            log.warn("BM25 search error: {}", e.getMessage());
            return List.of();
        }
    }

    private void refreshReader() throws IOException {
        DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
        if (newReader != null) {
            reader.close();
            reader = newReader;
            searcher = new IndexSearcher(reader);
        }
    }

    public record Bm25Result(String chunkId, float score, String text) {}
}
