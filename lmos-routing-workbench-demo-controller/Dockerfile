FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    curl \
    sudo \
    apt-transport-https \
    ca-certificates \
    gnupg \
    lsb-release \
    openjdk-21-jre \
    && rm -rf /var/lib/apt/lists/*

RUN curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

RUN useradd -m router
USER router
WORKDIR /home/router

COPY --chown=router:router build/libs/lmos-routing-workbench-demo-controller.jar /deployments/app.jar

ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]