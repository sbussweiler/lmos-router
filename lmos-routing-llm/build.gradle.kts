val langChain4jCoreVersion: String by project
val langChain4jModulesVersion: String by project

dependencies {
    api(project(":lmos-routing-core"))

    implementation("dev.langchain4j:langchain4j:$langChain4jCoreVersion")
    api("dev.langchain4j:langchain4j-embeddings:$langChain4jModulesVersion")
    api("dev.langchain4j:langchain4j-hugging-face:$langChain4jModulesVersion")

    implementation("dev.langchain4j:langchain4j-open-ai:1.0.1")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
    implementation("com.azure:azure-identity:1.16.1")
}
