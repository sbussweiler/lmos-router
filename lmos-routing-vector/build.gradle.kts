val langChain4jModulesVersion: String by project
val jacksonVersion: String by project

dependencies {
    api(project(":lmos-routing-core"))

    api("dev.langchain4j:langchain4j-embeddings:$langChain4jModulesVersion")
    api("dev.langchain4j:langchain4j-hugging-face:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-qdrant:$langChain4jModulesVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
}
