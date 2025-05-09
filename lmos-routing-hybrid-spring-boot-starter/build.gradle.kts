val springBootVersion: String by rootProject.extra

dependencies {
    api(project(":lmos-routing-hybrid"))
    api(project(":lmos-routing-core-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    implementation("dev.langchain4j:langchain4j-azure-open-ai-spring-boot-starter:1.0.0-beta2") //
}
