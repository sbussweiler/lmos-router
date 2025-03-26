package org.eclipse.lmos.router.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.query.Query
import org.eclipse.lmos.router.Agent
import org.eclipse.lmos.router.Router
import org.eclipse.lmos.router.RoutingResult
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

    @PostMapping("/llms")
    fun routeWithLLM(@RequestBody userQuery: UserQuery): RoutingResult {
        return routerService.route(userQuery.query)
    }

    @PostMapping("/embeddings")
    fun routeWithEmbedding(@RequestBody userQuery: UserQuery): RoutingResult {
        val agentCapabilities = mutableMapOf<String, MutableSet<String>>()
        val contentList = retriever.retrieve(Query(userQuery.query))
        contentList.forEach { content ->
            val agentId = content.textSegment().metadata().getString("agentId")
            val capabilityId = content.textSegment().metadata().getString("capabilityId")

            if (agentId != null && capabilityId != null) {
                agentCapabilities.computeIfAbsent(agentId) { mutableSetOf() }.add(capabilityId)
            }
        }

        val agents = agentCapabilities.map { (name, capabilities) ->
            Agent(name, capabilities.toList())
        }

        return RoutingResult(agents)
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
