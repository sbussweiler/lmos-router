package org.eclipse.lmos.routing.supervisor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.eclipse.lmos.routing.core.semantic.*
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths

@Component
class EmbeddingHandler(
    private val embeddingInjector: EmbeddingInjector,
    private val embeddingStoreProperties: EmbeddingStoreProperties
) {

    private val logger = LoggerFactory.getLogger(EmbeddingHandler::class.java)
    private val qdrantClient = QdrantClient(
        QdrantGrpcClient.newBuilder(
            embeddingStoreProperties.host,
            embeddingStoreProperties.port,
            false
        ).build()
    )

    @EventListener(ApplicationReadyEvent::class)
    fun embedDocuments() {
        cleanupCollection()
        createEmbeddings()
    }

    private fun cleanupCollection() {
        val collectionExists = qdrantClient.collectionExistsAsync(embeddingStoreProperties.collection).get()
        if (collectionExists) {
            qdrantClient.deleteCollectionAsync(embeddingStoreProperties.collection)
        }
    }

    private fun createEmbeddings() {
        val channelRoutings = loadJsonFilesAsChannelRoutings("/app/capabilities")
        val groups = channelRoutings.flatMap { it.capabilityGroups }
        embeddingInjector.ingest(embeddingStoreProperties.collection, groups)
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
