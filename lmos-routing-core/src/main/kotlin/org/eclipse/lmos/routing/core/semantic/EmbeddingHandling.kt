package org.eclipse.lmos.routing.core.semantic

import org.eclipse.lmos.routing.core.llm.Agent

/**
 * The [EmbeddingHandler] manages agent capabilities within an embedding store on a per-tenant basis.
 */
interface EmbeddingHandler {
    /**
     * Ingests the capabilities of the provided [agents] into the embedding store for the specified [tenant].
     *
     * This process typically includes transforming capability examples into embeddings
     * and storing them for use in semantic search or classification.
     *
     * @param tenant The unique identifier for the tenant/domain.
     * @param agents The list of agents whose capabilities should be ingested.
     */
    fun ingest(tenant: String, agents: List<Agent>)

    /**
     * Ingests the capabilities of the provided [agent] into the embedding store for the specified [tenant].
     *
     * This process typically includes transforming capability examples into embeddings
     * and storing them for use in semantic search or classification.
     *
     * @param tenant The unique identifier for the tenant/domain.
     * @param agent The list of agents whose capabilities should be ingested.
     */
    fun ingest(tenant: String, agent: Agent)

    /**
     * Deletes all stored agent capability embeddings associated with the specified [tenant].
     *
     * @param tenant The unique identifier for the tenant/domain.
     */
    fun remove(tenant: String)

    /**
     * Deletes all stored agent capability embeddings associated with the specified [tenant] and [agent].
     *
     * @param tenant The unique identifier for the tenant/domain.
     * @param agent The agent whose embeddings should be removed.
     */
    fun remove(tenant: String, agent: Agent)
}

/**
 * [EmbeddingRetriever] retrieves embeddings from an embedding store.
 */
interface EmbeddingRetriever {
    /**
     * Retrieves embeddings the given tenant and user query.
     *
     * @param tenant The tenant identifier.
     * @param query The user query.
     * @return A list of [Embedding]s related to the query.
     */
    fun retrieve(tenant: String, query: String): List<Embedding>
}

/**
 * A single vector embedding and its metadata for agent ranking.
 *
 * @property example A representative input example.
 * @property score Similarity score for the current query.
 * @property agentId ID of the agent this embedding belongs to.
 * @property capabilityId ID of the capability this embedding belongs to.
 * @property capabilityDescription Natural language description of the capability.
 */
data class Embedding(
    val example: String,
    val score: Double,
    val agentId: String,
    val capabilityId: String,
    val capabilityDescription: String,
)

/**
 * Exception indicating that the current tenant is not handled by the [EmbeddingRetriever].
 */
class TenantNotSupportedException(
    message: String, cause: Throwable? = null
) : RuntimeException(message, cause)

