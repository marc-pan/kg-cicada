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
vCount = g.V().count().next()

println "################################"
println "Data lineage vertex count: " + vCount

//subGraph = g.E().hasLabel('context').subgraph('subGraph').cap('subGraph').next()
//subGraph = g.V().has('name', 'Knowledge Accelerator for Healthcare').inE('context').where(outV().has('type', '_built_in###business_category')).subgraph('x').outV().inE('context').where(outV().has('type', '_built_in###business_category')).subgraph('x').outV().inE('context').where(outV().has('type', '_built_in###business_category')).subgraph('x').outV().inE('context').where(outV().has('type', '_built_in###business_category')).subgraph('x').cap('x').next()
subGraph = g.V().has('name', 'Default').repeat(__.inE('context').where(outV().has('type', '_built_in###business_category')).subgraph('x').outV()).times(3).cap('x').next()
g.V().has('type', '_built_in###business_category').has('name', 'Marc_Banking').repeat(__.inE('context').has('deleted', false).outV()).until(__.inE().count().is(0)).count()
sg = subGraph.traversal()
sgVCount = sg.V().count().next()
sgECount = sg.E().count().next()
println "Category graph vertex count: " + sgVCount
println "Category graph edge count: " + sgECount

path = "/tmp/janusgraph-0.6.0/graph.json"
g.io(path).with(IO.registry, JanusGraphIoRegistry.getInstance()).write().iterate()

graph.close()
