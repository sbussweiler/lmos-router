package org.eclipse.lmos.routing.hybrid.starter

import LangChainClientProvider.LangChainChatModelFactory
import LangChainClientProvider.ModelClientProperties
import dev.langchain4j.model.chat.ChatLanguageModel
import org.eclipse.lmos.routing.core.hybrid.HybridRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.routing.hybrid.DefaultHybridRouter
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(
    ChatModelProperties::class,
    EmbeddingRankingProperties::class
)
open class HybridRouterAutoConfiguration {

    @Bean
    open fun chatModel(chatModelProperties: ChatModelProperties): ChatLanguageModel =
        LangChainChatModelFactory.createClient(
            ModelClientProperties(
                provider = chatModelProperties.provider,
                apiKey = chatModelProperties.apiKey,
                baseUrl = chatModelProperties.baseUrl,
                model = chatModelProperties.model,
                maxTokens = chatModelProperties.maxTokens,
                temperature = chatModelProperties.temperature,
                logRequestsAndResponses = chatModelProperties.logRequestsAndResponses,
            )
        )

    @Bean
    open fun hybridRouter(
        chatModel: ChatLanguageModel,
        chatModelProperties: ChatModelProperties,
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): HybridRouter = DefaultHybridRouter.builder()
        .withChatModel(chatModel)
        .withSystemPrompt(chatModelProperties.systemPrompt)
        .withMaxMemoryMessages(chatModelProperties.maxChatHistory)
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
