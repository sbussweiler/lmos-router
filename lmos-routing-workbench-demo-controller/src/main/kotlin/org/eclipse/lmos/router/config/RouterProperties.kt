package org.eclipse.lmos.router.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.router.embedding.model")
data class EmbeddingDocumentProperties(
    val documentPath: String,
)


