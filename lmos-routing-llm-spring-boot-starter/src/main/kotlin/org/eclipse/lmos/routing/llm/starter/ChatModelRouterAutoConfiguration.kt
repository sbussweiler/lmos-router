package org.eclipse.lmos.routing.llm.starter

import LangChainClientProvider.LangChainChatModelFactory
import LangChainClientProvider.ModelClientProperties
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import org.eclipse.lmos.routing.core.llm.ChatModelRouter
import org.eclipse.lmos.routing.core.llm.RagChatModelRouter
import org.eclipse.lmos.routing.core.semantic.EMBEDDING_METADATA_CAPABILITY_EXAMPLE
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.llm.DefaultChatModelRouter
import org.eclipse.lmos.routing.llm.DefaultRagChatModelRouter
import org.eclipse.lmos.routing.llm.defaultSystemPromptWithRaq
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean


@EnableConfigurationProperties(
    EmbeddingStoreProperties::class,
    ChatModelProperties::class
)
open class ChatModelRouterAutoConfiguration {

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
    open fun llmRouter(
        chatModel: ChatLanguageModel,
        chatModelProperties: ChatModelProperties,
    ): ChatModelRouter = DefaultChatModelRouter.builder()
        .withChatModel(chatModel)
        .withSystemPrompt(chatModelProperties.systemPrompt)
        .withMaxMemoryMessages(chatModelProperties.maxChatHistory)
        .build()

    @Bean
    fun ragLlmRouter(
        chatModel: ChatLanguageModel,
        chatModelProperties: ChatModelProperties,
        embeddingModel: EmbeddingModel,
        embeddingStore: EmbeddingStore<TextSegment>
    ): RagChatModelRouter = DefaultRagChatModelRouter.builder()
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
