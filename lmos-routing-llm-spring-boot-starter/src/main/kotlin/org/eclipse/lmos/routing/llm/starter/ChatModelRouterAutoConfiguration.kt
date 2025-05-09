package org.eclipse.lmos.routing.llm.starter

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import org.eclipse.lmos.routing.core.llm.ChatModelRouter
import org.eclipse.lmos.routing.core.llm.RagChatModelRouter
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.routing.llm.DefaultChatModelRouter
import org.eclipse.lmos.routing.llm.DefaultRagChatModelRouter
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean


@EnableConfigurationProperties(
    EmbeddingStoreProperties::class
)
open class ChatModelRouterAutoConfiguration {

    @Bean
    open fun llmRouter(
        chatModel: ChatLanguageModel,
    ): ChatModelRouter = DefaultChatModelRouter.builder()
        .withChatModel(chatModel)
        // TODO: add max chat memory
        .build()

    @Bean
    fun ragLlmRouter(
        chatModel: ChatLanguageModel,
        embeddingModel: EmbeddingModel,
        embeddingStore: EmbeddingStore<TextSegment>
    ): RagChatModelRouter = DefaultRagChatModelRouter.builder()
        .withChatModel(chatModel)
        // TODO: add max chat memory
        .withEmbeddingModel(embeddingModel)
        .withEmbeddingStore(embeddingStore)
        .build()

    @Bean
    fun embeddingStore(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties
    ): EmbeddingStore<TextSegment> {
        // TODO: replace with EmbeddingRetriever to support dynamic collections
        val embeddingStore = QdrantEmbeddingStore.builder()
            .host(embeddingStoreProperties.host)
            .port(embeddingStoreProperties.port)
            .collectionName(embeddingStoreProperties.collection)
            .build()
        return embeddingStore
    }

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
