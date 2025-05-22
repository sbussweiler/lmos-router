package org.eclipse.lmos.router.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.lmos.routing.core.llm.ModelUserQuery
import org.eclipse.lmos.routing.core.hybrid.HybridUserQuery
import org.eclipse.lmos.routing.core.hybrid.HybridAgentClassifier
import org.eclipse.lmos.routing.core.hybrid.HybridAgentClassification
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.llm.ModelAgentClassifier
import org.eclipse.lmos.routing.core.llm.ModelAgentClassification
import org.eclipse.lmos.routing.core.llm.ModelRagAgentClassifier
import org.eclipse.lmos.routing.core.semantic.*
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
class ClassifierController(
    private val embeddingClassifier: EmbeddingAgentClassifier,
    private val modelClassifier: ModelAgentClassifier,
    private val modelRaqClassifier: ModelRagAgentClassifier,
    private val hybridClassifier: HybridAgentClassifier,
    private val embeddingRetriever: EmbeddingRetriever
) {

    private val logger = LoggerFactory.getLogger(ClassifierController::class.java)

    @PostMapping("/llm")
    fun routeByLLM(@RequestBody userQuery: UserAgentQuery): ModelAgentClassification {
        logger.info("Route user query '${userQuery.query}' by LLM.")
        val conversationId = userQuery.conversationId ?: "default"
        return modelClassifier.classify(
            ModelUserQuery(
                userQuery.query,
                userQuery.agents,
                conversationId
            )
        )
    }

    @PostMapping("/rag-llm")
    fun routeByRagLLM(@RequestBody userQuery: UserRequest): ModelAgentClassification {
        logger.info("Route user query '${userQuery.query}' by RagLLM.")
        val conversationId = userQuery.conversationId ?: "default"
        return modelRaqClassifier.classify(
            HybridUserQuery(
                userQuery.query,
                userQuery.tenant,
                conversationId
            )
        )
    }

    @PostMapping("/vector")
    fun vectorRouting(@RequestBody userQuery: UserRequest): EmbeddingAgentClassification {
        logger.info("Perform vector routing for ${userQuery.tenant} and user query '${userQuery.query}'.")
        return embeddingClassifier.classify(
            EmbeddingUserQuery(
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
    fun routeHybrid(@RequestBody userQuery: UserRequest): HybridAgentClassification {
        logger.info("Route user query '${userQuery.query}' by embeddings.")
        val conversationId = userQuery.conversationId ?: "default"
        return hybridClassifier.classify(
            HybridUserQuery(
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