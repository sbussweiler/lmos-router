import org.eclipse.lmos.routing.llm.defaultSystemPrompt
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.router.llm")
data class ChatModelProperties(
    val maxChatHistory: Int = 10,
    val systemPrompt: String = defaultSystemPrompt(),
)