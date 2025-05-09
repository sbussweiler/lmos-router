package org.eclipse.lmos.routing.supervisor

import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.routing.core.semantic.EmbeddingInjector
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.vector.injector.QdrantEmbeddingInjector
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties
class EmbeddingInjectorConfiguration {

    @Bean
    fun embeddingInjector(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties,
    ): EmbeddingInjector = QdrantEmbeddingInjector.builder()
        .withEmbeddingModel(embeddingModel)
        .withQdrantHost(embeddingStoreProperties.host)
        .withQdrantPort(embeddingStoreProperties.port)
        .build()

}
