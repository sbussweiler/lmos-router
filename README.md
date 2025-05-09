# Intelligent Agent Routing System
The LMOS Agent Router library allows to route user queries to the most suitable agent based on their capabilities, using four complementary strategies:

- **LLM-based routing**: Utilizes a LLM to select the most suitable agent based on a given user query and a list of agent’s capabilities.
- **Vector-based routing**: Performs a semantic vector search to compare the user query with available agent capability examples, identifying the closest match based on score rankings.
- **Hybrid routing**: Combines LLM-based and vector-based approaches. It first attempts to find a matching agent via semantic search. If no confident match is found,
the LLM is consulted with the agent capabilities from the vector search.
- **RAG-LLM-based routing**: LLM-based agent routing with RAG. Relevant agent capabilities are first retrieved using semantic vector search, then injected into the LLM prompt to provide context-aware, to select the most appropriate agent.

More documentation to follow...

## Contributing

Contributions are welcome! Please read the [contributing guidelines](Contributing.md) for more information.

## Code of Conduct

This project has adopted the [Contributor Covenant](https://www.contributor-covenant.org/) in version 2.1 as our code of conduct. Please see the details in our [CodeOfConduct.md](CodeOfConduct.md). All contributors must abide by the code of conduct.

By participating in this project, you agree to abide by its [Code of Conduct](./CodeOfConduct.md) at all times.

## Licensing
Copyright (c) 2025 Deutsche Telekom AG and others.

Sourcecode licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this project except in compliance with the License.

This project follows the [REUSE standard for software licensing](https://reuse.software/).    
Each file contains copyright and license information, and license texts can be found in the [./LICENSES](./LICENSES) folder. For more information visit https://reuse.software/.   

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the LICENSE for the specific language governing permissions and limitations under the License.

