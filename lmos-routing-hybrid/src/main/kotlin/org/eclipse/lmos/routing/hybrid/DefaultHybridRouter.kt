package org.eclipse.lmos.routing.hybrid

import dev.langchain4j.model.chat.ChatLanguageModel
import org.eclipse.lmos.routing.core.hybrid.HybridRouter
import org.eclipse.lmos.routing.core.llm.ChatModelRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRanker
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.semantic.EmbeddingRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRoutingResult
import org.eclipse.lmos.routing.llm.DefaultChatModelRouter
import org.eclipse.lmos.routing.llm.defaultSystemPrompt
import org.eclipse.lmos.routing.vector.EmbeddingVectorRouter
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.slf4j.LoggerFactory

class DefaultHybridRouter(
    private val embeddingRouter: EmbeddingRouter,
    private val llmRouter: ChatModelRouter,
) : HybridRouter {

    private val logger = LoggerFactory.getLogger(DefaultHybridRouter::class.java)

    override fun resolveAgent(query: String, tenant: String, conversationId: String): EmbeddingRoutingResult {
        logger.info("HybridRouter trying to find agent for query '$query'")
        val semanticRoutingResult = embeddingRouter.resolveAgent(query, tenant)
        if (semanticRoutingResult.agentId.isNullOrEmpty()) {
            logger.info("HybridRouter can not find agent using vector search, going to ask LLM for query '$query'")
            val llmRoutingResult = llmRouter.resolveAgent(query, semanticRoutingResult.consideredAgents, conversationId)
            return EmbeddingRoutingResult(
                agentId = llmRoutingResult.agentId,
                consideredAgents = semanticRoutingResult.consideredAgents
            )
        }
        logger.info("HybridRouter found agent for '$query': $semanticRoutingResult")
        return semanticRoutingResult
    }

    companion object {
        fun builder(): HybridRouterBuilder {
            return HybridRouterBuilder()
        }
    }

}

class HybridRouterBuilder {
    private var llm: ChatLanguageModel? = null
    private var llmSystemPrompt: String = defaultSystemPrompt()
    private var llmMaxChatMemory: Int = 10
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var embeddingRanker: EmbeddingRanker = EmbeddingScoreRanker(EmbeddingRankingThreshold())

    fun withChatModel(model: ChatLanguageModel) = apply {
        this.llm = model
    }

    fun withLlmSystemPrompt(systemPrompt: String) = apply {
        this.llmSystemPrompt = systemPrompt
    }

    fun withLlmMaxChatMemory(llmMaxChatMemory: Int) = apply {
        this.llmMaxChatMemory = llmMaxChatMemory
    }

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) = apply {
        this.embeddingRetriever = embeddingRetriever
    }

    fun withEmbeddingRanker(embeddingRanker: EmbeddingRanker) = apply {
        this.embeddingRanker = embeddingRanker
    }

    fun build(): DefaultHybridRouter {
        if (llm == null) throw IllegalStateException("ChatLanguageModel must be set")
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")

        val vectorRouter = EmbeddingVectorRouter.builder()
            .withEmbeddingRetriever(embeddingRetriever!!)
            .withEmbeddingRanker(embeddingRanker)
            .build()

        val llmRouter = DefaultChatModelRouter.builder()
            .withChatModel(llm!!)
            .withSystemPrompt(llmSystemPrompt)
            .withMaxMemoryMessages(llmMaxChatMemory)
            .build()

        return DefaultHybridRouter(vectorRouter, llmRouter)
    }

}
