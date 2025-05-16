package org.eclipse.lmos.router.embeddings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.lmos.router.config.EmbeddingDocumentProperties
import org.eclipse.lmos.routing.core.semantic.*
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.io.File

@Component
@Configuration
@EnableConfigurationProperties(EmbeddingDocumentProperties::class)
class DocumentHandler(
    private val embeddingHandler: EmbeddingHandler,
    private val embeddingDocumentProperties: EmbeddingDocumentProperties
) {

    private val logger = LoggerFactory.getLogger(DocumentHandler::class.java)

    fun ingestDocuments(tenant: String) {
        val channelRoutings = loadJsonFilesAsChannelRoutings(embeddingDocumentProperties.documentPath)
        val groups = channelRoutings.flatMap { it.capabilityGroups }
        embeddingHandler.ingest(tenant, groups)
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

    fun removeDocuments(tenant: String) {
        embeddingHandler.remove(tenant)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelRouting(
    val id: String,
    val capabilityGroups: List<CapabilityGroup>
)
