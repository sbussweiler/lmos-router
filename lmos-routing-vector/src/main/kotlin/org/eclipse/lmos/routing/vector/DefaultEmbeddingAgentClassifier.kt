package org.eclipse.lmos.routing.vector

import org.eclipse.lmos.routing.core.semantic.EmbeddingUserQuery
import org.eclipse.lmos.routing.core.semantic.EmbeddingAgentClassifier
import org.eclipse.lmos.routing.core.semantic.EmbeddingRanker
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.semantic.EmbeddingAgentClassification
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.utils.convertEmbeddingsToAgents
import org.slf4j.LoggerFactory


class DefaultEmbeddingAgentClassifier(
    private val embeddingRetriever: EmbeddingRetriever,
    private val embeddingRanker: EmbeddingRanker
) : EmbeddingAgentClassifier {

    private val logger = LoggerFactory.getLogger(EmbeddingAgentClassifier::class.java)

    override fun classify(query: EmbeddingUserQuery): EmbeddingAgentClassification {
        val embeddings = embeddingRetriever.retrieve(query.tenant, query.query)
        val qualifiedAgent = embeddingRanker.findQualifiedAgent(embeddings)
        logger.info("[EmbeddingAgentClassifier] Classified agent '${qualifiedAgent.agentId}' for query '${query.query}'.")
        return EmbeddingAgentClassification(
            qualifiedAgent.agentId,
            embeddings.convertEmbeddingsToAgents()
        )
    }

    companion object {
        fun builder(): EmbeddingAgentClassifierBuilder {
            return EmbeddingAgentClassifierBuilder()
        }
    }
}

class EmbeddingAgentClassifierBuilder {
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var embeddingRankingThreshold: EmbeddingRankingThreshold = EmbeddingRankingThreshold()
    private var embeddingRanker: EmbeddingRanker = EmbeddingScoreRanker(embeddingRankingThreshold)

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) = apply {
        this.embeddingRetriever = embeddingRetriever
    }

    fun withEmbeddingRanker(embeddingRanker: EmbeddingRanker) = apply {
        this.embeddingRanker = embeddingRanker
    }

    fun build(): DefaultEmbeddingAgentClassifier {
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")
        return DefaultEmbeddingAgentClassifier(embeddingRetriever!!, embeddingRanker)
    }
}
