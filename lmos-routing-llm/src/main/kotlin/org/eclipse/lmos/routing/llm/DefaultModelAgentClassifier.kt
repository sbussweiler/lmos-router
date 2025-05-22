package org.eclipse.lmos.routing.llm

import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import org.eclipse.lmos.routing.core.llm.ModelUserQuery
import org.eclipse.lmos.routing.core.llm.ModelAgentClassifier
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.llm.ModelAgentClassification
import org.slf4j.LoggerFactory

class DefaultModelAgentClassifier(
    private val langchainAIService: LangchainAgentClassifier
) : ModelAgentClassifier {

    private val logger = LoggerFactory.getLogger(DefaultModelAgentClassifier::class.java)

    override fun classify(query: ModelUserQuery): ModelAgentClassification {
        val classification = langchainAIService.resolveAgent(
            query.query,
            formatAsString(query.agents),
            query.conversationId
        )
        logger.info("[ModelAgentClassifier] Classified agent '${classification.agentId}' for query '${query.query}'.")
        return classification
    }

    private fun formatAsString(agents: List<Agent>) =
        agents.joinToString("\n\n") { agent ->
            String.format(
                "Agent '%s':\n\t- %s",
                agent.id,
                agent.capabilities.joinToString("\n\t- ") { capability -> capability.description })
        }

    companion object {
        fun builder(): ModelAgentClassifierBuilder {
            return ModelAgentClassifierBuilder()
        }
    }
}

interface LangchainAgentClassifier {

    fun resolveAgent(
        @UserMessage query: String,
        @V("agents") agentCapabilities: String,
        @MemoryId conversationId: String,
    ): ModelAgentClassification

}


class ModelAgentClassifierBuilder {
    private var model: ChatModel? = null
    private var systemPrompt: String = defaultSystemPrompt()
    private var maxMemoryMessages: Int = 10

    fun withChatModel(model: ChatModel) = apply {
        this.model = model
    }

    fun withSystemPrompt(systemPrompt: String) = apply {
        this.systemPrompt = systemPrompt
    }

    fun withMaxMemoryMessages(maxMessages: Int) = apply {
        this.maxMemoryMessages = maxMessages
    }

    fun build(): DefaultModelAgentClassifier {
        if (model == null) {
            throw IllegalStateException("ChatModel must be set")
        }

        val langchainClassifier = AiServices.builder(LangchainAgentClassifier::class.java)
            .chatModel(model)
            .systemMessageProvider { systemPrompt }
            .chatMemoryProvider { MessageWindowChatMemory.withMaxMessages(maxMemoryMessages) }
            .build()

        return DefaultModelAgentClassifier(langchainClassifier)
    }

}
