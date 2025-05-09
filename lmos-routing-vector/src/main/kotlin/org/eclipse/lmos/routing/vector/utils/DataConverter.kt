package org.eclipse.lmos.routing.vector.utils

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.ContentMetadata
import org.eclipse.lmos.routing.core.semantic.*
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.llm.AgentCapability
import org.eclipse.lmos.routing.core.semantic.Embedding


fun CapabilityGroup.convert(): List<Document> {
    val documents = mutableListOf<Document>()
    this.capabilities.forEach { capability ->
        capability.examples.forEach { example ->
            val metadata = Metadata.from(
                mapOf(
                    EMBEDDING_METADATA_AGENT_ID to this.id,
                    EMBEDDING_METADATA_CAPABILITY_ID to capability.id,
                    EMBEDDING_METADATA_CAPABILITY_NAME to capability.name,
                    EMBEDDING_METADATA_CAPABILITY_VERSION to capability.version,
                    EMBEDDING_METADATA_CAPABILITY_DESCRIPTION to capability.description,
                )
            )
            documents.add(Document.document(example, metadata))
        }
    }
    return documents
}

fun Content.convert(): Embedding? {
    val example = this.textSegment().text()
    val score = this.metadata()[ContentMetadata.SCORE]
    val agentId = this.textSegment().metadata().getString(EMBEDDING_METADATA_AGENT_ID)
    val capabilityId = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_ID)
    val capabilityVersion = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_VERSION)
    val capabilityDescription = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_DESCRIPTION)

    return if (score != null && agentId != null && capabilityId != null && capabilityVersion != null && capabilityDescription != null) {
        Embedding(example, score as Double, agentId, capabilityId, capabilityVersion, capabilityDescription)
    } else {
        null
    }
}

fun List<Embedding>.convertEmbeddingsToAgents(): List<Agent> {
    return this
        .groupBy { it.agentId }
        .map { (agentId, agentEmbeddings) ->
            val capabilities = agentEmbeddings
                .map { embedding ->
                    AgentCapability(
                        id = embedding.capabilityId,
                        description = embedding.capabilityDescription
                    )
                }
                .toSet()
            Agent(id = agentId, capabilities = capabilities)
        }
}