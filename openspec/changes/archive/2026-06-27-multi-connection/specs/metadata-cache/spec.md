## ADDED Requirements

### Requirement: Cache entries isolated between connections

The metadata index cache SHALL maintain separate entries per connection name without cross-contamination.

#### Scenario: Independent cache per connection

- **WHEN** `find_objects` is called for connection `ut` and builds the index cache
- **AND** `find_objects` is subsequently called for connection `ut-copy` with the same search query
- **THEN** the server builds or uses a separate cache entry keyed by `ut-copy`
- **AND** cache state for `ut` is not modified by operations on `ut-copy`
