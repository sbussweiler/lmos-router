package org.eclipse.lmos.routing.hybrid.starter

import ChatModelProperties
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import org.eclipse.lmos.routing.core.hybrid.HybridRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.routing.hybrid.DefaultHybridRouter
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.slf4j.LoggerFactory
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
    open fun hybridRouter(
        chatModel: ChatLanguageModel,
        chatModelProperties: ChatModelProperties,
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): HybridRouter = DefaultHybridRouter.builder()
        .withChatModel(chatModel)
        .withLlmSystemPrompt(chatModelProperties.systemPrompt)
        .withLlmMaxChatMemory(chatModelProperties.maxChatHistory)
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

    @Bean
    open fun chatModelConversationListener(): ChatModelListener = ModelConversationLogger()

}

class ModelConversationLogger : ChatModelListener {

    private val logger = LoggerFactory.getLogger(ChatModelListener::class.java)

    override fun onRequest(requestContext: ChatModelRequestContext) {
        logger.info("onRequest(): {}", requestContext.chatRequest())
    }

    override fun onResponse(responseContext: ChatModelResponseContext) {
        logger.info("onResponse(): {}", responseContext.chatResponse())
    }

    override fun onError(errorContext: ChatModelErrorContext) {
        logger.info("onError(): {}", errorContext.error().message)
    }
}
