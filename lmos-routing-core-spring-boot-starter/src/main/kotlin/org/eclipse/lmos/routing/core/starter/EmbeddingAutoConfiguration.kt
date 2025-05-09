package org.eclipse.lmos.routing.core.starter

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel
import dev.langchain4j.model.embedding.onnx.PoolingMode
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.vector.retriever.QdrantEmbeddingRetriever
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableConfigurationProperties(
    EmbeddingStoreProperties::class,
    HuggingfaceEmbeddingModelProperties::class,
    LocalEmbeddingModelProperties::class,
    EmbeddingRankingProperties::class
)
open class EmbeddingAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "lmos.router.embedding.model.local", name = ["enabled"], havingValue = "true")
    open fun localEmbeddingModel(
        embeddingModelProperties: LocalEmbeddingModelProperties
    ): EmbeddingModel = OnnxEmbeddingModel(
        embeddingModelProperties.modelPath,
        embeddingModelProperties.tokenizerPath,
        PoolingMode.MEAN
    )

    @Bean
    @ConditionalOnProperty(prefix = "lmos.router.embedding.model.huggingface", name = ["enabled"], havingValue = "true")
    open fun huggingfaceEmbeddingModel(
        embeddingModelProperties: HuggingfaceEmbeddingModelProperties
    ): EmbeddingModel = HuggingFaceEmbeddingModel.builder()
        .accessToken(embeddingModelProperties.apiKey)
        .modelId(embeddingModelProperties.modelName)
        .waitForModel(true)
        .timeout(Duration.ofSeconds(1000))
        .build()

    @Bean
    open fun embeddingRetriever(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties,
        embeddingRankingProperties: EmbeddingRankingProperties
    ): EmbeddingRetriever = QdrantEmbeddingRetriever.builder()
        .withQdrantHost(embeddingStoreProperties.host)
        .withQdrantPort(embeddingStoreProperties.port)
        .withEmbeddingModel(embeddingModel)
        .withEmbeddingMaxResults(embeddingRankingProperties.maxEmbeddings)
        .build()

}