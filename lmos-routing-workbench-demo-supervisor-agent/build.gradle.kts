val langChain4jCoreVersion: String by project
val langChain4jModulesVersion: String by project
val jacksonVersion: String by project

plugins {
    val kotlinVersion = "2.1.20"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false

    kotlin("plugin.spring") version "2.0.20"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.graalvm.buildtools.native") version "0.10.2"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xcontext-receivers")
    }
}

dependencies {
    val arcVersion = "0.1.0-SNAPSHOT"
    val langchain4jVersion = "0.36.2"

    // Arc
    implementation("org.eclipse.lmos:arc-azure-client:$arcVersion")
    implementation("org.eclipse.lmos:arc-spring-boot-starter:$arcVersion")
    implementation("org.eclipse.lmos:arc-reader-pdf:$arcVersion")
    implementation("org.eclipse.lmos:arc-reader-html:$arcVersion")
    implementation("org.eclipse.lmos:arc-assistants:$arcVersion")
    implementation("org.eclipse.lmos:arc-reader-html:$arcVersion")
    implementation("org.eclipse.lmos:arc-api:$arcVersion")
    implementation("org.eclipse.lmos:arc-graphql-spring-boot-starter:$arcVersion")

    implementation(project(":lmos-routing-llm-spring-boot-starter"))
    implementation(project(":lmos-routing-vector-spring-boot-starter"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // Tracing
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin")
    implementation("com.google.protobuf:protobuf-java:3.25.1")
    implementation("io.opentelemetry.proto:opentelemetry-proto:1.3.2-alpha")

    // Azure
    implementation("com.azure:azure-identity:1.13.1")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Langchain4j
    implementation("dev.langchain4j:langchain4j-qdrant:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-bedrock:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jCoreVersion")

    // Metrics
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb:1.19.7")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
