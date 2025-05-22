package org.eclipse.lmos.routing.core

/**
 * The [AgentClassifier] classifies a given [UserQuery] to the most appropriate agent by producing an [AgentClassification].
 *
 * @param Query The user query.
 * @param Classification The result of the classification, containing the selected agent ID.
 */
interface AgentClassifier<in Query : UserQuery, out Classification : AgentClassification> {
    /**
     * Classifies the given query and returns the corresponding [AgentClassification].
     *
     * @param query The user query to classify.
     * @return The classification result including the selected agent.
     */
    fun classify(query: Query): Classification
}

/**
 * Base interface representing the user's message intended for agent classification.
 */
interface UserQuery {
    val query: String
}

/**
 * Base interface representing the classified agent.
 */
interface AgentClassification {
    var agentId: String?
}
