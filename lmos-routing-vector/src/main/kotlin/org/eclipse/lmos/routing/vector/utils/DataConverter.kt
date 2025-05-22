package org.eclipse.lmos.routing.vector.utils

import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.ContentMetadata
import org.eclipse.lmos.routing.core.semantic.*
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.llm.Capability
import org.eclipse.lmos.routing.core.semantic.Embedding


fun Content.convert(): Embedding? {
    val example = this.textSegment().text()
    val score = this.metadata()[ContentMetadata.SCORE]
    val agentId = this.textSegment().metadata().getString(EMBEDDING_METADATA_AGENT_ID)
    val capabilityId = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_ID)
    val capabilityDescription = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_DESCRIPTION)

    return if (score != null && agentId != null && capabilityId != null && capabilityDescription != null) {
        Embedding(example, score as Double, agentId, capabilityId, capabilityDescription)
    } else {
        null
    }
}

fun List<Embedding>.convertEmbeddingsToAgents(): List<Agent> {
    return this
        .groupBy { it.agentId }
        .map { (agentId, embeddings) ->
            val examplesByCapability = embeddings
                .groupBy(
                    keySelector = { it.capabilityId },
                    valueTransform = { it.example }
                )
            val capabilities = embeddings
                .distinctBy { it.capabilityId }
                .map {
                    Capability(
                        id = it.capabilityId,
                        description = it.capabilityDescription,
                        examples = examplesByCapability[it.capabilityId] ?: emptyList()
                    )
                }
            Agent(id = agentId, capabilities = capabilities)
        }
}