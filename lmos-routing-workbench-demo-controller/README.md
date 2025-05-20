# Router Demo Controller
This sample application offers a separate HTTP endpoint for each supported routing approach, allowing you to test them individually.

## Prepare Environment
Prepare your environment by setting the required environment variables in the `.env` file.

#### Capabilities documents
- `CAPABILITIES_DOCUMENTS_DIR` - The path to the directory containing the LMOS-Capability files.
#### Embedding Ranking settings
- `EMBEDDING_RANKING_MAX_EMBEDDINGS` - The maximum number of embeddings to be used.
- `EMBEDDING_RANKING_MIN_WEIGHT` - The minimum weight for the ranking algorithm.
- `EMBEDDING_RANKING_MIN_DISTANCE` - The minimum distance for the ranking algorithm.
- `EMBEDDING_RANKING_MIN_MEAN_SCORE` - The minimum mean score for the ranking algorithm.
- `EMBEDDING_RANKING_MIN_REAL_DISTANCE` - The minimum real distance for the ranking algorithm.
####  Local ONNX embedding model
- `EMBEDDING_MODEL_LOCAL_ENABLED` - If you want to use a local ONNX embedding model, set this to `true`. Otherwise, set it to `false`.
- `EMBEDDING_MODEL_LOCAL_DIR` - The path to the directory containing your local model files.
- `EMBEDDING_MODEL_LOCAL_FILE_NAME` - The name of your local ONNX model file.
- `EMBEDDING_MODEL_LOCAL_TOKENIZER_FILE_NAME` - The name of your local tokenizer file.
####  Local HuggingFace embedding model
- `EMBEDDING_MODEL_HUGGINGFACE_ENABLED`- If you want to use a Hugging Face embedding model via the Inference API, set this to `true`. Otherwise, set it to `false`.
- `EMBEDDING_MODEL_HUGGINGFACE_MODEL_NAME` - The name of the Hugging Face model.
- `EMBEDDING_MODEL_HUGGINGFACE_API_KEY` - The API key for the Hugging Face model.
#### Azure OpenAI Settings
- `LLM_BASE_URL` - The base url to the LLM endpoint.
- `LLM_API_KEY` - The LLM api key.
- `LLM_MODEL_NAME` - THe LLM model name.
#### LLM History settings
- `LLM_MAX_CHAT_HISTORY` - The maximum number of chat history messages to be used in the LLM prompt.


Note: Either a local model or a Hugging Face model can be used. 

## Run Application 

Run the application by using Docker Compose: 

```
docker-compose up
```

## Manage Embeddings

### Create Embeddings

Read all LMOS-Capability files from `CAPABILITIES_DOCUMENTS_DIR` and create embeddings in a vector database.

**Curl Command:**

```sh
curl -X POST "http://localhost:8080/api/v1/embeddings"
```

### Delete All Embeddings

Deletes all stored embeddings from the vector database.

**Curl Command:**

```sh
curl -X DELETE "http://localhost:8080/api/v1/embeddings"
```

## Agent Routing

This sample application offers a separate endpoint for each supported routing approach, allowing you to test them individually.

### LLM-based routing

**Curl Command:**

```sh
curl --location 'http://localhost:8080/api/v1/routings/llm' \
--header 'Content-Type: application/json' \
--data '{
    "query": "I want to see my bill",
    "conversationId": "myUser",
    "agents": [
        {
            "id": "urn:my-company:agent:billing",
            "capabilities": [
                {
                    "id": "urn:my-company:capability:billing:view",
                    "description": "Allows a customer to retrieve and view their latest ..."
                },
                {
                    "id": "urn:my-company:capability:billing:address:change",
                    "description": "Allows a customer to change his billing address ..."
                }
            ]
        },
        {
            "id": "urn:my-company:agent:sales",
            "capabilities": [
                {
                    "id": "urn:my-company:capability:sales:upgrade-plan",
                    "description": "Helps a customer upgrade their existing service plan"
                },
                {
                    "id": "urn:my-company:capability:sales:view-offers",
                    "description": "Allows a customer to browse and view current promotions, discounts, and available offers"
                }
            ]
        }
    ]
}'
```

**Sample Response:**
```json
{
    "agentId": "urn:my-company:agent:billing"
}
```

