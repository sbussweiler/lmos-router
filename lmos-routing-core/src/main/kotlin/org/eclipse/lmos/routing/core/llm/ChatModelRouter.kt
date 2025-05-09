package org.eclipse.lmos.routing.core.llm

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.lmos.routing.core.Router

/**
 * [ChatModelRouter] performs agent routing using an LLM.
 */
interface ChatModelRouter : Router {
    /**
     * Routes a query to the most suitable agent using an LLM.
     *
     * @param query The user input.
     * @param agents A list of agents to consider.
     * @param conversationId The conversation context ID.
     * @return A [ChatModelRoutingResult] containing the selected agent.
     */
    fun resolveAgent(query: String, agents: List<Agent>, conversationId: String): ChatModelRoutingResult
}

/**
 * [RagChatModelRouter] performs agent routing using an LLM + RAG
 */
interface RagChatModelRouter : Router {
    fun resolveAgent(query: String, tenant: String, conversationId: String): ChatModelRoutingResult
}

data class Agent @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("capabilities") val capabilities: Set<AgentCapability>,
)

data class AgentCapability @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("description") val description: String,
)

/**
 * [ChatModelRoutingResult] represents the routing result based on an LLM decision.
 */
open class ChatModelRoutingResult @JsonCreator constructor(
    @JsonProperty("agentId") var agentId: String?
)