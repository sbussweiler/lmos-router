package org.eclipse.lmos.router.embeddings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import org.eclipse.lmos.router.config.EmbeddingDocumentProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class DocumentHandler(
    private val ingestor: EmbeddingStoreIngestor,
    private val embeddingStore: EmbeddingStore<TextSegment>,
    private val embeddingDocumentProperties: EmbeddingDocumentProperties
) {

    private val logger = LoggerFactory.getLogger(DocumentHandler::class.java)

    fun embedDocuments() {
        logger.info("Start to read documents from path: ${embeddingDocumentProperties.documentPath}")
        loadJsonFilesAsChannelRoutings(embeddingDocumentProperties.documentPath).forEach { channelRouting ->
            channelRouting.capabilityGroups.forEach { group ->
                group.capabilities.forEach { capability ->
                    capability.examples.forEach { example ->
                        val metadata = Metadata.from(
                            mapOf(
                                "agentId" to group.id,
                                "capabilityId" to capability.id,
                                "capabilityName" to capability.name,
                                "capabilityVersion" to capability.version,
                                "capabilityDescription" to capability.description,
                            )
                        )
                        logger.info("Embedding example '$example' from ${group.id}/${capability.id}")
                        ingestor.ingest(Document.document(example, metadata))
                    }
                }
            }
        }
        logger.info("Finished to read documents from path: ${embeddingDocumentProperties.documentPath}")
    }

    fun deleteAllDocuments() {
        embeddingStore.removeAll()
        logger.info("Deleted all embedded documents.")
    }

    private fun loadJsonFilesAsChannelRoutings(directoryPath: String): List<ChannelRouting> {
        val objectMapper = jacksonObjectMapper()
        return File(directoryPath)
            .listFiles { file -> file.isFile && file.extension == "json" }
            ?.mapNotNull { file ->
                runCatching {
                    objectMapper.readValue(file, ChannelRouting::class.java)
                }.onFailure { ex ->
                    logger.error("Failed to parse file: ${file.name}", ex)
                }.onSuccess {
                    logger.info("Successfully parsed file: ${file.name}")
                }.getOrNull()
            } ?: emptyList()
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelRouting(
    val id: String,
    val capabilityGroups: List<CapabilityGroup>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CapabilityGroup(
    val id: String,
    val description: String?,
    val capabilities: List<Capability>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Capability(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val examples: List<String>
)