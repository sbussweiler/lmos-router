package org.eclipse.lmos.routing.vector.retriever

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import org.eclipse.lmos.routing.core.semantic.*
import org.eclipse.lmos.routing.core.semantic.Embedding
import org.eclipse.lmos.routing.vector.utils.convert

class QdrantEmbeddingRetriever(
    private val qdrantHost: String,
    private val qdrantPort: Int,
    private val embeddingModel: EmbeddingModel,
    private var embeddingMaxResults: Int = 15,
) : EmbeddingRetriever {

    private val retrieverByCollection = mutableMapOf<String, ContentRetriever>()

    override fun retrieve(tenant: String, query: String): List<Embedding> {
        try {
            val retriever = retrieverByCollection.getOrPut(tenant) { createRetriever(tenant) }
            val contents = retriever.retrieve(Query(query))
            val embeddings = contents.mapNotNull { it.convert() }
            return embeddings
        } catch (e: RuntimeException) {
            throw TenantNotSupportedException("Tenant '$tenant' is not supported")
        }
    }

    private fun createRetriever(tenant: String): EmbeddingStoreContentRetriever {
        val embeddingStore = QdrantEmbeddingStore.builder()
            .host(qdrantHost)
            .port(qdrantPort)
            .collectionName(tenant)
            .build()

        val contentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .maxResults(embeddingMaxResults)
            .build()

        return contentRetriever
    }

    companion object {
        fun builder(): QdrantEmbeddingRetrieverBuilder {
            return QdrantEmbeddingRetrieverBuilder()
        }
    }
}


class QdrantEmbeddingRetrieverBuilder {
    private var qdrantHost: String = "localhost"
    private var qdrantPort: Int = 6334
    private var embeddingModel: EmbeddingModel? = null
    private var embeddingMaxResults: Int = 15

    fun withQdrantHost(qdrantHost: String) = apply {
        this.qdrantHost = qdrantHost
    }

    fun withQdrantPort(qdrantPort: Int) = apply {
        this.qdrantPort = qdrantPort
    }

    fun withEmbeddingModel(embeddingModel: EmbeddingModel) = apply {
        this.embeddingModel = embeddingModel
    }

    fun withEmbeddingMaxResults(embeddingMaxResults: Int) = apply {
        this.embeddingMaxResults = embeddingMaxResults
    }

    fun build(): QdrantEmbeddingRetriever {
        if (embeddingModel == null) throw IllegalStateException("EmbeddingModel must be set")
        return QdrantEmbeddingRetriever(qdrantHost, qdrantPort, embeddingModel!!, embeddingMaxResults)
    }
}