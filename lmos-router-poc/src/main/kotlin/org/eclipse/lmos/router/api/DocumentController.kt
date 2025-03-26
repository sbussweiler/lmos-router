package org.eclipse.lmos.router.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.langchain4j.rag.content.retriever.ContentRetriever
import org.eclipse.lmos.router.embeddings.DocumentHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/embeddings", produces = [MediaType.APPLICATION_JSON_VALUE])
class DocumentController(
    private val documentReader: DocumentHandler,
    private val retriever: ContentRetriever
) {

    @PostMapping
    fun embedDocuments() {
        documentReader.embedDocuments()
    }

    @DeleteMapping
    fun deleteAllDocuments() {
        documentReader.deleteAllDocuments()
    }
}


data class DocumentPath @JsonCreator constructor(
    @JsonProperty("path") val path: String
)
