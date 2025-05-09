package org.eclipse.lmos.routing.vector.starter

import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.routing.vector.EmbeddingVectorRouter
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(
    EmbeddingRankingProperties::class
)
open class EmbeddingRouterAutoConfiguration {

    @Bean
    open fun vectorRouter(
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): EmbeddingVectorRouter = EmbeddingVectorRouter.builder()
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
