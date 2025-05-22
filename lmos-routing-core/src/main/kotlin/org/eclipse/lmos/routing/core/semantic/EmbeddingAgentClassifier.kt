package org.eclipse.lmos.routing.core.semantic

import org.eclipse.lmos.routing.core.AgentClassification
import org.eclipse.lmos.routing.core.AgentClassifier
import org.eclipse.lmos.routing.core.UserQuery
import org.eclipse.lmos.routing.core.llm.Agent

/**
 * The [EmbeddingAgentClassifier] classifies the given [EmbeddingUserQuery] based on semantic similarity using vector
 * search. It Uses an [EmbeddingRanker] to finde the most relevant agent from a set of candidate vectors.
 */
interface EmbeddingAgentClassifier : AgentClassifier<EmbeddingUserQuery, EmbeddingAgentClassification> {
    /**
     * Classifies the given query using semantic search.
     *
     ** @param query The user query to classify.
     *  @return The classification result selected by the semantic search.
     */
    override fun classify(query: EmbeddingUserQuery): EmbeddingAgentClassification
}

/**
 * Input query for embedding-based classification.
 *
 * @property query The user's message.
 * @property tenant Identifier for the tenant or domain scope.
 */
data class EmbeddingUserQuery(
    override val query: String,
    val tenant: String,
) : UserQuery

/**
 * Output of a vector-based agent classification.
 *
 * @property agentId The ID of the selected agent.
 * @property consideredAgents The agents that were found during the vector search, and evaluated during the ranking process.
 */
class EmbeddingAgentClassification(
    override var agentId: String?,
    val consideredAgents: List<Agent>,
) : AgentClassification
