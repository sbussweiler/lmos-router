package org.eclipse.lmos.routing.core.semantic

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.lmos.routing.core.EmbeddingRoutingRequest
import org.eclipse.lmos.routing.core.Router
import org.eclipse.lmos.routing.core.llm.Agent

/**
 * [EmbeddingRouter] performs agent routing using semantic vector search.
 */
interface EmbeddingRouter : Router {
    /**
     * Routes a query based on semantic similarity using embeddings.
     *
     * @param query The user input.
     * @param tenant The tenant or domain identifier.
     * @return An [EmbeddingRoutingResult] containing the best-matching agent.
     */
    fun resolveAgent(routingRequest: EmbeddingRoutingRequest): EmbeddingRoutingResult
}


/**
 * The [EmbeddingRanker] evaluates and ranks a list of embeddings
 * to identify the most qualified agent.
 */
interface EmbeddingRanker {

    /**
     * Determines the most qualified agent from a given set of embeddings.
     *
     * @param embeddings A set of scored embeddings.
     * @return A qualified agent or null if no clear winner is found.
     */
    fun findQualifiedAgent(embeddings: List<Embedding>): QualifiedAgent
}

/**
 * [EmbeddingHandler] handles capabilities for a tenant in an embedding store.
 */
interface EmbeddingHandler {
    /**
     * Ingests capability groups into the embedding store for a given tenant.
     *
     * @param tenant The tenant identifier.
     * @param groups The capability groups to ingest.
     */
    fun ingest(tenant: String, groups: List<CapabilityGroup>)

    /**
     * Removes all capabilities from the embedding store for a given tenant.
     *
     * @param tenant The tenant identifier.
     */
    fun remove(tenant: String)
}

/**
 * [EmbeddingRetriever] retrieves embeddings from a embedding store.
 */
interface EmbeddingRetriever {
    /**
     * Retrieves embeddings for a query in the given tenant scope.
     *
     * @param tenant The tenant identifier.
     * @param query The user input.
     * @return A list of [Embedding]s related to the query.
     */
    fun retrieve(tenant: String, query: String): List<Embedding>
}

/**
 * Exception indicating that the current tenant is not handled by the [EmbeddingRetriever].
 */
class TenantNotSupportedException(
    message: String, cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Represents a group of capabilities for a given agent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CapabilityGroup(
    val id: String,
    val description: String?,
    val capabilities: List<Capability>,
)

/**
 * Represents an individual capability within a group.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Capability(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val examples: List<String>
)

/**
 * [EmbeddingRoutingResult] represents the routing result from an embedding-based router,
 * including the selected agent and all considered agents.
 */
class EmbeddingRoutingResult @JsonCreator constructor(
    @JsonProperty("agentId") var agentId: String?,
    @JsonProperty("consideredAgents") val consideredAgents: List<Agent>,
    @JsonProperty("foundBySemanticSearch") val foundBySemanticSearch: Boolean = false
)

/**
 * [Embedding] holds a single semantic example and its relevance score,
 * along with metadata about the corresponding capability and agent.
 */
data class Embedding(
    val example: String,
    val score: Double,
    val agentId: String,
    val capabilityId: String,
    val capabilityVersion: String,
    val capabilityDescription: String,
)

/**
 * [QualifiedAgent] wraps the ID of the most qualified agent after ranking.
 */
data class QualifiedAgent (
    val agentId: String?
)