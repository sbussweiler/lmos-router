package org.eclipse.lmos.routing.llm

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.injector.DefaultContentInjector
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import dev.langchain4j.store.embedding.EmbeddingStore
import org.eclipse.lmos.routing.core.EmbeddingChatModelRoutingRequest
import org.eclipse.lmos.routing.core.llm.RagChatModelRouter
import org.eclipse.lmos.routing.core.semantic.EMBEDDING_METADATA_AGENT_ID
import org.eclipse.lmos.routing.core.semantic.EMBEDDING_METADATA_CAPABILITY_DESCRIPTION
import org.eclipse.lmos.routing.core.llm.ChatModelRoutingResult

class DefaultRagChatModelRouter(
    private val langchainAIService: LangchainRagLlmAgentResolver
) : RagChatModelRouter {

    override fun resolveAgent(routingRequest: EmbeddingChatModelRoutingRequest):ChatModelRoutingResult {
        return langchainAIService.resolveAgent(routingRequest.query, routingRequest.conversationId)
    }

    companion object {
        fun builder(): DefaultRagChatModelRouterBuilder {
            return DefaultRagChatModelRouterBuilder()
        }
    }
}

interface LangchainRagLlmAgentResolver {

    fun resolveAgent(@UserMessage query: String, @MemoryId conversationId: String, ): ChatModelRoutingResult

}

class DefaultRagChatModelRouterBuilder {
    private var llm: ChatModel? = null
    private var systemPrompt: String = defaultSystemPromptWithRaq()
    private var embeddingModel: EmbeddingModel? = null
    private var embeddingStore: EmbeddingStore<TextSegment>? = null
    private var maxEmbeddingResults: Int = 10
    private var maxMemoryMessages: Int = 10

    fun withChatModel(model: ChatModel) = apply {
        this.llm = model
    }

    fun withSystemPrompt(systemPrompt: String) = apply {
        this.systemPrompt = systemPrompt
    }

    fun withEmbeddingModel(embeddingModel: EmbeddingModel) = apply {
        this.embeddingModel = embeddingModel
    }

    fun withEmbeddingStore(embeddingStore: EmbeddingStore<TextSegment>) = apply {
        this.embeddingStore = embeddingStore
    }

    fun withMaxEmbeddingResults(maxResults: Int) = apply {
        this.maxEmbeddingResults = maxResults
    }

    fun withMaxMemoryMessages(maxMessages: Int) = apply {
        this.maxMemoryMessages = maxMessages
    }

    fun build(): DefaultRagChatModelRouter {
        if (llm == null) throw IllegalStateException("ChatModel must be set")
        if (embeddingModel == null) throw IllegalStateException("EmbeddingModel must be set")
        if (embeddingStore == null) throw IllegalStateException("EmbeddingStore must be set")

        val agentContentInjector = AgentContentInjector(
            listOf(
                EMBEDDING_METADATA_AGENT_ID,
                EMBEDDING_METADATA_CAPABILITY_DESCRIPTION
            )
        )

        val contentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .maxResults(maxEmbeddingResults)
            .build()

        val retrievalAugmentor = DefaultRetrievalAugmentor.builder()
            .contentRetriever(contentRetriever)
            .contentInjector(agentContentInjector)
            .build()

        val langchain4jRouter =  AiServices.builder(LangchainRagLlmAgentResolver::class.java)
            .chatModel(llm)
            .retrievalAugmentor(retrievalAugmentor)
            .systemMessageProvider { systemPrompt }
            .chatMemoryProvider { MessageWindowChatMemory.withMaxMessages(maxMemoryMessages) }
            .build()

        return DefaultRagChatModelRouter(langchain4jRouter)
    }

}


class AgentContentInjector(metadataKeysToInclude: List<String>) : DefaultContentInjector(metadataKeysToInclude) {

    override fun format(contents: MutableList<Content>): String {
        val agentCapabilities = mutableMapOf<String, MutableSet<String>>()

        contents.forEach {
            val metadata = it.textSegment().metadata()
            val agentId = metadata.getString(EMBEDDING_METADATA_AGENT_ID)
            val capabilityDescription = metadata.getString(EMBEDDING_METADATA_CAPABILITY_DESCRIPTION)

            if (agentId != null && capabilityDescription != null) {
                val descriptions = agentCapabilities.getOrDefault(agentId, mutableSetOf())
                descriptions.add(capabilityDescription)
                agentCapabilities[agentId] = descriptions
            }
        }

        return format(agentCapabilities)
    }

    private fun format(agentCapabilities: Map<String, MutableSet<String>>) =
        agentCapabilities.map {
            String.format(
                "Agent '%s':\n\t- %s",
                it.key,
                it.value.joinToString("\n\t- ")
            )
        }.joinToString("\n\n")
}

