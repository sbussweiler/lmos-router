dependencies {
    api(project(":lmos-routing-core"))

    implementation("dev.langchain4j:langchain4j:1.0.0-beta1")
    api("dev.langchain4j:langchain4j-embeddings:1.0.0-beta2")
    api("dev.langchain4j:langchain4j-hugging-face:1.0.0-beta1")

    implementation("dev.langchain4j:langchain4j-open-ai:0.36.2")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:0.36.2")
    implementation("dev.langchain4j:langchain4j-anthropic:0.36.2")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:0.36.2")
    implementation("dev.langchain4j:langchain4j-ollama:0.36.2")
    implementation("com.azure:azure-identity:1.16.0")
}
