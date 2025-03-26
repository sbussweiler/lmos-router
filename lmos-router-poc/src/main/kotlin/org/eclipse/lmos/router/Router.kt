package org.eclipse.lmos.router

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.spring.AiService


@AiService
interface Router {

    @SystemMessage("""
            You are an AI tasked with selecting the most suitable agent to address a user query based on the agents' examples. 
            You will be provided with a list of agents and their capabilities (capability Id, name, description and examples), followed by a user query. 
            Your goal is to analyze the query and match it with the most appropriate example of a capability.

            To select the most suitable capability, follow these steps:

            1. Carefully read and understand the user query.
            2. Review the list of examples.
            3. Analyze how well each example matches the requirements of the user query.
            4. Consider factors such as relevance, expertise, and specificity of the examples in relation to the query.
            5. Select the agent whose examples best align with the user's needs.

            Select the most suitable agent and their capabilities based on the provided examples. 
            Your response format strictly follows this JSON structure:

            ```json
            {
                "agents": [
                    {
                        "name": "<agentId>",
                        "capabilities": ["<capabilityId>"]
                    }
                ]
            }

            Ensure that the names of agents and capabilities you provide exactly matches the provided agentId and capabilityId. 
            If there is no matching agent, please return an empty agent array.
            Do not include any additional explanation or justification in your response.
            """)
    fun route(query: String): RoutingResult

}

data class RoutingResult @JsonCreator constructor (
    @JsonProperty("agents") val agents: List<Agent>
)

data class Agent @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("capabilities") val capabilities: List<String> = emptyList()
)
