package org.eclipse.lmos.routing.llm

import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import org.eclipse.lmos.routing.core.llm.ChatModelRouter
import org.eclipse.lmos.routing.core.llm.Agent
import org.eclipse.lmos.routing.core.llm.ChatModelRoutingResult
import org.slf4j.LoggerFactory

class DefaultChatModelRouter(
    private val langchainAIService: LangchainLlmAgentResolver
) : ChatModelRouter {

    private val logger = LoggerFactory.getLogger(DefaultChatModelRouter::class.java)

    override fun resolveAgent(query: String, agents: List<Agent>, conversationId: String): ChatModelRoutingResult {
        logger.info("Resolving $conversationId agent for $agents")
        return langchainAIService.resolveAgent(query, formatAsString(agents), conversationId)
    }

    private fun formatAsString(agents: List<Agent>) =
        agents.joinToString("\n\n") { agent ->
            String.format(
                "Agent '%s':\n\t- %s",
                agent.id,
                agent.capabilities.joinToString("\n\t- ") { capability -> capability.description })
        }

    companion object {
        fun builder(): LlmRouterBuilder {
            return LlmRouterBuilder()
        }
    }
}

interface LangchainLlmAgentResolver {

    fun resolveAgent(@UserMessage query: String, @V("agents") agentCapabilities: String, @MemoryId conversationId: String, ): ChatModelRoutingResult

}


class LlmRouterBuilder {
    private var model: ChatLanguageModel? = null
    private var systemPrompt: String = defaultSystemPrompt()
    private var maxMemoryMessages: Int = 10

    fun withChatModel(model: ChatLanguageModel) = apply {
        this.model = model
    }

    fun withSystemPrompt(systemPrompt: String) = apply {
        this.systemPrompt = systemPrompt
    }

    fun withMaxMemoryMessages(maxMessages: Int) = apply {
        this.maxMemoryMessages = maxMessages
    }
    
    fun build(): DefaultChatModelRouter {
        if (model == null) {
            throw IllegalStateException("ChatLanguageModel must be set")
        }

        val langchain4jRouter = AiServices.builder(LangchainLlmAgentResolver::class.java)
            .chatLanguageModel(model)
            .systemMessageProvider { systemPrompt }
            .chatMemoryProvider { MessageWindowChatMemory.withMaxMessages(maxMemoryMessages) }
            .build()

        return DefaultChatModelRouter(langchain4jRouter)
    }

}