### Vector-based routing

Performs a semantic vector search to compare the user query with available agent capability examples, identifying the closest match based on score rankings.

**Curl Command:**

```sh
curl --location 'http://localhost:8080/api/v1/routings/vector' \
--header 'Content-Type: application/json' \
--data '{
    "query": "I want to view my bill",
    "tenant": "telekom"
}'
```

**Sample Response:**

```json
{
  "agentId": "urn:my-company:agent:billing",
  "consideredAgents": [
    {
      "id": "urn:my-company:agent:billing",
      "capabilities": [
        {
          "id": "urn:my-company:capability:billing:view",
          "description": "Allows a customer to retrieve and view their latest invoices and billing information."
        },
        {
          "id": "urn:my-company:capability:billing:address:change",
          "description": "Allows a customer to change their billing address."
        }
      ]
    },
    {
      "id": "urn:my-company:agent:sales",
      "capabilities": [
        {
          "id": "urn:my-company:capability:sales:upgrade-plan",
          "description": "Helps a customer upgrade their existing service plan."
        }
      ]
    }
  ]
}
```


### Hybrid routing

Combines LLM-based and vector-based approaches. It first attempts to find a matching agent via semantic search. If no confident match is found, the LLM is consulted with the agent capabilities from the vector search.

**Curl Command:**

```sh
curl --location 'http://localhost:8080/api/v1/routings/hybrid' \
--header 'Content-Type: application/json' \
--data '{
    "query": "I want to view my bill",
    "tenant": "telekom",
    "conversationId": "myUser"
}'
```

**Sample Response:**

```json
{
  "agentId": "urn:my-company:agent:billing",
  "consideredAgents": [
    {
      "id": "urn:my-company:agent:billing",
      "capabilities": [
        {
          "id": "urn:my-company:capability:billing:view",
          "description": "Allows a customer to retrieve and view their latest invoices and billing information."
        },
        {
          "id": "urn:my-company:capability:billing:address:change",
          "description": "Allows a customer to change their billing address."
        }
      ]
    },
    {
      "id": "urn:my-company:agent:sales",
      "capabilities": [
        {
          "id": "urn:my-company:capability:sales:upgrade-plan",
          "description": "Helps a customer upgrade their existing service plan."
        }
      ]
    }
  ]
}
```

### RAG-LLM-based routing

LLM-based agent routing with RAG. Relevant agent capabilities are first retrieved using semantic vector search, then injected into the LLM prompt to provide context-aware, to select the most appropriate agent.


**Curl Command:**

```sh
curl --location 'http://localhost:8080/api/v1/routings/rag-llm' \
--header 'Content-Type: application/json' \
--data '{
    "query": "I want to view my bill",
    "tenant": "telekom",
    "conversationId": "myUser"
}'
```

**Sample Response:**

```json
{
  "agentId": "urn:my-company:agent:billing"
}
```




## Analyse Embeddings

For analysis purposes, the stored embeddings from the database can be displayed in raw format

**Curl Command:**

```sh
curl --location 'http://localhost:8080/api/v1/routings/vector/plain' \
--header 'Content-Type: application/json' \
--data '{
    "query": "I want to view my bill",
    "tenant": "telekom"
}'
```

**Sample Response:**

```json
[
  {
    "example": "show my bill",
    "score": 0.9602068288916702,
    "agentId": "urn:my-company:agent:billing",
    "capabilityId": "urn:my-company:capability:billing:view",
    "capabilityVersion": "1.0",
    "capabilityDescription": "Allows a customer to retrieve and view their latest invoices and billing information."
  },
  {
    "example": "show my bill",
    "score": 0.9602068288916702,
    "agentId": "urn:my-company:agent:billing",
    "capabilityId": "urn:my-company:capability:billing:view",
    "capabilityVersion": "1.0",
    "capabilityDescription": "Allows a customer to retrieve and view their latest invoices and billing information."
  },
  {
    "example": "show my bill",
    "score": 0.9602068288916702,
    "agentId": "urn:my-company:agent:billing",
    "capabilityId": "urn:my-company:capability:billing:view",
    "capabilityVersion": "1.0",
    "capabilityDescription": "Allows a customer to retrieve and view their latest invoices and billing information."
  }
]
```

