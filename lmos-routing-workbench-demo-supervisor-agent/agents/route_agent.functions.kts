import org.eclipse.lmos.routing.core.llm.RagChatModelRouter
import org.eclipse.lmos.routing.core.EmbeddingChatModelRoutingRequest

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

function(
    name = "route_agent",
    description = "Returns the qualified agent based on the user query.",
    params = types(
        string("query", "The user query for the routing.", required = true),
    )
) { (query) ->
    val router = get<RagChatModelRouter>()
    val routingResult = router.resolveAgent(EmbeddingChatModelRoutingRequest(query.toString(), "arc-supervisor", "conversationId"))
    "${routingResult.agentId}"
}