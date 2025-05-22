package org.eclipse.lmos.routing.core.llm

import org.eclipse.lmos.routing.core.AgentClassification
import org.eclipse.lmos.routing.core.hybrid.HybridUserQuery
import org.eclipse.lmos.routing.core.AgentClassifier
import org.eclipse.lmos.routing.core.UserQuery

/**
 * The [ModelAgentClassifier] classifies the given [ModelUserQuery] using a large language model (LLM).
 */
interface ModelAgentClassifier : AgentClassifier<ModelUserQuery, ModelAgentClassification> {
    /**
     * Classifies the given query using an LLM.
     *
     * @param query The user query to classify.
     * @return The classification result selected by the model.
     */
    override fun classify(query: ModelUserQuery): ModelAgentClassification
}

/**
 * Input for LLM-based classifiers.
 *
 * @property query The user's message.
 * @property agents A list of available agents to select from.
 * @property conversationId An identifier for the conversation history.
 */
data class ModelUserQuery(
    override val query: String,
    val agents: List<Agent>,
    val conversationId: String,
) : UserQuery

/**
 * Output of an LLM-based agent classification.
 *
 * @property agentId The ID of the selected agent.
 */
data class ModelAgentClassification(
    override var agentId: String?
) : AgentClassification

/**
 * Represents an agent with a unique identifier and a set of capabilities.
 *
 * @property id The unique identifier of the agent.
 * @property capabilities The list of capabilities.
 */
data class Agent(
    val id: String,
    val capabilities: List<Capability>,
)

/**
 * Represents a specific capability of an agent.
 *
 * @property id The unique identifier of the capability.
 * @property description A textual description of what the capability entails.
 * @property examples Examples illustrating the capability.
 */
data class Capability(
    val id: String,
    val description: String,
    val examples: List<String> = emptyList(),
)

/**
 * The [ModelRagAgentClassifier] enhances LLM-based classification by incorporating retrieval-augmented generation (RAG).
 *
 * It supplements the query with retrieved knowledge before invoking the language model for agent selection.
 */
interface ModelRagAgentClassifier : AgentClassifier<HybridUserQuery, ModelAgentClassification> {
    /**
     * Classifies the given query using an LLM with RAQ.
     *
     * @param query The user query to classify.
     * @return The classification result selected by the model with RAG assistance.
     */
    override fun classify(query: HybridUserQuery): ModelAgentClassification
}