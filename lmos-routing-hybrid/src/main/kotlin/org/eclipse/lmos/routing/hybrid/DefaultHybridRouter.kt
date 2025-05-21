package org.eclipse.lmos.routing.hybrid

import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.routing.core.ChatModelRoutingRequest
import org.eclipse.lmos.routing.core.EmbeddingChatModelRoutingRequest
import org.eclipse.lmos.routing.core.EmbeddingRoutingRequest
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

    override fun resolveAgent(routingRequest: EmbeddingChatModelRoutingRequest): EmbeddingRoutingResult {
        logger.info("HybridRouter trying to find agent for query '${routingRequest.query}'")
        val semanticRoutingResult = embeddingRouter.resolveAgent(EmbeddingRoutingRequest(routingRequest.query, routingRequest.tenant))
        if (semanticRoutingResult.agentId.isNullOrEmpty()) {
            logger.info("HybridRouter can not find agent using vector search, going to ask LLM for query '${routingRequest.query}'")
            val llmRoutingResult = llmRouter.resolveAgent(
                ChatModelRoutingRequest(
                    routingRequest.query,
                    semanticRoutingResult.consideredAgents,
                    routingRequest.conversationId
                )
            )
            return EmbeddingRoutingResult(
                agentId = llmRoutingResult.agentId,
                consideredAgents = semanticRoutingResult.consideredAgents
            )
        }
        logger.info("HybridRouter found agent for '${routingRequest.query}': $semanticRoutingResult")
        return semanticRoutingResult
    }

    companion object {
        fun builder(): HybridRouterBuilder {
            return HybridRouterBuilder()
        }
    }

}

class HybridRouterBuilder {
    private var llm: ChatModel? = null
    private var llmSystemPrompt: String = defaultSystemPrompt()
    private var llmMaxChatMemory: Int = 10
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var embeddingRanker: EmbeddingRanker = EmbeddingScoreRanker(EmbeddingRankingThreshold())

    fun withChatModel(model: ChatModel) = apply {
        this.llm = model
    }

    fun withSystemPrompt(systemPrompt: String) = apply {
        this.llmSystemPrompt = systemPrompt
    }

    fun withMaxMemoryMessages(llmMaxChatMemory: Int) = apply {
        this.llmMaxChatMemory = llmMaxChatMemory
    }

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) = apply {
        this.embeddingRetriever = embeddingRetriever
    }

    fun withEmbeddingRanker(embeddingRanker: EmbeddingRanker) = apply {
        this.embeddingRanker = embeddingRanker
    }

    fun build(): DefaultHybridRouter {
        if (llm == null) throw IllegalStateException("ChatModel must be set")
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
