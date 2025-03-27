package org.eclipse.lmos.router.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.langchain4j.rag.content.ContentMetadata
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.query.Query
import org.eclipse.lmos.router.Router
import org.eclipse.lmos.router.RoutingResult
import org.eclipse.lmos.router.embeddings.EMBEDDING_METADATA_AGENT_ID
import org.eclipse.lmos.router.embeddings.EMBEDDING_METADATA_CAPABILITY_DESCRIPTION
import org.eclipse.lmos.router.embeddings.EMBEDDING_METADATA_CAPABILITY_ID
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/routings", produces = [MediaType.APPLICATION_JSON_VALUE])
class RouterController(
    private val routerService: Router,
    private val retriever: ContentRetriever
) {

    val logger = LoggerFactory.getLogger(RouterController::class.java)

    @PostMapping("/llms")
    fun routeByLLM(@RequestBody userQuery: UserQuery): RoutingResult {
        logger.info("Route user query '${userQuery.query}' by LLM.")
        return routerService.route(userQuery.query)
    }

    @PostMapping("/embeddings")
    fun routeByEmbeddings(@RequestBody userQuery: UserQuery): Set<Embedding> {
        logger.info("Route user query '${userQuery.query}' by embeddings.")
        val embeddings = mutableSetOf<Embedding>()
        val contentList = retriever.retrieve(Query(userQuery.query))
        contentList.forEach { content ->
            val example = content.textSegment().text()
            val score = content.metadata()[ContentMetadata.SCORE]
            val agentId = content.textSegment().metadata().getString(EMBEDDING_METADATA_AGENT_ID)
            val capabilityId = content.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_ID)
            val capabilityDescription = content.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_DESCRIPTION)

            if (agentId != null && capabilityId != null && capabilityDescription != null && score != null) {
                embeddings.add(Embedding(example, score, agentId, capabilityId, capabilityDescription))
            }
        }
        return embeddings
    }

    @PostMapping("/embeddings/plain")
    fun routeWithEmbeddingAndReturnEmbeddings(@RequestBody userQuery: UserQuery): String {
        val queryObject = Query(userQuery.query)
        return retriever.retrieve(queryObject).toString()
    }

}

data class UserQuery @JsonCreator constructor(
    @JsonProperty("query") val query: String
)

data class Embedding(
    val example: String,
    val score: Any,
    val agentId: String,
    val capabilityId: String,
    val capabilityDescription: String,
)
