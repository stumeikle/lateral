
A Lateral example, using a hazelcast distributed cache.
Comprising the following parts:

-- libDomain: the domain definition
-- serverApplication: a (skeletal) REST endpoint, used to create entries into the hazelcast cache
-- serverDbDumper: an application which captures changes to the cache and persists to db

Db transactions occur asynchronously to the distributed cache updates in this example
Db uses Guids for most entity ids in this example