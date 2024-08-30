import org.janusgraph.core.JanusGraphFactory
import org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry

config = new BaseConfiguration()
config.setProperty('gremlin.graph', 'org.janusgraph.core.JanusGraphFactory')
config.setProperty('storage.backend', 'org.janusgraph.diskstorage.foundationdb.FoundationDBStoreManager')
config.setProperty('query.batch', true)
config.setProperty('storage.fdb.isolation-level', 'read_committed_with_write')
config.setProperty('storage.fdb.version', '620')
config.setProperty('schema.default', 'none')
config.setProperty('storage.fdb.get-range-mode', 'iterator')
config.setProperty('storage.fdb.directory', 'kg__999__datalineage')

graph = JanusGraphFactory.open(config)
g = graph.traversal()
:set max-iteration 1000
