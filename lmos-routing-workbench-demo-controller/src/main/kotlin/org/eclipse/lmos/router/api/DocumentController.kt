package org.eclipse.lmos.router.api

import org.eclipse.lmos.router.embeddings.DocumentHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/embeddings", produces = [MediaType.APPLICATION_JSON_VALUE])
class DocumentController(
    private val documentHandler: DocumentHandler
) {

    @PostMapping
    fun ingestDocuments(@RequestParam(defaultValue = "telekom") tenant: String) {
        documentHandler.ingestDocuments(tenant)
    }

    @DeleteMapping
    fun removeDocuments(@RequestParam(defaultValue = "telekom") tenant: String) {
        documentHandler.removeDocuments(tenant)
    }

}
