package org.eclipse.lmos.routing.hybrid

import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.routing.core.llm.ModelUserQuery
import org.eclipse.lmos.routing.core.hybrid.HybridUserQuery
import org.eclipse.lmos.routing.core.semantic.EmbeddingUserQuery
import org.eclipse.lmos.routing.core.hybrid.HybridAgentClassifier
import org.eclipse.lmos.routing.core.hybrid.HybridAgentClassification
import org.eclipse.lmos.routing.core.llm.ModelAgentClassifier
import org.eclipse.lmos.routing.core.semantic.EmbeddingRanker
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.semantic.EmbeddingAgentClassifier
import org.eclipse.lmos.routing.llm.DefaultModelAgentClassifier
import org.eclipse.lmos.routing.llm.defaultSystemPrompt
import org.eclipse.lmos.routing.vector.DefaultEmbeddingAgentClassifier
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.slf4j.LoggerFactory

class DefaultHybridAgentClassifier(
    private val embeddingAgentClassifier: EmbeddingAgentClassifier,
    private val modelAgentClassifier: ModelAgentClassifier,
) : HybridAgentClassifier {

    private val logger = LoggerFactory.getLogger(DefaultHybridAgentClassifier::class.java)

    override fun classify(query: HybridUserQuery): HybridAgentClassification {
        logger.info("Trying to find agent for query '${query.query}'")
        val agentClassification = embeddingAgentClassifier.classify(EmbeddingUserQuery(query.query, query.tenant))
        if (agentClassification.agentId.isNullOrEmpty()) {
            logger.info("Can not find agent using vector search, going to ask LLM for query '${query.query}'")
            val llmRoutingResult = modelAgentClassifier.classify(
                ModelUserQuery(
                    query.query,
                    agentClassification.consideredAgents,
                    query.conversationId
                )
            )
            return HybridAgentClassification(
                agentId = llmRoutingResult.agentId,
                consideredAgents = agentClassification.consideredAgents,
                foundBySemanticSearch = false
            )
        }
        logger.info("Found agent for '${query.query}': $agentClassification")
        return HybridAgentClassification(
            agentId = agentClassification.agentId,
            consideredAgents = agentClassification.consideredAgents,
            foundBySemanticSearch = true
        )
    }

    companion object {
        fun builder(): HybridAgentClassifierBuilder {
            return HybridAgentClassifierBuilder()
        }
    }

}

class HybridAgentClassifierBuilder {
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

    fun build(): DefaultHybridAgentClassifier {
        if (llm == null) throw IllegalStateException("ChatModel must be set")
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")

        val embeddingClassifier = DefaultEmbeddingAgentClassifier.builder()
            .withEmbeddingRetriever(embeddingRetriever!!)
            .withEmbeddingRanker(embeddingRanker)
            .build()

        val modelClassifier = DefaultModelAgentClassifier.builder()
            .withChatModel(llm!!)
            .withSystemPrompt(llmSystemPrompt)
            .withMaxMemoryMessages(llmMaxChatMemory)
            .build()

        return DefaultHybridAgentClassifier(embeddingClassifier, modelClassifier)
    }

}
