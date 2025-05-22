package org.eclipse.lmos.routing.llm.starter

import LangChainClientProvider.LangChainChatModelFactory
import LangChainClientProvider.ModelClientProperties
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import org.eclipse.lmos.routing.core.llm.ModelAgentClassifier
import org.eclipse.lmos.routing.core.llm.ModelRagAgentClassifier
import org.eclipse.lmos.routing.core.semantic.EMBEDDING_METADATA_CAPABILITY_EXAMPLE
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.llm.DefaultModelAgentClassifier
import org.eclipse.lmos.routing.llm.DefaultModelRagAgentClassifier
import org.eclipse.lmos.routing.llm.defaultSystemPromptWithRaq
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean


@EnableConfigurationProperties(
    EmbeddingStoreProperties::class,
    ChatModelProperties::class
)
open class ModelAgentClassifierAutoConfiguration {

    @Bean
    open fun chatModel(chatModelProperties: ChatModelProperties): ChatModel =
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
    open fun modelAgentClassifier(
        chatModel: ChatModel,
        chatModelProperties: ChatModelProperties,
    ): ModelAgentClassifier = DefaultModelAgentClassifier.builder()
        .withChatModel(chatModel)
        .withSystemPrompt(chatModelProperties.systemPrompt)
        .withMaxMemoryMessages(chatModelProperties.maxChatHistory)
        .build()

    @Bean
    fun modelRagAgentClassifier(
        chatModel: ChatModel,
        chatModelProperties: ChatModelProperties,
        embeddingModel: EmbeddingModel,
        embeddingStore: EmbeddingStore<TextSegment>
    ): ModelRagAgentClassifier = DefaultModelRagAgentClassifier.builder()
        .withChatModel(chatModel)
        .withSystemPrompt(defaultSystemPromptWithRaq())
        .withMaxMemoryMessages(chatModelProperties.maxChatHistory)
        .withEmbeddingModel(embeddingModel)
        .withEmbeddingStore(embeddingStore)
        .build()

    @Bean
    fun embeddingStore(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties
    ): EmbeddingStore<TextSegment> {
        // TODO: only required for LLM-RAQ based approach, must be replaced with "dynamic collections".
        val embeddingStore = QdrantEmbeddingStore.builder()
            .host(embeddingStoreProperties.host)
            .port(embeddingStoreProperties.port)
            .collectionName(embeddingStoreProperties.collection)
            .payloadTextKey(EMBEDDING_METADATA_CAPABILITY_EXAMPLE)
            .build()
        return embeddingStore
    }

}
