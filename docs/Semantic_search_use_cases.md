# Semantic Search Use Cases

## Graph edge mapping to semantic message

| Source vertex | Target vertex | Edge label | Edge direction | Semantic message key name |
| --- | --- | --- | --- | --- |
| Business term | Business term | is_a_type_of | outgoing | child |
| Business term | Business term | is_a_type_of | incoming | parent |
| Business term | Business term | is_a_type_of | outgoing | grandchild |
| Business term | Business term | is_a_type_of | incoming | grandparent |
| Business term | Business term | related | outgoing | directly_related |
| Business term | Data class | related | outgoing | loosely_related |
| Business term | Business term | synonym | outgoing | equivalent |

## Vertex properties mapping to semantic entity metadata

| Vertex Property | Entity Metadata |
| --- | --- |
| name | name |
| aliases | abbreviations |
| type | artifact_type |
| category id | container_id |
| category name | container_name |
| id | artifact_id |
| category guid | artifact_guid |

**CAUTION** we have to verify the edge direction for child and parent in the graph, and all the vertex properties

## Use Case List
1. A new relationship creation between business terms, the relationship can be any one of `is_a_type_of` edge in/out 1 hop and 2 hop, `synonym`, `related`.
2. A new relationship creation from business term to data class, the relationship is `related` edge out
3. A business term update includes name, description, abbreviations
4. A relationship deletion between business terms or from business term to data class
5. A business term deletion
6. 

## E2E Cases
### Query knowledge graph for new relationship between business terms

### Query knowledge graph for del relationship between business terms

### Query Knowledge graph for business term deletion

### Query knowledge graph for business term name changes


## References
- [Epic#63341: Semantic Search: Terms](https://github.ibm.com/wdp-gov/tracker/issues/63341)
- [Task#83392](https://github.ibm.com/wdp-gov/tracker/issues/83392)
- [Task#83394](https://github.ibm.com/wdp-gov/tracker/issues/83394)
- [Task#70211](https://github.ibm.com/wdp-gov/tracker/issues/70221)
