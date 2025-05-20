package org.eclipse.lmos.routing.core.starter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.router.embedding.store")
data class EmbeddingStoreProperties(
    val host: String,
    val port: Int,
    // TODO: only required for LLM-RAQ based approach, will be replaced with "dynamic collections".
    val collection: String = "telekom"
)

@ConfigurationProperties(prefix = "lmos.router.embedding.ranking")
data class EmbeddingRankingProperties(
    val maxEmbeddings: Int,
    val minWeight: Double,
    val minDistance: Double,
    val minMeanScore: Double,
    val minRealDistance: Double
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
