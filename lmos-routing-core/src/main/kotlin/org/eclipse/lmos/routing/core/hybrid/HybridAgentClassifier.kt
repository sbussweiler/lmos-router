package org.eclipse.lmos.routing.core.hybrid

import org.eclipse.lmos.routing.core.AgentClassification
import org.eclipse.lmos.routing.core.AgentClassifier
import org.eclipse.lmos.routing.core.UserQuery
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.semantic.EmbeddingAgentClassification
import org.eclipse.lmos.routing.core.semantic.EmbeddingAgentClassifier
import org.eclipse.lmos.routing.core.llm.ModelAgentClassifier

/**
 * The [HybridAgentClassifier] combines [EmbeddingAgentClassifier] and [ModelAgentClassifier] for classification.
 *
 * Attempts to find an agent using semantic search and falls back to LLM-based classification if no match is found.
 */
interface HybridAgentClassifier : AgentClassifier<HybridUserQuery, HybridAgentClassification> {
    /**
     * Classifies the given query using a hybrid approach (semantic search + LLM fallback).
     *
     * @param query The user query to classify.
     * @return The classification result selected by the semantic search or the model.
     */
    override fun classify(query: HybridUserQuery): HybridAgentClassification
}

/**
 * Input for hybrid classification combining tenant context and conversation information.
 *
 * @property query The user's message.
 * @property tenant Identifier for the tenant or domain scope.
 * @property conversationId An identifier for the conversation history, in case of LLM fallback.
 */
data class HybridUserQuery(
    override val query: String,
    val tenant: String,
    val conversationId: String,
) : UserQuery

/**
 * Output of hybrid classification.
 *
 * @property agentId The ID of the selected agent.
 * @property consideredAgents The agents that were found during the vector search, and handed over to the LLM.
 * @property foundBySemanticSearch Whether the result came from semantic search (true) or LLM (false).
 */
class HybridAgentClassification(
    override var agentId: String?,
    val consideredAgents: List<Agent>,
    val foundBySemanticSearch: Boolean = false
) : AgentClassification
