package org.eclipse.lmos.router.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.router.embedding.store")
data class EmbeddingStoreProperties(
    val host: String,
    val port: Int,
    val collection: String
)

@ConfigurationProperties(prefix = "lmos.router.embedding.model.local")
data class LocalEmbeddingModelProperties(
    val enabled: Boolean,
    val modelPath: String,
    val tokenizerPath: String
)

@ConfigurationProperties(prefix = "lmos.router.embedding.model.huggingface")
data class HuggingfaceEmbeddingModelProperties(
    val enabled: Boolean,
    val modelName: String,
    val apiKey: String
)

@ConfigurationProperties(prefix = "lmos.router.embedding.model")
data class EmbeddingDocumentProperties(
    val documentPath: String,
)


