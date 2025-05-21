package org.eclipse.lmos.routing.core

import org.eclipse.lmos.routing.core.llm.Agent

/**
 * Marker interface for all LMOS routers.
 */
interface Router

interface RoutingRequest {
    val query: String
}

data class ChatModelRoutingRequest(
    override val query: String,
    val agents: List<Agent>,
    val conversationId: String,
) : RoutingRequest

data class EmbeddingRoutingRequest(
    override val query: String,
    val tenant: String,
) : RoutingRequest

data class EmbeddingChatModelRoutingRequest(
    override val query: String,
    val tenant: String,
    val conversationId: String,
) : RoutingRequest

