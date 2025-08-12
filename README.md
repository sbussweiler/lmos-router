<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
[![GitHub Actions Build Status](https://github.com/eclipse-lmos/lmos-router/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/eclipse-lmos/lmos-router/actions/workflows/gradle.yml)
[![GitHub Actions Publish Status](https://github.com/eclipse-lmos/lmos-router/actions/workflows/gradle-publish.yml/badge.svg?branch=main)](https://github.com/eclipse-lmos/lmos-router/actions/workflows/gradle-publish.yml)
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)

# Agent Classifier

The LMOS Agent Classifier library allows you to set up an agent classification system that identifies the most appropriate agent based on the conversation and system context, using the following complementary classifier strategies:

- **Embedding-based Classification**: Finds the most qualified agent using a semantic vector search and a ranking algorithm.
- **LLM-based Classification**: Utilizes a LLM to select the most appropriate agent based on the conversation context.
- **Hybrid Classification:** Combines semantic retrieval with LLM-based reasoning:
  - **Fast-Track Strategy**: First performs the Embedding-based Classification to find a matching agent. If no confident match is found, the system falls back to an LLM. The agents retrieved during the semantic search are passed to the LLM, enabling it to make an informed decision. 
  - **RAG Strategy**: This strategy follows the classic RAG approach. It first retrieves a relevant subset of agents using semantic search. Then, an LLM selects the most appropriate agent from this set.

In the initial version, classification returns a single best-matching agent. A future extension could allow multiple candidates to be considered, including coordination patterns if needed.

# Module Overview

Each classification strategy has a dedicated implementation module as well as a Spring Boot Starter for easy integration if needed.

### `lmos-classifier-core`
Contains the common models and the classifier interfaces.

### `lmos-classifier-vector`

Implementation of the **vector-based classification strategy** using semantic similarity. It retrieves and ranks agents based on embedding similarity.

Spring Boot Integration: `lmos-classifier-vector-spring-boot-starter`

### `lmos-classifier-llm`

Implementation of the **LLM-based classification strategy**, which uses a large language model to select the most suitable agent.

Spring Boot Integration: `lmos-classifier-llm-spring-boot-starter`

### `lmos-classifier-hybrid`

Implements the **hybrid strategies** `FastTrackAgentClassifier` and `RagAgentClassifier`.

Spring Boot Integration: `lmos-classifier-hybrid-spring-boot-starter`

### `lmos-classifier-workbench-demo-controller`

A Spring Boot example application that demonstrates how to configure and use the available classifier strategies via the Spring Boot starter modules.
It exposes all supported classification strategies through HTTP endpoints and serves as a practical reference for testing and comparing different classifier approaches.


# Classifier Guide

This section explains how to use the available classifier implementations. There are two main ways to use the classifiers:

- Programmatic Usage – Directly instantiate and configure classifiers using the provided builders.
- Spring Boot Starter – Use the Spring Boot starter modules to easily configure and wire classifiers through the `application.yaml`.

## Programmatic Usage

All classifier implementations can be instantiated and configured manually. Therefore, each classifier exposes a builder.

Example for LLM-based classification:

```kotlin
val chatModel = LangChainChatModelFactory.createClient(
    ChatModelClientProperties(
        provider = "azure",
        apiKey = "your-api-key",
        baseUrl = "https://model-base-url.com",
        model = "gpt-35-turbo",
        maxTokens = 512,
        temperature = 0.2,
        logRequestsAndResponses = false,
    )
)

val classifier = DefaultModelAgentClassifier
    .builder()
    .withChatModel(chatModel)
    .build()
```

Further examples on how to use the builders and their related components can be found in the Spring Boot starter auto-configuration classes:
`ModelAgentClassifierAutoConfiguration`, `EmbeddingAgentClassifierAutoConfiguration`, `FastTrackAgentClassifierAutoConfiguration`, and `RagAgentClassifierAutoConfiguration`.

Details on available configuration options for LLMs and embedding models can be found in the Spring Boot Starter chapter.

## Spring Boot Starter

### Enable Classifier
The corresponding Spring Boot starter project must be added as a dependency to your application, and the classifier strategies must then be enabled explicitly in the `application.yaml` file.

```yaml
lmos:
  router:
    classifier:
      llm:
        enabled: true
      vector:
        enabled: false
      hybrid-rag:
        enabled: false
      hybrid-fast-track:
        enabled: false
```

Only enabled classifiers will be instantiated. You can activate one or more simultaneously.

### LLM Configuration

If an LLM is involved in the classification process, it must be configured in the `application.yaml`. E.g.:

```yaml
lmos:
  router:
    llm:
      provider: azure_openai
      api-key: your-api-key
      base-url: https://model-base-url.com
      model: gpt-4
      maxChatHistory: 10
````

Refer to the table to see which LLM providers are supported and what configuration options are available for each.

| Provider                | `api-key` | `base-url` | `model`        | Optional Params             |
| ----------------------- | --------- | ---------- | -------------- | --------------------------- |
| `openai`                | ✅         | ❌          | ✅              | `maxTokens`, `temperature` |
| `azure_openai`          | ✅         | ✅          | ✅ (deployment) | `maxTokens`, `temperature` |
| `azure_openai_identity` | ❌         | ✅          | ✅              | `maxTokens`, `temperature` |
| `anthropic`             | ✅         | ❌          | ✅              | `maxTokens`, `temperature` |
| `gemini`                | ✅         | ❌          | ✅              | `maxTokens`, `temperature` |
| `ollama`                | ❌         | ✅          | ✅              | `temperature`              |
| `other`                 | ✅         | ✅          | ✅              | `maxTokens`, `temperature` |

> The `azure_openai_identity` relies on environment-based authentication (Azure Identity SDK).


### Embedding Model Configuration
To enable semantic classification, an embedding model must be configured. Currently, three providers are supported. The required configuration parameters depend on the selected provider and model.

```yaml
lmos:
  router:
    embedding:
      model:
        provider: openai                                           # or huggingface, local_onnx
        base-url: https://my-api.openai.com/v1/embeddings          # Required for openai
        model-name: hugginface-model-name                          # Required for huggingface
        api-key: hugginface-api-key                                # Required for huggingface
        model-path: /path/to/local-model.onnx                      # Required for local_onnx
        tokenizer-path: /path/to/local-tokenizer.json              # Required for local_onnx
```

Refer to the table to see which providers are supported and what configuration options are available for each.

| Provider      | Required Settings              |
| ------------- | ------------------------------ |
| `openai`      | `base-url`                     |
| `huggingface` | `model-name`, `api-key`        |
| `local_onnx`  | `model-path`, `tokenizer-path` |


### Embedding Store Configuration

In addition to an embedding model, a store must be configured to persist and query the vector embeddings.

```yaml
lmos:
  router:
    embedding:
      store:
        host: localhost
        port: 6334
        tlsEnabled: false
        apiKey: my-api-key
```

* Defines the connection to the external embedding store.
* Supports TLS and API key-based authentication.

### Embedding Ranking Configuration

For the Embedding-based Classification, a ranker is used. The goal of the ranker is to determine the most qualified agent based on scores, thresholds, or other heuristics.

There is currently a default implementation (`EmbeddingScoreRanker`) for the ranker, where the agent with the highest cumulative score is only selected if:
- The score difference to the second-best agent exceeds a minimum distance
- The total and mean scores exceed predefined thresholds
- And the relative score difference is sufficiently large

Defaults threshold values are provided, but tuning is highly recommended. Optimal values depend on:
- Number and type of agents
- Embedding model behavior
- Language characteristics
- Well defined capabilities examples

The ranking thresholds can be configured as follows:
```yaml
lmos:
  router:
    embedding:
      ranking:
        maxEmbeddings: 15
        minWeight: 5.0
        minDistance: 4.0
        minMeanScore: 0.5
        minRealDistance: 0.3
```
