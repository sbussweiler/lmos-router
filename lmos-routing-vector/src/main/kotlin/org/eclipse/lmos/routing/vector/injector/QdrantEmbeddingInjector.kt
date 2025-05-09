package org.eclipse.lmos.routing.vector.injector

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import org.eclipse.lmos.routing.core.semantic.CapabilityGroup
import org.eclipse.lmos.routing.core.semantic.EmbeddingInjector
import org.eclipse.lmos.routing.vector.utils.convert
import org.slf4j.LoggerFactory


class QdrantEmbeddingInjector(
    private val qdrantHost: String,
    private val qdrantPort: Int,
    private val embeddingModel: EmbeddingModel,
) : EmbeddingInjector {

    private val logger = LoggerFactory.getLogger(QdrantEmbeddingInjector::class.java)
    private val qdrantClient = QdrantClient(
        QdrantGrpcClient.newBuilder(
            qdrantHost,
            qdrantPort,
            false
        ).build()
    )
    private val ingestorByCollection = mutableMapOf<String, EmbeddingStoreIngestor>()

    override fun ingest(tenant: String, groups: List<CapabilityGroup>) {
        val documents = groups.flatMap { it.convert() }
        if (documents.isNotEmpty()) {
            logger.info("Start to ingest ${documents.size} documents in collection '$tenant'.")
            val ingestor = ingestorByCollection.getOrPut(tenant) { createIngestor(tenant) }
            ingestor.ingest(documents)
            logger.info("Created ${documents.size} documents in collection '$tenant'.")
        }
    }

    private fun createIngestor(collection: String): EmbeddingStoreIngestor {
        createCollectionIfNotExist(collection)

        val embeddingStore = QdrantEmbeddingStore.builder()
            .host(qdrantHost)
            .port(qdrantPort)
            .collectionName(collection)
            .build()

        val ingestor = EmbeddingStoreIngestor.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build()

        return ingestor
    }

    private fun createCollectionIfNotExist(collection: String) {
        val collectionExists = qdrantClient.collectionExistsAsync(collection).get()
        if (!collectionExists) {
            qdrantClient.createCollectionAsync(
                collection,
                VectorParams.newBuilder()
                    .setDistance(Distance.Cosine) // sollten wir über die Properties konfiguriren
                    .setSize(1024)
                    .build()
            ).get()
        }
    }

    companion object {
        fun builder(): QdrantEmbeddingInjectorBuilder {
            return QdrantEmbeddingInjectorBuilder()
        }
    }
}


class QdrantEmbeddingInjectorBuilder {
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

    fun build(): QdrantEmbeddingInjector {
        if (embeddingModel == null) throw IllegalStateException("EmbeddingModel must be set")
        return QdrantEmbeddingInjector(qdrantHost, qdrantPort, embeddingModel!!)
    }
}
