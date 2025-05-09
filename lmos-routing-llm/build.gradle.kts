dependencies {
    api(project(":lmos-routing-core"))

    implementation("dev.langchain4j:langchain4j:1.0.0-beta1")
    api("dev.langchain4j:langchain4j-embeddings:1.0.0-beta2")
    api("dev.langchain4j:langchain4j-hugging-face:1.0.0-beta1")

}
