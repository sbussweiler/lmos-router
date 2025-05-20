package LangChainClientProvider

import com.azure.identity.DefaultAzureCredentialBuilder
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.azure.AzureOpenAiChatModel
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder
import LangChainClientProvider.LangChainClientProvider.*
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import org.slf4j.LoggerFactory

/**
 * A factory class to create a Langchain4j language model client.
 */
class LangChainChatModelFactory private constructor() {
    companion object {
        fun createClient(properties: ModelClientProperties): ChatLanguageModel {
            return when (properties.provider) {
                OPENAI.name.lowercase(),
                    -> {
                    OpenAiChatModelBuilder()
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                ANTHROPIC.name.lowercase() -> {
                    AnthropicChatModel.builder()
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                AZURE_OPENAI.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    AzureOpenAiChatModel.builder()
                        .endpoint(properties.baseUrl)
                        .apiKey(properties.apiKey)
                        .deploymentName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequestsAndResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                AZURE_OPENAI_IDENTITY.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    AzureOpenAiChatModel.builder()
                        .endpoint(properties.baseUrl)
                        .tokenCredential(DefaultAzureCredentialBuilder().build())
                        .deploymentName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequestsAndResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                GEMINI.name.lowercase() -> {
                    GoogleAiGeminiChatModel.builder()
                        .modelName(properties.model)
                        .maxOutputTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequestsAndResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                OLLAMA.name.lowercase() -> {
                    OllamaChatModel.builder().baseUrl(properties.baseUrl)
                        .modelName(properties.model)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                OTHER.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for OTHER provider" }
                    OpenAiChatModelBuilder()
                        .baseUrl(properties.baseUrl)
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                else -> {
                    throw IllegalArgumentException("Unknown model client properties: $properties")
                }
            }
        }
    }
}

enum class LangChainClientProvider {
    OPENAI,
    ANTHROPIC,
    AZURE_OPENAI,
    AZURE_OPENAI_IDENTITY,
    GEMINI,
    OLLAMA,
    OTHER,
}

open class ModelClientProperties(
    open val provider: String,
    open val apiKey: String? = null,
    open val baseUrl: String? = null,
    open val model: String,
    open val maxTokens: Int = 2000,
    open val temperature: Double = 0.0,
    open val logRequestsAndResponses: Boolean = false
//    open val format: String? = null,
//    open val topK: Int? = null,
//    open val topP: Double? = null,
)

class ModelConversationLogger : ChatModelListener {

    private val logger = LoggerFactory.getLogger(ChatModelListener::class.java)

    override fun onRequest(requestContext: ChatModelRequestContext) {
        logger.info("onRequest(): {}", requestContext.chatRequest())
    }

    override fun onResponse(responseContext: ChatModelResponseContext) {
        logger.info("onResponse(): {}", responseContext.chatResponse())
    }

    override fun onError(errorContext: ChatModelErrorContext) {
        logger.info("onError(): {}", errorContext.error().message)
    }
}
