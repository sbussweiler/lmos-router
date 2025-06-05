package org.eclipse.lmos.routing.core.starter

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "lmos.router.embedding.model")
open class EmbeddingModelProperties(
    open val provider: String,
    open val modelName: String? = null,
    open val apiKey: String? = null,
    open val baseUrl: String? = null,
    open val modelPath: String? = null,
    open val tokenizerPath: String? = null,
)

@ConfigurationProperties(prefix = "lmos.router.embedding.store")
data class EmbeddingStoreProperties(
    val host: String,
    val port: Int,
    // TODO: only required for LLM-RAQ based approach, will be replaced with "dynamic collections".
    val collection: String = "telekom"
)

@ConfigurationProperties(prefix = "lmos.router.embedding.ranking")
data class EmbeddingRankingProperties(
    val maxEmbeddings: Int = 10,
    val minWeight: Double = 5.0,
    val minDistance: Double = 4.0,
    val minMeanScore: Double = 0.8,
    val minRealDistance: Double = 0.3
)
