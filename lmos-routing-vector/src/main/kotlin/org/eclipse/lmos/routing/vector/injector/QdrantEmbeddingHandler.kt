package org.eclipse.lmos.routing.vector.injector

import dev.langchain4j.model.embedding.EmbeddingModel
import io.qdrant.client.ConditionFactory
import io.qdrant.client.PointIdFactory
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import io.qdrant.client.grpc.Points.*
import org.eclipse.lmos.routing.core.semantic.*
import org.slf4j.LoggerFactory
import java.util.UUID

class QdrantEmbeddingHandler(
    qdrantHost: String,
    qdrantPort: Int,
    private val embeddingModel: EmbeddingModel,
) : EmbeddingHandler {

    private val logger = LoggerFactory.getLogger(QdrantEmbeddingHandler::class.java)

    private val client = QdrantClient(
        QdrantGrpcClient.newBuilder(
            qdrantHost,
            qdrantPort,
            false
        ).build()
    )

    override fun ingest(tenant: String, groups: List<CapabilityGroup>) {
        logger.info("Start ingesting embeddings into the '$tenant' collection for the following agents: ${groups.map { it.id }}.")
        groups.forEach {
            try {
                ingest(tenant, it)
            } catch (e: Exception) {
                logger.error("Failed to ingest embeddings for agent '${it.id}' to collection '$tenant'.", e)
            }
        }
        logger.info("Completed ingestion of embeddings into the '$tenant' collection for the following agents: ${groups.map { it.id }}.")
    }

    private fun ingest(tenant: String, group: CapabilityGroup) {
        createCollectionIfNotExist(tenant)
        val existingPoints = getExistingPoints(tenant, group.id)
        val desiredPoints = getDesiredPoints(group)
        val pointsToAdd = desiredPoints.keys - existingPoints.keys
        val pointsToDelete = existingPoints.keys - desiredPoints.keys

        if (pointsToAdd.isNotEmpty()) {
            val points = pointsToAdd.map { createPointStruct(desiredPoints[it]!!) }
            client.upsertAsync(tenant, points)
            logger.info("Added ${pointsToAdd.size} embeddings to the '$tenant' collection for agent '${group.id}'.")
        }
        if (pointsToDelete.isNotEmpty()) {
            val points = pointsToDelete.map { existingPoints[it]!! }
            client.deleteAsync(tenant, points.map { it.id })
            logger.info("Deleted ${pointsToDelete.size} embeddings from the '$tenant' collection for agent '${group.id}'.")
        }
        if (pointsToAdd.isEmpty() && pointsToDelete.isEmpty()) {
            logger.info("No embedding changes for agent '${group.id}'.")
        }
    }

    private fun getExistingPoints(collection: String, agentId: String): Map<UUID, RetrievedPoint> {
        val agentFilter = Filter.newBuilder()
            .addMust(ConditionFactory.matchKeyword(EMBEDDING_METADATA_AGENT_ID, agentId))
            .build()
        val scrollPoints = ScrollPoints.newBuilder()
            .setCollectionName(collection)
            .setFilter(agentFilter)
            .setLimit(5000)
            .build()
        return client.scrollAsync(scrollPoints).get()
            .resultList
            .associateBy { UUID.fromString(it.id.uuid) }
    }

    private fun getDesiredPoints(group: CapabilityGroup): Map<UUID, PointInfo> =
        group.capabilities.flatMap { capability ->
            capability.examples.map { example ->
                val rawId = "${group.id}::${capability.id}::${example.hashCode()}"
                val uuid = UUID.nameUUIDFromBytes(rawId.toByteArray())
                uuid to PointInfo(uuid, group.id, capability, example)
            }
        }.toMap()

    private fun createPointStruct(pointInfo: PointInfo): PointStruct {
        val embedding = embedText(pointInfo.example)
        val payload = mapOf(
            EMBEDDING_METADATA_AGENT_ID to value(pointInfo.groupId),
            EMBEDDING_METADATA_CAPABILITY_ID to value(pointInfo.capability.id),
            EMBEDDING_METADATA_CAPABILITY_NAME to value(pointInfo.capability.name),
            EMBEDDING_METADATA_CAPABILITY_VERSION to value(pointInfo.capability.version),
            EMBEDDING_METADATA_CAPABILITY_DESCRIPTION to value(pointInfo.capability.description),
            EMBEDDING_METADATA_CAPABILITY_EXAMPLE to value(pointInfo.example),
        )
        return PointStruct.newBuilder()
            .setId(PointIdFactory.id(pointInfo.uuid))
            .setVectors(vectors(embedding))
            .putAllPayload(payload)
            .build()
    }

    private fun embedText(text: String): List<Float> {
        return embeddingModel.embed(text).content().vectorAsList()
    }

    private fun createCollectionIfNotExist(collection: String) {
        val collectionExists = client.collectionExistsAsync(collection).get()
        if (!collectionExists) {
            client.createCollectionAsync(
                collection,
                VectorParams.newBuilder()
                    .setDistance(Distance.Cosine)
                    .setSize(1024)
                    .build()
            ).get()
        }
    }

    override fun remove(tenant: String) {
        client.deleteCollectionAsync(tenant).get()
        logger.info("Removed '$tenant' collection and all related embeddings.")
    }

    companion object {
        fun builder(): QdrantEmbeddingHandlerBuilder {
            return QdrantEmbeddingHandlerBuilder()
        }
    }
}

data class PointInfo(
    val uuid: UUID,
    val groupId: String,
    val capability: Capability,
    val example: String
)

class QdrantEmbeddingHandlerBuilder {
    private var qdrantHost: String = "localhost"
    private var qdrantPort: Int = 6334
    private var embeddingModel: EmbeddingModel? = null

    fun withQdrantHost(qdrantHost: String) = apply {
        this.qdrantHost = qdrantHost
    }

    fun withQdrantPort(qdrantPort: Int) = apply {
        this.qdrantPort = qdrantPort
    }

    fun withEmbeddingModel(embeddingModel: EmbeddingModel) = apply {
        this.embeddingModel = embeddingModel
    }

    fun build(): QdrantEmbeddingHandler {
        if (embeddingModel == null) throw IllegalStateException("EmbeddingModel must be set")
        return QdrantEmbeddingHandler(qdrantHost, qdrantPort, embeddingModel!!)
    }
}
