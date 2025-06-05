package org.eclipse.lmos.routing.vector.starter

import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.routing.core.semantic.EmbeddingHandler
import org.eclipse.lmos.routing.core.starter.EmbeddingModelProperties
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.vector.handler.QdrantEmbeddingHandler
import org.eclipse.lmos.routing.vector.utils.EmbeddingModelClientProperties
import org.eclipse.lmos.routing.vector.utils.LangChainEmbeddingModelFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    EmbeddingModelProperties::class,
    EmbeddingStoreProperties::class,
)
@ConditionalOnProperty(
    prefix = "lmos.router.embedding",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
open class EmbeddingHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel::class)
    open fun embeddingModel(chatModelProperties: EmbeddingModelProperties): EmbeddingModel =
        LangChainEmbeddingModelFactory.createClient(
            EmbeddingModelClientProperties(
                provider = chatModelProperties.provider,
                modelName = chatModelProperties.modelName,
                apiKey = chatModelProperties.apiKey,
                baseUrl = chatModelProperties.baseUrl,
                modelPath = chatModelProperties.modelPath,
                tokenizerPath = chatModelProperties.tokenizerPath
            )
        )

    @Bean
    @ConditionalOnMissingBean(EmbeddingHandler::class)
    open fun embeddingHandler(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties,
    ): EmbeddingHandler = QdrantEmbeddingHandler.builder()
        .withEmbeddingModel(embeddingModel)
        .withQdrantHost(embeddingStoreProperties.host)
        .withQdrantPort(embeddingStoreProperties.port)
        .build()

}