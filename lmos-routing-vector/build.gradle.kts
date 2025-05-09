dependencies {
    api(project(":lmos-routing-core"))

    api("dev.langchain4j:langchain4j-embeddings:1.0.0-beta2")
    api("dev.langchain4j:langchain4j-hugging-face:1.0.0-beta1")

    implementation("dev.langchain4j:langchain4j-qdrant:1.0.0-beta1")

}
