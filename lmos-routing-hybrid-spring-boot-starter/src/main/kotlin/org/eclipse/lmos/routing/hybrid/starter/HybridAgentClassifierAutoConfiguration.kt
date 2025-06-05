package org.eclipse.lmos.routing.hybrid.starter

import LangChainClientProvider.LangChainChatModelFactory
import LangChainClientProvider.ChatModelClientProperties
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.routing.core.hybrid.HybridAgentClassifier
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.starter.ChatModelProperties
import org.eclipse.lmos.routing.core.starter.EmbeddingModelProperties
import org.eclipse.lmos.routing.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.hybrid.DefaultHybridAgentClassifier
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.eclipse.lmos.routing.vector.retriever.QdrantEmbeddingRetriever
import org.eclipse.lmos.routing.vector.utils.EmbeddingModelClientProperties
import org.eclipse.lmos.routing.vector.utils.LangChainEmbeddingModelFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(
    ChatModelProperties::class,
    EmbeddingStoreProperties::class,
    EmbeddingModelProperties::class,
    EmbeddingRankingProperties::class
)
@ConditionalOnProperty(
    prefix = "lmos.router.classifier.hybrid",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
open class HybridAgentClassifierAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel::class)
    open fun embeddingModel(embeddingModelProperties: EmbeddingModelProperties): EmbeddingModel =
        LangChainEmbeddingModelFactory.createClient(
            EmbeddingModelClientProperties(
                provider = embeddingModelProperties.provider,
                modelName = embeddingModelProperties.modelName,
                apiKey = embeddingModelProperties.apiKey,
                baseUrl = embeddingModelProperties.baseUrl,
                modelPath = embeddingModelProperties.modelPath,
                tokenizerPath = embeddingModelProperties.tokenizerPath
            )
        )

    @Bean
    @ConditionalOnMissingBean(EmbeddingRetriever::class)
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

    @Bean
    @ConditionalOnMissingBean(ChatModel::class)
    open fun chatModel(chatModelProperties: ChatModelProperties): ChatModel =
        LangChainChatModelFactory.createClient(
            ChatModelClientProperties(
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
    @ConditionalOnMissingBean(HybridAgentClassifier::class)
    open fun hybridAgentClassifier(
        chatModel: ChatModel,
        chatModelProperties: ChatModelProperties,
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): HybridAgentClassifier = DefaultHybridAgentClassifier.builder()
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
                    minRealDistance = embeddingRankingProperties.minRealDistance,
                )
            )
        )
        .build()

}
