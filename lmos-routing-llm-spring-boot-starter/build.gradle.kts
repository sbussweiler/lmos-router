val springBootVersion: String by project
val langChain4jModulesVersion: String by project

dependencies {
    api(project(":lmos-routing-core"))
    api(project(":lmos-routing-llm"))
    api(project(":lmos-routing-core-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    implementation("dev.langchain4j:langchain4j-azure-open-ai-spring-boot-starter:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-qdrant:$langChain4jModulesVersion")
}

