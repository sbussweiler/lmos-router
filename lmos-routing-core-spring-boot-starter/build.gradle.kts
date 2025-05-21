val springBootVersion: String by project

dependencies {
    api(project(":lmos-routing-core"))
    api(project(":lmos-routing-vector"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
}
