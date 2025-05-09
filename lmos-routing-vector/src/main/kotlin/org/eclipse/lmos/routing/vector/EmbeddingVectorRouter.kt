package org.eclipse.lmos.routing.vector

import org.eclipse.lmos.routing.core.semantic.EmbeddingRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRanker
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.semantic.EmbeddingRoutingResult
import org.eclipse.lmos.routing.vector.ranker.EmbeddingScoreRanker
import org.eclipse.lmos.routing.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.routing.vector.utils.convertEmbeddingsToAgents


class EmbeddingVectorRouter(
    private val embeddingRetriever: EmbeddingRetriever,
    private val embeddingRanker: EmbeddingRanker
) : EmbeddingRouter {

    override fun resolveAgent(query: String, tenant: String): EmbeddingRoutingResult {
        val embeddings = embeddingRetriever.retrieve(tenant, query)
        val qualifiedAgent = embeddingRanker.findQualifiedAgent(embeddings)
        return EmbeddingRoutingResult(
            qualifiedAgent.agentId,
            embeddings.convertEmbeddingsToAgents(),
            qualifiedAgent.agentId != null
        )
    }

    companion object {
        fun builder(): VectorRouterBuilder {
            return VectorRouterBuilder()
        }
    }
}

class VectorRouterBuilder {
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var embeddingRankingThreshold: EmbeddingRankingThreshold = EmbeddingRankingThreshold()
    private var embeddingRanker: EmbeddingRanker = EmbeddingScoreRanker(embeddingRankingThreshold)

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) = apply {
        this.embeddingRetriever = embeddingRetriever
    }

    fun withEmbeddingRanker(embeddingRanker: EmbeddingRanker) = apply {
        this.embeddingRanker = embeddingRanker
    }

    fun build(): EmbeddingVectorRouter {
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")
        return EmbeddingVectorRouter(embeddingRetriever!!, embeddingRanker)
    }
}
