package org.eclipse.lmos.router.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.lmos.routing.core.ChatModelRoutingRequest
import org.eclipse.lmos.routing.core.EmbeddingChatModelRoutingRequest
import org.eclipse.lmos.routing.core.EmbeddingRoutingRequest
import org.eclipse.lmos.routing.core.hybrid.HybridRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.routing.core.semantic.TenantNotSupportedException
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.llm.ChatModelRouter
import org.eclipse.lmos.routing.core.semantic.Embedding
import org.eclipse.lmos.routing.core.llm.ChatModelRoutingResult
import org.eclipse.lmos.routing.core.llm.RagChatModelRouter
import org.eclipse.lmos.routing.core.semantic.EmbeddingRoutingResult
import org.eclipse.lmos.routing.vector.EmbeddingVectorRouter
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/routings", produces = [MediaType.APPLICATION_JSON_VALUE])
class RouterController(
    private val vectorRouter: EmbeddingVectorRouter,
    private val llmRouter: ChatModelRouter,
    private val ragRouter: RagChatModelRouter,
    private val hybridRouter: HybridRouter,
    private val embeddingRetriever: EmbeddingRetriever
) {

    private val logger = LoggerFactory.getLogger(RouterController::class.java)

    @PostMapping("/llm")
    fun routeByLLM(@RequestBody userQuery: UserAgentQuery): ChatModelRoutingResult {
        logger.info("Route user query '${userQuery.query}' by LLM.")
        val conversationId = userQuery.conversationId ?: "default"
        return llmRouter.resolveAgent(
            ChatModelRoutingRequest(
                userQuery.query,
                userQuery.agents,
                conversationId
            )
        )
    }

    @PostMapping("/rag-llm")
    fun routeByRagLLM(@RequestBody userQuery: UserRequest): ChatModelRoutingResult {
        logger.info("Route user query '${userQuery.query}' by RagLLM.")
        val conversationId = userQuery.conversationId ?: "default"
        return ragRouter.resolveAgent(
            EmbeddingChatModelRoutingRequest(
                userQuery.query,
                userQuery.tenant,
                conversationId
            )
        )
    }

    @PostMapping("/vector")
    fun vectorRouting(@RequestBody userQuery: UserRequest): EmbeddingRoutingResult {
        logger.info("Perform vector routing for ${userQuery.tenant} and user query '${userQuery.query}'.")
        return vectorRouter.resolveAgent(
            EmbeddingRoutingRequest(
                userQuery.query,
                userQuery.tenant
            )
        )
    }

    @PostMapping("/vector/plain")
    fun routeByEmbeddings(@RequestBody userQuery: UserRequest): List<Embedding> {
        logger.info("Retrieve plain embeddings for ${userQuery.tenant} and user query '${userQuery.query}'.")
        return embeddingRetriever.retrieve(userQuery.tenant, userQuery.query)
    }

    @PostMapping("/hybrid")
    fun routeHybrid(@RequestBody userQuery: UserRequest): EmbeddingRoutingResult {
        logger.info("Route user query '${userQuery.query}' by embeddings.")
        val conversationId = userQuery.conversationId ?: "default"
        return hybridRouter.resolveAgent(
            EmbeddingChatModelRoutingRequest(
                userQuery.query,
                userQuery.tenant,
                conversationId
            )
        )
    }

    @ExceptionHandler(TenantNotSupportedException::class)
    fun handleTenantNotSupported(ex: TenantNotSupportedException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            error = "TENANT_NOT_SUPPORTED",
            message = ex.message ?: "Unsupported tenant"
        )
        return ResponseEntity.badRequest().body(response)
    }
}

data class UserRequest @JsonCreator constructor(
    @JsonProperty("query") val query: String,
    @JsonProperty("tenant") val tenant: String,
    @JsonProperty("conversationId") val conversationId: String?
)


data class UserAgentQuery @JsonCreator constructor(
    @JsonProperty("query") val query: String,
    @JsonProperty("conversationId") val conversationId: String?,
    @JsonProperty("agents") val agents: List<Agent>
)

data class ErrorResponse(
    val error: String,
    val message: String,
)