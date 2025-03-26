# LMOS Router Service

## Prepare Environment
Prepare your environment by setting the required environment variables in the `.env` file.
- `CAPABILITIES_DOCUMENTS_DIR` - The path to the directory containing the LMOS-Capability files.
- `EMBEDDING_MODEL_LOCAL_ENABLED` - If you want to use a local ONNX embedding model, set this to `true`. Otherwise, set it to `false`.
  - `EMBEDDING_MODEL_LOCAL_DIR` - The path to the directory containing your local model files.
  - `EMBEDDING_MODEL_LOCAL_FILE_NAME` - The name of your local ONNX model file.
  - `EMBEDDING_MODEL_LOCAL_TOKENIZER_FILE_NAME` - The name of your local tokenizer file.
- `EMBEDDING_MODEL_HUGGINGFACE_ENABLED`- If you want to use a Hugging Face embedding model via the Inference API, set this to `true`. Otherwise, set it to `false`.
    - `EMBEDDING_MODEL_HUGGINGFACE_MODEL_NAME` - The name of the Hugging Face model.
    - `EMBEDDING_MODEL_HUGGINGFACE_API_KEY` - The API key for the Hugging Face model.
- `AZURE_OPENAI_ENDPOINT` - Your Azure OpenAI endpoint.
- `AZURE_OPENAI_API_KEY` - Your Azure OpenAI API key.
- `AZURE_OPENAI_MODEL_NAME` - Your Azure OpenAI model name.

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

### Embedding-Based Agent Routing

Process a query using the stored embeddings to determine the relevant agents the capabilities.

**Curl Command:**

```sh
curl -X POST "http://localhost:8080/api/v1/routings/embeddings" \
     -H "Content-Type: application/json" \
     -d '{
           "query": "I want to view my bill and buy a router"
         }'
```

**Sample Response:**

```json
{
    "agents": [
        {
            "name": "urn:telekom:agent:ordering",
            "capabilities": [
                "urn:telekom:capability:ordering:place-order"
            ]
        },
        {
            "name": "urn:telekom:agent:billing",
            "capabilities": [
                "urn:telekom:capability:billing:view"
            ]
        }
    ]
}
```

### LLMBased Agent Routing

Process a query using a LLM with RAG.

**Curl Command:**

```sh
curl -X POST "http://localhost:8080/api/v1/routings/llms" \
     -H "Content-Type: application/json" \
     -d '{
           "query": "I want to view my bill and buy a router"
         }'
```

**Sample Response:**

```json
{
    "agents": [
        {
            "name": "urn:telekom:agent:billing",
            "capabilities": [
                "urn:telekom:capability:billing:view"
            ]
        },
        {
            "name": "urn:telekom:agent:ordering",
            "capabilities": [
                "urn:telekom:capability:ordering:place-order"
            ]
        }
    ]
}
```

## Analyse Embeddings

For analysis purposes, the stored embeddings from the database can be displayed in raw format

**Curl Command:**

```sh
curl -X POST "http://localhost:8080/api/v1/routings/embeddings/plain" \
     -H "Content-Type: application/json" \
     -d '{
           "query": "I want to view my bill and buy a router"
         }'
```

**Sample Response:**

```json
[DefaultContent[textSegment=TextSegment { text = "Purchase a router" metadata = {agentId=urn:telekom:agent:ordering, capabilityName=Place Order, capabilityId=urn:telekom:capability:ordering:place-order, capabilityDescription=Allows a customer to place a new order for products or services, index=0, capabilityVersion=1.0
}
}, metadata={SCORE=0.9337242798454917, EMBEDDING_ID=4f82596b-f851-4eb7-9e4f-de03a19cce3a
  }
], DefaultContent[textSegment=TextSegment { text = "show my bill" metadata = {agentId=urn:telekom:agent:billing, capabilityName=View Bills, capabilityId=urn:telekom:capability:billing:view, capabilityDescription=Allows a customer to retrieve and view their latest ..., index=0, capabilityVersion=1.0
}
}, metadata={SCORE=0.8998691390843978, EMBEDDING_ID=c35ef124-f91d-4780-b974-9409c14dec2c
  }
], DefaultContent[textSegment=TextSegment { text = "Order internet service" metadata = {agentId=urn:telekom:agent:ordering, capabilityName=Place Order, capabilityId=urn:telekom:capability:ordering:place-order, capabilityDescription=Allows a customer to place a new order for products or services, index=0, capabilityVersion=1.0
}
}, metadata={SCORE=0.8722086522380023, EMBEDDING_ID=5e2927ae-2227-4b22-8cb1-dbe80922f145
  }
]
]
```

