package com.ibm.wdp.gs.kg;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.port;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphException;
import org.janusgraph.core.JanusGraphFactory;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ibm.research.ergs.query.engine.ArtifactConstant.ArtifactType;
import com.ibm.research.ergs.query.engine.ArtifactException;
import com.ibm.research.ergs.query.engine.ArtifactQuerySPARQL;
import com.ibm.research.ergs.query.model.Artifact;
import com.ibm.research.ergs.query.model.Entity;
import com.ibm.wala.util.perf.Stopwatch;

import spark.Request;
import spark.Response;

/**
 * Hello world!
 */
public final class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String LINEAGE_GIDPREFIX = "GLOSSARY###id###";
    private static final String PRODUCER_PROPERTY = "producer_property";
    private static final String IS_A_TYPE_OF = "is_a_type_of";
    private static final String HAS_TYPE = "has_type";
    private static final String LAST_UPDATED = "last_updated";
    private static final String TENANT_ID = "tenant_id";
    private static final String WKC = "wkc";
    private Artifact artifact;

    private App() {
        artifact = new Artifact();
    }

    private static String cleanup(List<Entity> entities, List<Artifact> artifacts) {
        String result = gson.toJson(artifacts);
        artifacts.clear();
        entities.clear();

        return result;
    }

    private void addGlobalGuid(Entity entity, String producerProperty) {
        List<String> gid = formateArtifactGuid(Arrays.asList(producerProperty));
        entity.setArtifactGuid(gid);
        String artifactId = gid.get(0).contains("_") ? gid.get(0).split("_")[1] : "-";
        entity.setArtifactId(artifactId);
    }

    private void addContainerProperties(GraphTraversalSource g, Entity entity){
        GraphTraversal<Vertex, Edge> categoryTraversal = g.V().has("id", entity.getId()).outE("context");
        Optional<Edge> hasCategoryEdge = categoryTraversal.tryNext();
        if(hasCategoryEdge.isPresent()){
            // Optional<Vertex> hasCatgeory = g.E(hasCategoryEdge.get().id()).inV().has("type", TextP.containing(ArtifactType.BUSINESS_CATEGORY.alias())).tryNext();
            // if(hasCatgeory.isPresent()){
            //     Vertex category = hasCatgeory.get();
            //     if(category.keys().contains(PRODUCER_PROPERTY)){
            //         List<String> properties = Arrays.asList(category.values(PRODUCER_PROPERTY).next().toString());
            //         List<String> gids = formateArtifactGuid(properties);
            //         entity.put("container_id", gids.get(0), null);
            //     }
            //     if(category.keys().contains("name")){
            //         entity.put("container_name", category.value("name").toString(), null);
            //     }
            //     entity.put("container_type", "category", null);
            // }
        }
    }

    private List<String> formateArtifactGuid(List<String> producerProperty){
        if(producerProperty ==null || producerProperty.isEmpty()){
            return Arrays.asList("_");
        }
        String v1 = producerProperty.get(0);
        if (v1.contains(LINEAGE_GIDPREFIX.toLowerCase()))
            v1 = v1.replace(LINEAGE_GIDPREFIX.toLowerCase(), "");
        else
            v1 = v1.replace(LINEAGE_GIDPREFIX, "");
        return Arrays.asList(v1);
    }

    @SuppressWarnings("unchecked")
    private Entity vertex(GraphTraversalSource g, String id) {
        Entity entity = new Entity();
        Map<Object, Object> vertix = g.V(id).valueMap("id", "name", "type", PRODUCER_PROPERTY, LAST_UPDATED, "abbreviations", "alternative_names").next();

        if (vertix != null) {
            vertix.forEach((k,v) -> {
                List<String> vv = ((ArrayList<Object>)v).stream().map(String::valueOf).collect(Collectors.toList());
                switch(k.toString()) {
                    case "id":
                        entity.setId(vv.get(0));
                        break;
                    case "name":
                        entity.setName(vv.get(0));
                        break;
                    case "type":
                        entity.setType(vv.get(0));
                        break;
                    case PRODUCER_PROPERTY:
                        addGlobalGuid(entity, vv.get(0));
                        break;
                    case "abbreviations":
                        entity.setAbbreviations(vv);
                        break;
                    case "alternative_names":
                        entity.setAliases(vv);
                        break;
                    case LAST_UPDATED:
                        entity.setLastUpdatedAt(vv.get(0));
                        break;
                    default:
                        break;
                }
            });
            addContainerProperties(g, entity);
        }

        return entity;
    }

    @SuppressWarnings("unchecked")
    private List<Entity> edge(GraphTraversalSource g, String id, String edgeLabel, int hops) {
        List<Entity> entities = new ArrayList<>();
        List<Map<Object, Object>> vertices = null;
        if (edgeLabel.equalsIgnoreCase(IS_A_TYPE_OF) || edgeLabel.equalsIgnoreCase(HAS_TYPE)) {
            if (hops == 1)
                vertices = g.V(id).inE(edgeLabel).outV().valueMap("id", "name", "type", PRODUCER_PROPERTY, LAST_UPDATED).toList();
            else
                vertices = g.V(id).inE(edgeLabel).outV().inE(edgeLabel).outV().valueMap("id", "name", "type", PRODUCER_PROPERTY, LAST_UPDATED).toList();
        } else {
            if (hops == 1)
                vertices = g.V(id).outE(edgeLabel).inV().valueMap("id", "name", "type", PRODUCER_PROPERTY, LAST_UPDATED).toList();
            else
                vertices = g.V(id).outE(edgeLabel).inV().outE(edgeLabel).inV().filter(v -> !v.get().id().toString().equals(id)).valueMap("id", "name", "type", PRODUCER_PROPERTY, LAST_UPDATED).toList();
        }

        for (Map<Object, Object> entry: vertices) {
            Entity entity = new Entity();
            entry.forEach((k,v) -> {
                String vv = ((ArrayList<Object>)v).get(0).toString();
                switch(k.toString()) {
                    case "id":
                        entity.setId(vv);
                        break;
                    case "name":
                        entity.setName(vv);
                        break;
                    case "type":
                        entity.setType(vv);
                        break;
                    case PRODUCER_PROPERTY:
                        addGlobalGuid(entity, vv);
                        break;
                    case LAST_UPDATED:
                        entity.setLastUpdatedAt(vv);
                        break;
                    default:
                        break;
                }
            });
            if (entity.getLastUpdatedAt() == null) {
                entity.setLastUpdatedAt(String.valueOf(System.currentTimeMillis()));
            }
            entities.add(entity);
        }

        return entities;
    }

    private static Artifact queryByName(Request req, Response res, String graphName) throws ArtifactException {
        res.header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.type());
        String tenant = (req.queryParams(TENANT_ID));
        if (tenant == null || tenant.startsWith(WKC))
            tenant = WKC;
        else
            tenant = String.format(graphName, req.queryParamOrDefault(TENANT_ID, WKC));
        Entity body = gson.fromJson(URLDecoder.decode(req.body(), StandardCharsets.UTF_8), Entity.class);
        JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(tenant).getConfiguration());
        ArtifactQuerySPARQL aq = new ArtifactQuerySPARQL(graph, tenant);
        Artifact result = aq.getArtifact(body.getName(), ArtifactType.TERM);
        logger.info(result.getEntity().getId());
        return result;
    }

    private static List<Artifact> queryByIds(Request req, Response res, String graphName) throws ArtifactException {
        res.header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.type());
        String tenant = (req.queryParams(TENANT_ID));
        if (tenant == null || tenant.startsWith(WKC))
            tenant = WKC;
        else
            tenant = String.format(graphName, req.queryParamOrDefault(TENANT_ID, WKC));
        List<Entity> body = gson.fromJson(URLDecoder.decode(req.body(), StandardCharsets.UTF_8), new TypeToken<List<Entity>>() {}.getType());
        JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(tenant).getConfiguration());
        ArtifactQuerySPARQL aq = new ArtifactQuerySPARQL(graph, tenant);
        List<Artifact> result = aq.getArtifactList(Collections.singletonList(body.get(0).getId()), ArtifactType.TERM);
        logger.info(result.get(0).getEntity().getId());
        return result;
    }

    private static String queryByVids(Request req, Response res, String graphName, String message) throws ArtifactException {
        res.header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.type());
        List<Entity> body = gson.fromJson(URLDecoder.decode(req.body(), StandardCharsets.UTF_8), new TypeToken<List<Entity>>() {}.getType());
        String type = req.queryParamOrDefault("type", "term");
        String label = req.queryParamOrDefault("label", null);
        String tenant = (req.queryParams(TENANT_ID));
        if (tenant == null || tenant.startsWith(WKC))
            tenant = WKC;
        else
            tenant = String.format(graphName, req.queryParamOrDefault(TENANT_ID, WKC));
        logger.info("{}, {}, {}", message, tenant, body.stream().map(Entity::getId).collect(Collectors.toList()));
        Stopwatch sw = new Stopwatch();
        sw.start();
        JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(tenant).getConfiguration());
        ArtifactQuerySPARQL aq = new ArtifactQuerySPARQL(graph, tenant);
        sw.stop();
        logger.info("The ArtifactQuerySPARQL initialization exec time: {}ms", sw.getElapsedMillis());
        sw.start();
        ArtifactType atype = null;
        switch (type) {
            case "term":
                atype = ArtifactType.TERM;
                break;
            case "category":
                // atype = ArtifactType.BUSINESS_CATEGORY;
                break;
            case "test":
                atype = ArtifactType.ASSET;
                break;
            default:
                atype = ArtifactType.TERM;
                break;
        }

        List<Artifact> result = aq.getArtifactList(body.stream().map(Entity::getId).map(Long::valueOf).collect(Collectors.toList()), atype, label);
        sw.stop();
        logger.info("The SPARQL exec time: {}ms", sw.getElapsedMillis());

        if (!result.isEmpty())
            logger.info(result.get(0).getEntity().getId());
        return cleanup(body, result);
    }

    private static List<Artifact> queryByJG(Request req, Response res, String graphName, String message) {
        res.header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.type());
        List<Entity> body = gson.fromJson(URLDecoder.decode(req.body(), StandardCharsets.UTF_8), new TypeToken<List<Entity>>() {}.getType());
        String tenant = (req.queryParams(TENANT_ID));
        if (tenant == null || tenant.startsWith(WKC))
            tenant = WKC;
        else
            tenant = String.format(graphName, req.queryParamOrDefault(TENANT_ID, WKC));
        logger.info("message:{}, tenant:{}, body:{}", message, tenant, body.stream().map(Entity::getId).collect(Collectors.toList()));
        List<String> ids = body.stream().map(Entity::getId).collect(Collectors.toList());
        Stopwatch sw = new Stopwatch();
        sw.start();
        App t = new App();
        List<Artifact> artifacts = new ArrayList<>();
        JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(tenant).getConfiguration());
        try (GraphTraversalSource g = graph.newTransaction().traversal()) {
            for (String id: ids) {
                sw.start();
                Entity entity = t.vertex(g, id);
                t.artifact.setEntity(entity);

                List<Entity> entities = new ArrayList<>();
                entities = t.edge(g, id, "related", 1);
                t.artifact.setDirectlyRelateds(entities);

                entities = t.edge(g, id, "related", 2);
                Set<Entity> eset = new HashSet<>();
                eset.addAll(entities);
                t.artifact.setLooselyRelateds(new ArrayList<>(eset));

                entities = t.edge(g, id, "synonym", 1);
                t.artifact.setEquivalents(entities);

                entities = t.edge(g, id, IS_A_TYPE_OF, 1);
                t.artifact.setChilds(entities);

                entities = t.edge(g, id, IS_A_TYPE_OF, 2);
                eset.clear();
                eset.addAll(entities);
                t.artifact.setGrandChilds(new ArrayList<>(eset));

                entities = t.edge(g, id, HAS_TYPE, 1);
                t.artifact.setParents(entities);

                entities = t.edge(g, id, HAS_TYPE, 2);
                eset.clear();
                eset.addAll(entities);
                t.artifact.setGrandParents(new ArrayList<>(eset));
                sw.stop();
                logger.info("API exec time: {}ms", sw.getElapsedMillis());
            }
            artifacts.add(t.artifact);
        } catch (Exception e) {
            logger.error("");
        }
        return artifacts;
    }

    private static List<Artifact> deleteById(Request req, Response res, String graphName, String message) throws ArtifactException {
        res.header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.type());
        List<Entity> body = gson.fromJson(URLDecoder.decode(req.body(), StandardCharsets.UTF_8), new TypeToken<List<Entity>>() {}.getType());
        String tenant = (req.queryParams(TENANT_ID));
        if (tenant == null || tenant.startsWith(WKC))
            tenant = WKC;
        else
            tenant = String.format(graphName, req.queryParamOrDefault(TENANT_ID, WKC));
        logger.info("message: {}, tenant:{}, body:{}", message, tenant, body.stream().map(Entity::getId).collect(Collectors.toList()));
        List<Artifact> result = null;
        Stopwatch sw = new Stopwatch();
        sw.start();
        try (JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(tenant).getConfiguration());) {
            ArtifactQuerySPARQL aq = new ArtifactQuerySPARQL(graph, tenant);
            Optional<String> id = body.stream().map(Entity::getId).findFirst();
            if (id.isPresent()) {
                aq.getArtifact(id.get(), ArtifactType.TERM);
                result = aq.getDeletedEntityList(body.stream().map(Entity::getId).map(Long::valueOf).collect(Collectors.toList()));
            }
            sw.stop();
            logger.info("The SPARQL exec time: {}ms", sw.getElapsedMillis());
        } catch (JanusGraphException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     * @throws Exception
     */
    public static void main(String[] args) {
        port(Integer.valueOf(System.getenv("PORT")));
        String graphName = "kg__%s__datalineage";
        String message = "The WKC graph name is %s, vertex id: %s";
        get("/termbyname", (req, res) -> {
            Artifact result = queryByName(req, res, graphName);
            return gson.toJson(result);
        });

        get("/term", (req, res) -> {
            List<Artifact> result = queryByIds(req, res, graphName);
            return gson.toJson(result);
        });

        get("/terms", (req, res) -> queryByVids(req, res, graphName, message));

        get("/api", (req, res) -> {
            List<Artifact> artifacts = queryByJG(req, res, graphName, message);
            return gson.toJson(artifacts);
        });

        delete("/terms", (req, res) -> {
            List<Artifact> result = deleteById(req, res, graphName, message);
            return gson.toJson(result);
        });

    }
}
