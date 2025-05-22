package org.eclipse.lmos.routing.core.semantic

/**
 * The [EmbeddingRanker] is used by the [EmbeddingAgentClassifier] to evaluate a list of [Embedding]s
 * to determine the most qualified agent based on scores, thresholds, or other heuristics.
 *
 * The specific ranking algorithm depends on the concrete implementation
 * and may involve techniques such as maximum score selection, threshold filtering,
 * or composite scoring strategies.
 */
interface EmbeddingRanker {
    /**
     * Find the most qualified agent from a given set of [Embedding]s.
     *
     * The selection is typically based on the highest semantic similarity score
     * or a ranking strategy implemented by the classifier.
     *
     * @param embeddings A list of embeddings.
     * @return A [QualifiedAgent] containing the ID of the best-matching agent,
     *         or null if no qualified agent is found.
     */
    fun findQualifiedAgent(embeddings: List<Embedding>): QualifiedAgent
}

/**
 * Represents the ID of the most qualified agent from the [EmbeddingRanker].
 *
 * @property agentId ID of the best-matching agent.
 */
data class QualifiedAgent(
    val agentId: String?
)