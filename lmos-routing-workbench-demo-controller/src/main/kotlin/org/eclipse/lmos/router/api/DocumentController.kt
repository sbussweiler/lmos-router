package org.eclipse.lmos.router.api

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import org.eclipse.lmos.router.embeddings.DocumentHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/embeddings", produces = [MediaType.APPLICATION_JSON_VALUE])
class DocumentController(
    private val documentReader: DocumentHandler,
    private val embeddingStore: EmbeddingStore<TextSegment>
) {

    @PostMapping
    fun embedDocuments(@RequestParam(defaultValue = "telekom") tenant: String) {
        documentReader.embedDocuments(tenant)
    }

    @DeleteMapping
    fun deleteAllDocuments() {
        embeddingStore.removeAll()
    }

}
