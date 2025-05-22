package org.eclipse.lmos.routing.vector.starter

import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.routing.core.semantic.EmbeddingAgentClassifier
import org.eclipse.lmos.routing.core.semantic.EmbeddingHandler
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.vector.DefaultEmbeddingAgentClassifier
import org.eclipse.lmos.routing.vector.handler.QdrantEmbeddingHandler
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(
    EmbeddingRankingProperties::class
)
open class EmbeddingAgentClassifierAutoConfiguration {

    @Bean
    open fun embeddingHandler(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties,
    ): EmbeddingHandler = QdrantEmbeddingHandler.builder()
        .withEmbeddingModel(embeddingModel)
        .withQdrantHost(embeddingStoreProperties.host)
        .withQdrantPort(embeddingStoreProperties.port)
        .build()

    @Bean
    open fun embeddingAgentClassifier(
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): EmbeddingAgentClassifier = DefaultEmbeddingAgentClassifier.builder()
        .withEmbeddingRetriever(embeddingRetriever)
        .withEmbeddingRanker(
            EmbeddingScoreRanker(
                EmbeddingRankingThreshold(
                    minWeight = embeddingRankingProperties.minWeight,
                    minDistance = embeddingRankingProperties.minDistance,
                    minMeanScore = embeddingRankingProperties.minMeanScore,
                    minRelDistance = embeddingRankingProperties.minRealDistance,
                )
            )
        )
        .build()

}
