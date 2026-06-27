# Metadata Cache

In-memory metadata index cache on the Java MCP server.

## ADDED Requirements

### Requirement: Flat index cached per connection

The MCP server SHALL maintain an in-memory flat metadata index per configured connection, built from the 1C `metadata?mode=list` API for P0 metadata types.

#### Scenario: Index built on first search

- **WHEN** `find_objects` is called for a connection with no cached index
- **THEN** the server fetches the full object list from 1C (paginated if needed)
- **AND** stores the result in memory keyed by connection name
- **AND** subsequent `find_objects` calls for the same connection search the cached index without calling 1C search endpoint

### Requirement: Cache expires after configured TTL

The metadata index cache SHALL expire after a configurable TTL and be rebuilt on next access.

#### Scenario: Cache hit within TTL

- **WHEN** `find_objects` is called within the TTL period after index was built
- **THEN** the server searches the in-memory index
- **AND** does not call 1C to rebuild the index

#### Scenario: Cache miss after TTL expiry

- **WHEN** the TTL has elapsed since the index was built
- **AND** `find_objects` is called
- **THEN** the server rebuilds the index from 1C before searching

### Requirement: Describe requests bypass index cache

The server SHALL NOT cache `describe_object` responses on the Java side in this change.

#### Scenario: Describe always calls 1C

- **WHEN** `describe_object` is called
- **THEN** the server calls `GET /hs/datamcp/v1/objects/{type}/{name}` on 1C directly
- **AND** does not serve a cached describe response
