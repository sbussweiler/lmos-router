package org.eclipse.lmos.router.config

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel
import dev.langchain4j.model.embedding.onnx.PoolingMode
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.RetrievalAugmentor
import dev.langchain4j.rag.content.injector.ContentInjector
import dev.langchain4j.rag.content.injector.DefaultContentInjector
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import org.eclipse.lmos.router.embeddings.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration


@Configuration
@EnableConfigurationProperties(
    EmbeddingStoreProperties::class,
    HuggingfaceEmbeddingModelProperties::class,
    LocalEmbeddingModelProperties::class,
    EmbeddingDocumentProperties::class
)
class RouterConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "lmos.router.embedding.model.local", name = ["enabled"], havingValue = "true")
    fun localEmbeddingModel(
        embeddingModelProperties: LocalEmbeddingModelProperties
    ): EmbeddingModel = OnnxEmbeddingModel(
        embeddingModelProperties.modelPath,
        embeddingModelProperties.tokenizerPath,
        PoolingMode.MEAN
    )

    @Bean
    @ConditionalOnProperty(prefix = "lmos.router.embedding.model.huggingface", name = ["enabled"], havingValue = "true")
    fun huggingfaceEmbeddingModel(
        embeddingModelProperties: HuggingfaceEmbeddingModelProperties
    ): EmbeddingModel = HuggingFaceEmbeddingModel.builder()
        .accessToken(embeddingModelProperties.apiKey)
        .modelId(embeddingModelProperties.modelName)
        .waitForModel(true)
        .timeout(Duration.ofSeconds(1000))
        .build()

    @Bean
    @ConditionalOnProperty(prefix = "lmos.router.embedding.store.in-memory", name = ["enabled"], havingValue = "false")
    fun embeddingStore(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties
    ): EmbeddingStore<TextSegment> {
        val embeddingStore = QdrantEmbeddingStore.builder()
            .host(embeddingStoreProperties.host)
            .port(embeddingStoreProperties.port)
            .collectionName(embeddingStoreProperties.collection)
            .build()
        createCollection(embeddingStoreProperties)
        return embeddingStore
    }

    @Bean
    @ConditionalOnProperty(prefix = "lmos.router.embedding.store.in-memory", name = ["enabled"], havingValue = "true")
    fun inMemoryEmbeddingStore(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties
    ): EmbeddingStore<TextSegment> = InMemoryEmbeddingStore()

    @Bean
    fun embeddingStoreIngestor(
        embeddingModel: EmbeddingModel,
        embeddingStore: EmbeddingStore<TextSegment>
    ): EmbeddingStoreIngestor = EmbeddingStoreIngestor.builder()
        .embeddingModel(embeddingModel)
        .embeddingStore(embeddingStore)
        .build()

    @Bean
    fun contentRetriever(
        embeddingModel: EmbeddingModel,
        embeddingStore: EmbeddingStore<TextSegment>
    ): ContentRetriever = EmbeddingStoreContentRetriever.builder()
        .embeddingModel(embeddingModel)
        .embeddingStore(embeddingStore)
        .maxResults(10)
        .build()

    @Bean
    fun contentInjector(): ContentInjector = DefaultContentInjector.builder()
        .metadataKeysToInclude(
            listOf(
                EMBEDDING_METADATA_AGENT_ID,
                EMBEDDING_METADATA_CAPABILITY_ID,
                EMBEDDING_METADATA_CAPABILITY_NAME,
                EMBEDDING_METADATA_CAPABILITY_VERSION,
                EMBEDDING_METADATA_CAPABILITY_DESCRIPTION
            )
        )
        .build()

    @Bean
    fun retrievalAugmentor(
        contentRetriever: ContentRetriever,
        contentInjector: ContentInjector
    ): RetrievalAugmentor = DefaultRetrievalAugmentor.builder()
        .contentRetriever(contentRetriever)
        .contentInjector(contentInjector)
        .build()

    @Bean
    fun chatMemoryProvider(): MessageWindowChatMemory = MessageWindowChatMemory.withMaxMessages(10)

    @Bean
    fun chatModelListener(): ChatModelListener = RouterChatModelListener()

    fun createCollection(embeddingStoreProperties: EmbeddingStoreProperties) {
        val client = QdrantClient(
            QdrantGrpcClient.newBuilder(
                embeddingStoreProperties.host,
                embeddingStoreProperties.port,
                false
            ).build()
        )
        try {
            client.getCollectionInfoAsync(embeddingStoreProperties.collection).get()
        } catch (e: Exception) {
            client.createCollectionAsync(
                embeddingStoreProperties.collection,
                VectorParams.newBuilder()
                    .setDistance(Distance.Cosine)
                    .setSize(1024)
                    .build()
            ).get()
        }
    }

}

class RouterChatModelListener : ChatModelListener {

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
