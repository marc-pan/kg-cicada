package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.wdp.gs.kg.BuildFDBConfiguration;
import com.ibm.wdp.gs.kg.Entity;

import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.commons.io.FileUtils;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.JanusGraphManagement.IndexBuilder;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.awaitility.Awaitility;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public class GraphSchemaAndData {
    private static final Logger logger = LoggerFactory.getLogger(GraphSchemaAndData.class);

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final Set<String> indexVertex = Sets.newHashSet("id", "name", "state", "description", "ontology", "type",
                                                                    "types", "abbreviations", "alternative_names", "last_updated",
                                                                    "producer_property", "deleted", "recent", "context_id", "provider",
                                                                    "checksum", "display_name", "_marker_id", "_marker_name");
    private static final Set<String> indexEdgeLabel = Sets.newHashSet("acronyms", "contains", "is_a_type_of", "has_type", "related", "synonym", "context");
    private static JanusGraph graph;

    public static void createSchema(JanusGraph graph) {
        try {
            JanusGraphManagement mgmt = graph.openManagement();
            // prepare a exist schema
            // create properties
            mgmt.makePropertyKey("provider").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("producer_property").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("transaction_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("types").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("wkc_catalog_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("wkc_asset_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("wkc_tags").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("_display_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("_lower_case_display_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("_proplabel").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("abbreviations").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("alternative_names").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("avgRating").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("checksum").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("count").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("created").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("custom_properties").dataType(String.class).cardinality(Cardinality.LIST).make();
            mgmt.makePropertyKey("description").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("display_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("edges").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("feature_last_updated").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("feature_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("feature_score").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("features").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("foreign_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("last_updated").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("ontology").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("origin").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("promoted_from").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("score").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("scores_last_updated").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("timestamp").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("totalComments").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("totalRatings").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("tz_offset").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("url").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("weight").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("wkc_owner_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("_marker_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("_marker_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("deleted").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("recent").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("context_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("type").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("state").dataType(String.class).cardinality(Cardinality.SINGLE).make();

            // create edge label
            mgmt.makeEdgeLabel("acronyms").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("assigned").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("contains").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_a_type_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("has_type").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_a_child_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_a_parent_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_steward_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("owns").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("referenceing").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("related").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("synonym").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("context").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("referencing").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.commit();
            graph.tx().commit();
        } catch (Exception e) {
            logger.error(e.toString());
            Thread.currentThread().interrupt();
            System.exit(1);
        } finally {
            graph.tx().close();
        }
    }

    public static void createOntology(JanusGraph graph, String ontgFile) {
        String ontologyFile = (ontgFile == null || ontgFile.isEmpty())? "test/wkc-ontology-v4.owl": ontgFile;

        try {
            logger.info("create node to store: ontology, edgeLabels, propertyKeys, conflicts");
            JanusGraphVertex ontology = graph.addVertex();
            String path = GraphSchemaAndData.class.getClassLoader().getResource(ontologyFile).getPath();
            File file = new File(path);
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ontology.property("id", "tbox");
            ontology.property("ontology", text);
            JanusGraphVertex edgeLabels = graph.addVertex();
            edgeLabels.property("id", "edgeLabels");
            edgeLabels.property("types", "acronyms");
            edgeLabels.property("types", "assigned");
            edgeLabels.property("types", "contains");
            edgeLabels.property("types", "is_of");
            edgeLabels.property("types", "is_a_type_of");
            edgeLabels.property("types", "has_type");
            edgeLabels.property("types", "is_a_child_of");
            edgeLabels.property("types", "is_a_parent_of");
            edgeLabels.property("types", "is_steward_of");
            edgeLabels.property("types", "owns");
            edgeLabels.property("types", "referenceing");
            edgeLabels.property("types", "related");
            edgeLabels.property("types", "synonym");
            edgeLabels.property("types", "context");
            edgeLabels.property("types", "referencing");
            JanusGraphVertex propertyKeys = graph.addVertex();
            propertyKeys.property("id", "propertyKeys");
            propertyKeys.property("types", "id");
            propertyKeys.property("types", "name");
            propertyKeys.property("types", "state");
            propertyKeys.property("types", "description");
            propertyKeys.property("types", "alternative_names");
            propertyKeys.property("types", "abbreviations");
            propertyKeys.property("types", "type");
            propertyKeys.property("types", "score");
            propertyKeys.property("types", "producer_property");
            propertyKeys.property("types", "last_updated");
            propertyKeys.property("types", "deleted");
            propertyKeys.property("types", "provider");
            propertyKeys.property("types", "recent");
            propertyKeys.property("types", "checksum");
            propertyKeys.property("types", "context_id");
            propertyKeys.property("types", "display_name");
            propertyKeys.property("types", "_marker_name");
            propertyKeys.property("types", "_marker_id");
            JanusGraphVertex conflicts = graph.addVertex();
            conflicts.property("id", "conflicts");
            graph.tx().commit();

            Thread.sleep(2000);
        } catch (InterruptedException | IOException e) {
            logger.error(e.toString());
            Thread.currentThread().interrupt();
            System.exit(1);
        } finally {
            graph.tx().close();
        }
        logger.info("schema and ontology are created successfull.");
    }

    public static void createSchemaAndData(JanusGraph graph, String ontgFile, String dataFile) {
        String ontologyFile = (ontgFile == null || ontgFile.isEmpty())? "test/wkc-ontology-v4.owl": ontgFile;
        String simpleData = (dataFile == null || dataFile.isEmpty())? "test/data_N1.json": dataFile;
        try {
            JanusGraphManagement mgmt = graph.openManagement();
            // prepare a exist schema
            // create properties
            mgmt.makePropertyKey("id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("state").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("description").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("ontology").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("type").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("types").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("alternative_names").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("abbreviations").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("last_updated").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("producer_property").dataType(String.class).cardinality(Cardinality.SET).make();
            mgmt.makePropertyKey("deleted").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("provider").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("recent").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("checksum").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("context_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("display_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("_marker_name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
            mgmt.makePropertyKey("_marker_id").dataType(String.class).cardinality(Cardinality.SINGLE).make();

            // create edge label
            mgmt.makeEdgeLabel("acronyms").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("assigned").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("contains").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_a_type_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("has_type").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_a_child_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_a_parent_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("is_steward_of").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("owns").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("referenceing").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("related").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("synonym").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("context").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.makeEdgeLabel("referencing").multiplicity(Multiplicity.SIMPLE).make();
            mgmt.commit();

            // ingest data
            String path = GraphSchemaAndData.class.getClassLoader().getResource(simpleData).getPath();
            File file = new File(path);
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(text);

            // create class nodes
            logger.info("create class nodes.");
            JSONArray classes = obj.getJSONArray("classes");
            for (int i = 0; i < classes.length(); i++) {
                JanusGraphVertex v = graph.addVertex();
                v.property("name", classes.getJSONObject(i).getString("name"));
                v.property("id", classes.getJSONObject(i).getString("id"));
            }

            // create vertices
            logger.info("create vertices.");
            JSONArray vertices = obj.getJSONArray("vertices");
            for (int i = 0; i < vertices.length(); i++) {
                JanusGraphVertex v = graph.addVertex();
                v.property("name", vertices.getJSONObject(i).getString("name"));
                v.property("id", vertices.getJSONObject(i).getString("id"));
                v.property("type", vertices.getJSONObject(i).getString("type"));
                if (vertices.getJSONObject(i).has("state")) {
                    v.property("state", vertices.getJSONObject(i).getString("state"));
                }
                if (vertices.getJSONObject(i).has("abbreviations")) {
                    vertices.getJSONObject(i).getJSONArray("abbreviations").forEach(item -> v.property("abbreviations", item));
                }
                if (vertices.getJSONObject(i).has("alternative_names")) {
                    vertices.getJSONObject(i).getJSONArray("alternative_names").forEach(item -> v.property("alternative_names", item));
                }
                if (vertices.getJSONObject(i).has("producer_property")) {
                    vertices.getJSONObject(i).getJSONArray("producer_property").forEach(item -> v.property("producer_property", item));
                }
                if (vertices.getJSONObject(i).has("last_updated")) {
                    v.property("last_updated", vertices.getJSONObject(i).getString("last_updated"));
                }
                if (vertices.getJSONObject(i).has("deleted")) {
                    v.property("deleted", vertices.getJSONObject(i).getBoolean("deleted"));
                }
                if (vertices.getJSONObject(i).has("provider")) {
                    vertices.getJSONObject(i).getJSONArray("provider").forEach(item -> v.property("provider", item));
                }
                if (vertices.getJSONObject(i).has("recent")) {
                    vertices.getJSONObject(i).getJSONArray("recent").forEach(item -> v.property("recent", item));
                }
                if (vertices.getJSONObject(i).has("checksum")) {
                    vertices.getJSONObject(i).getJSONArray("checksum").forEach(item -> v.property("checksum", item));
                }
                if (vertices.getJSONObject(i).has("context_id")) {
                    vertices.getJSONObject(i).getJSONArray("context_id").forEach(item -> v.property("context_id", item));
                }
                if (vertices.getJSONObject(i).has("display_name")) {
                    vertices.getJSONObject(i).getJSONArray("display_name").forEach(item -> v.property("display_name", item));
                }
                if (vertices.getJSONObject(i).has("_marker_name")) {
                    vertices.getJSONObject(i).getJSONArray("_marker_name").forEach(item -> v.property("_marker_name", item));
                }
                if (vertices.getJSONObject(i).has("_marker_id")) {
                    vertices.getJSONObject(i).getJSONArray("_marker_id").forEach(item -> v.property("_marker_id", item));
                }
            }

            // create node to store: ontology, edgeLabels, propertyKeys, conflicts
            logger.info("create node to store: ontology, edgeLabels, propertyKeys, conflicts");
            JanusGraphVertex ontology = graph.addVertex();
            path = GraphSchemaAndData.class.getClassLoader().getResource(ontologyFile).getPath();
            file = new File(path);
            text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ontology.property("id", "tbox");
            ontology.property("ontology", text);
            JanusGraphVertex edgeLabels = graph.addVertex();
            edgeLabels.property("id", "edgeLabels");
            edgeLabels.property("types", "acronyms");
            edgeLabels.property("types", "assigned");
            edgeLabels.property("types", "contains");
            edgeLabels.property("types", "is_of");
            edgeLabels.property("types", "is_a_type_of");
            edgeLabels.property("types", "has_type");
            edgeLabels.property("types", "is_a_child_of");
            edgeLabels.property("types", "is_a_parent_of");
            edgeLabels.property("types", "is_steward_of");
            edgeLabels.property("types", "owns");
            edgeLabels.property("types", "referenceing");
            edgeLabels.property("types", "related");
            edgeLabels.property("types", "synonym");
            edgeLabels.property("types", "context");
            edgeLabels.property("types", "referencing");
            JanusGraphVertex propertyKeys = graph.addVertex();
            propertyKeys.property("id", "propertyKeys");
            propertyKeys.property("types", "id");
            propertyKeys.property("types", "name");
            propertyKeys.property("types", "state");
            propertyKeys.property("types", "description");
            propertyKeys.property("types", "alternative_names");
            propertyKeys.property("types", "abbreviations");
            propertyKeys.property("types", "type");
            propertyKeys.property("types", "score");
            propertyKeys.property("types", "producer_property");
            propertyKeys.property("types", "last_updated");
            propertyKeys.property("types", "deleted");
            propertyKeys.property("types", "provider");
            propertyKeys.property("types", "recent");
            propertyKeys.property("types", "checksum");
            propertyKeys.property("types", "context_id");
            propertyKeys.property("types", "display_name");
            propertyKeys.property("types", "_marker_name");
            propertyKeys.property("types", "_marker_id");
            JanusGraphVertex conflicts = graph.addVertex();
            conflicts.property("id", "conflicts");
            graph.tx().commit();

            // create edge
            logger.info("create edge");
            JSONArray edges = obj.getJSONArray("edges");
            GraphTraversalSource g = graph.traversal();
            for (int i = 0; i < edges.length(); i++) {
                String source = edges.getJSONObject(i).getString("source");
                String target = edges.getJSONObject(i).getString("target");
                String label = edges.getJSONObject(i).getString("label");
                Vertex v1 = g.V().has("id", source).next();
                Vertex v2 = g.V().has("id", target).next();
                if (edges.getJSONObject(i).has("deleted")) {
                    v1.addEdge(label, v2).property("deleted", edges.getJSONObject(i).getBoolean("deleted"));
                }else{
                    v1.addEdge(label, v2).property("deleted", false);
                }
                if (edges.getJSONObject(i).has("related") || edges.getJSONObject(i).has("synonym")) {
                    if (edges.getJSONObject(i).has("deleted")) {
                        v2.addEdge(label, v1).property("deleted", edges.getJSONObject(i).getBoolean("deleted"));
                    } else {
                        v2.addEdge(label, v1).property("deleted", false);
                    }
                }
            }
            graph.tx().commit();
            Thread.sleep(2000);
        } catch (InterruptedException | IOException e) {
            logger.error(e.toString());
            Thread.currentThread().interrupt();
            System.exit(1);
        } finally {
            graph.tx().close();
        }
        logger.info("Load data done.");
    }

    private static void createSampleGraph(JanusGraph graph, String graphFile) throws IOException {
        graphFile = String.format("%s/src/main/resources/test/%s", System.getenv("PWD"), graphFile);
        File f = new File(graphFile);

        if (f.exists())
            logger.info("the graph file {} does exist.", graphFile);
        else {
            logger.error("the graph file {} does not exist.", graphFile);
            throw new FileNotFoundException("s");
        }

        try {
            graph.io(IoCore.graphson()).readGraph(graphFile);
        } catch (Exception e) {
            logger.error("Import graph failure with an exception {}", e.getMessage(), e);
        }

        logger.info("Import WKC graph of single tenant done.");
    }

    private static void statistics(JanusGraph graph) {
        Set<Entity> entities = new HashSet<>();
        try (GraphTraversalSource g = graph.newTransaction().traversal()) {
            List<Object> idList = g.V().toStream().map(Vertex::id).map(Object::toString).collect(Collectors.toList());
            for (Object id: idList) {
                Entity entity = new Entity();
                entity.setvId(Long.valueOf(id.toString()));
                entities.add(entity);
            }
        } catch (Exception e) {
            logger.error("the traversal failure {}", e.getMessage());
        }
        if (logger.isInfoEnabled())
            logger.info(gson.toJson(entities));
    }

    private static void createVertexIndex(JanusGraph graph) {
        graph.tx().rollback();
        JanusGraphManagement mgmt = graph.openManagement();
        try {
            IndexBuilder ib = mgmt.buildIndex("VertexIndex", Vertex.class);
            // for (String key: indexVertex) {
            //     PropertyKey proKey = mgmt.getPropertyKey(key);
            //     ib = ib.addKey(proKey);
            // }
            PropertyKey proKey = mgmt.getPropertyKey("id");
            ib = ib.addKey(proKey);
            ib.buildCompositeIndex();
            mgmt.commit();
            ManagementSystem.awaitGraphIndexStatus(graph, "VertexIndex").call();
            logger.info("The graph vertex index creation done.");
        } catch (InterruptedException e) {
            logger.error("The graph vertex index creation failure with {}", e.getMessage());
            Thread.currentThread().interrupt();
            mgmt.rollback();
        }
    }

    private static void createEdgeIndex(JanusGraph graph) {
        graph.tx().rollback();
        JanusGraphManagement mgmt = graph.openManagement();

        for (String label: indexEdgeLabel) {
            EdgeLabel elabel = mgmt.getEdgeLabel(label);
            PropertyKey proKey = mgmt.getPropertyKey("id");
            mgmt.buildEdgeIndex(elabel, "EdgeIndex", Direction.BOTH, Order.asc, proKey);
        }
        mgmt.commit();

        for (String label: indexEdgeLabel) {
            ManagementSystem.awaitRelationIndexStatus(graph, "EdgeIndex", label);
        }
        logger.info("The graph edge index creation done.");
    }

    private static boolean checkGraphStatus(JanusGraph graph) {
        return graph.isClosed();
    }
    public static void main(String[] args) throws BackendException {
        String graphName = System.getProperty("wkc.tenant", "kg__999__datalineage");
        String graphGsonFile = System.getProperty("graph.gson.file", "graph1.json");
        logger.info("The graph name: {}, loading graph file: {}", graphName, graphGsonFile);
        graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(graphName).getConfiguration());
        JanusGraphFactory.drop(graph);
        Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> checkGraphStatus(graph));

        graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(graphName).getConfiguration());
        // createSchemaAndData(graph, null, null);
        try {
            createSchema(graph);
            createSampleGraph(graph, graphGsonFile);
            createOntology(graph, null);
            createVertexIndex(graph);
            createEdgeIndex(graph);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            graph.tx().rollback();
        } finally {
            statistics(graph);
            graph.close();
        }
    }

}
