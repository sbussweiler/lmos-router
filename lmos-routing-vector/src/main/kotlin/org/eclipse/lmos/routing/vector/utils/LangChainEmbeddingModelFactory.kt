package org.eclipse.lmos.routing.vector.utils

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import java.time.Duration
import org.eclipse.lmos.routing.vector.utils.EmbeddingModelProvider.*


class LangChainEmbeddingModelFactory private constructor() {
    companion object {
        fun createClient(properties: EmbeddingModelClientProperties): EmbeddingModel {
            return when (properties.provider) {

                HUGGINGFACE.name.lowercase(),
                    -> {
                    if (properties.apiKey == null || properties.modelName == null) {
                        throw IllegalArgumentException("API key and model name must be provided for HuggingFace embedding models.")
                    }
                    HuggingFaceEmbeddingModel.builder()
                        .accessToken(properties.apiKey)
                        .modelId(properties.modelName)
                        .waitForModel(true)
                        .timeout(Duration.ofSeconds(1000))
                        .build()
                }

                OPENAI.name.lowercase() -> {
                    if (properties.baseUrl == null) {
                        throw IllegalArgumentException("Base URL must be provided for OpenAI embedding models.")
                    }
                    OpenAiEmbeddingModel.builder()
                        .baseUrl(properties.baseUrl)
                        .apiKey(properties.apiKey)
                        .modelName(properties.modelName)
                        .build()
                }

                LOCAL_ONNX.name.lowercase() -> {
                    if (properties.modelPath == null || properties.tokenizerPath == null) {
                        throw IllegalArgumentException("Model path and tokenizer path must be provided for local embedding models.")
                    }
                    OnnxEmbeddingModel(
                        properties.modelPath,
                        properties.tokenizerPath,
                        dev.langchain4j.model.embedding.onnx.PoolingMode.MEAN
                    )
                }

                else -> {
                    throw IllegalArgumentException("Unknown embedding model properties: $properties")
                }
            }
        }
    }
}

enum class EmbeddingModelProvider {
    HUGGINGFACE,
    OPENAI,
    LOCAL_ONNX,
}

open class EmbeddingModelClientProperties(
    open val provider: String,
    open val modelName: String? = null,
    open val apiKey: String? = null,
    open val baseUrl: String? = null,
    open val modelPath: String? = null,
    open val tokenizerPath: String? = null,
)
