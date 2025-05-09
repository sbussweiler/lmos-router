FROM registry.access.redhat.com/ubi9/openjdk-21-runtime
USER 185
COPY build/libs/lmos-routing-workbench-demo-controller.jar /deployments/app.jar
ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]