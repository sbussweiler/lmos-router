# Supervisor Routing Agent
The Supervisor Routing Agent is a sample application designed to test the routing functionality. For each user input, 
it determines the most suitable agent responsible for handling the request. The available agents and their capabilities 
are defined in the `./capabilities` directory. The application scans the directory on each startup and generates embeddings for the capabilities and examples.

## Prepare Environment
Prepare your environment by setting the required environment variables in the `.env` file. 
The following environment variables configure the embedding model and the LLM used by the application.

You can choose between a local ONNX embedding model or a Hugging Face embedding model. 

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
- `AZURE_OPENAI_ENDPOINT` - Your Azure OpenAI endpoint.
- `AZURE_OPENAI_API_KEY` - Your Azure OpenAI API key.
- `AZURE_OPENAI_MODEL_NAME` - Your Azure OpenAI model name.

## Run Application 

Run the application by using Docker Compose: 

```
docker-compose up
```

Open the [ARC View](https://eclipse.dev/lmos/chat/index.html?agentUrl=http://localhost:8080#/chat) to chat with the supervisor agent.
