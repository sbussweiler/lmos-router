package org.eclipse.lmos.routing.supervisor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.semantic.*
import org.eclipse.lmos.routing.core.starter.EmbeddingStoreProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.io.File

@Component
class EmbeddingImporter(
    private val embeddingHandler: EmbeddingHandler,
    private val embeddingStoreProperties: EmbeddingStoreProperties
) {

    private val logger = LoggerFactory.getLogger(EmbeddingImporter::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun import() {
        val channelRoutings = loadJsonFilesAsChannelRoutings("/app/capabilities")
        val groups = channelRoutings.flatMap { it.capabilityGroups }
        embeddingHandler.ingest(embeddingStoreProperties.collection, groups)
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
    val capabilityGroups: List<Agent>
)
