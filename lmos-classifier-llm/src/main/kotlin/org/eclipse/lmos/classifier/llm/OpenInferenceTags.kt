// SPDX-FileCopyrightText: 2026 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import org.eclipse.lmos.classifier.core.tracing.Tags

object OpenInferenceTags {
    fun applyModelTracingTags(
        tags: Tags,
        chatRequest: ChatRequest,
        chatResponse: ChatResponse,
    ) {
        val model = chatResponse.modelName()
        val temperature = chatRequest.temperature()
        tags.tag("openinference.span.kind", "LLM")
        tags.tag("llm.model_name", model)
        tags.tag(
            "llm.invocation_parameters",
            """{"model_name": "$model", "temperature": "$temperature"}""",
        )
        tags.tag("input.mime_type", "text/plain")

        chatRequest.messages().forEachIndexed { i, message ->
            val content =
                when (message) {
                    is UserMessage -> message.singleText()
                    is AiMessage -> message.text()
                    is SystemMessage -> message.text()
                    is ToolExecutionResultMessage -> message.text()
                    else -> null
                }
            content?.let { tags.tag("llm.input_messages.$i.message.content", it) }
            val role =
                when (message) {
                    is UserMessage -> "user"
                    is AiMessage -> "assistant"
                    is SystemMessage -> "system"
                    is ToolExecutionResultMessage -> "function"
                    else -> "unknown"
                }
            tags.tag("llm.input_messages.$i.message.role", role)
            if (i == chatRequest.messages().size - 1) {
                tags.tag("input.value", content.toString())
                tags.tag("message.content", content.toString())
                tags.tag("message.role", role)
                tags.tag("messagecontent.type", "text")
                tags.tag("messagecontent.text", content.toString())
            }
        }

        tags.tag("llm.output_messages.0.message.content", chatResponse.aiMessage().text())
        tags.tag("llm.output_messages.0.message.role", "assistant")

        tags.tag("llm.token_count.prompt", chatResponse.tokenUsage().inputTokenCount().toLong())
        tags.tag("llm.token_count.completion", chatResponse.tokenUsage().outputTokenCount().toLong())
        tags.tag("llm.token_count.total", chatResponse.tokenUsage().totalTokenCount().toLong())
    }
}
