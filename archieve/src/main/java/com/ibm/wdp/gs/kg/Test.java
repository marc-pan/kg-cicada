package com.ibm.wdp.gs.kg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    private Artifacts artifact;
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String PRODUCER_PROPERTY = "producer_property";

    public Test() {
        artifact = new Artifacts();
    }

    private Entity vertex(GraphTraversalSource g, String id) {
        Entity entity = new Entity();
        entity.setvId(Long.parseLong(id));
        List<Map<Object, Object>> vertices = g.V(id).valueMap("id", "name", "type", PRODUCER_PROPERTY, "abbreviations", "alternative_names").toList();
        for (Map<Object, Object> entry: vertices) {
            entry.forEach((k,v) -> {
                switch(k.toString()) {
                    case "id":
                        entity.setId(v.toString());
                        break;
                    case "name":
                        entity.setName(v.toString());
                        break;
                    case "type":
                        entity.setType(v.toString());
                        break;
                    default:
                        break;
                }
            });
        }
        return entity;
    }

    private List<Entity> related(GraphTraversalSource g, String id, String edgeLabel, int hops) {
        List<Entity> entities = new ArrayList<>();
        List<Map<Object, Object>> vertices = null;
        if (hops == 1)
            vertices = g.V(id).outE(edgeLabel).inV().valueMap("id", "name", "type", PRODUCER_PROPERTY).toList();
        else
            vertices = g.V(id).outE(edgeLabel).inV().outE(edgeLabel).inV().valueMap("id", "name", "type", PRODUCER_PROPERTY).toList();

        for (Map<Object, Object> entry: vertices) {
            Entity entity = new Entity();
            entry.forEach((k,v) -> {
                switch(k.toString()) {
                    case "id":
                        entity.setId(v.toString());
                        break;
                    case "name":
                        entity.setName(v.toString());
                        break;
                    case "type":
                        entity.setType(v.toString());
                        break;
                    default:
                        break;
                }
            });
            entities.add(entity);
        }

        return entities;
    }

    private List<Entity> childParent(GraphTraversalSource g, String id, String edgeLabel, int hops) {
        List<Entity> entities = new ArrayList<>();
        List<Map<Object, Object>> vertices = null;
        if (hops == 1)
            vertices = g.V(id).outE(edgeLabel).inV().outE(edgeLabel).inV().valueMap("id", "name", "type", PRODUCER_PROPERTY).toList();
        else
            vertices = g.V(id).inE(edgeLabel).outV().inE(edgeLabel).outV().not(g.V(id).id()).valueMap("id", "name", "type", PRODUCER_PROPERTY).toList();

        for (Map<Object, Object> entry: vertices) {
            Entity entity = new Entity();
            entry.forEach((k,v) -> {
                switch(k.toString()) {
                    case "id":
                        entity.setId(v.toString());
                        break;
                    case "name":
                        entity.setName(v.toString());
                        break;
                    case "type":
                        entity.setType(v.toString());
                        break;
                    default:
                        break;
                }
            });
            entities.add(entity);
        }

        return entities;
    }

    public static void main(String[] args) {
        String graphName=null;
        String id=null;
        final Options options = new Options();
        options.addOption("i", "id", true, "vertex id long format");
        options.addOption("n", "name", true, "graph name");

        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine;
        
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return;
        }

        if (commandLine.hasOption("name"))
            graphName = commandLine.getOptionValue("name");
        if (commandLine.hasOption("id"))
            id = commandLine.getOptionValue("id");

        Test t = new Test();
        Stopwatch sw = Stopwatch.createStarted();

        JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(graphName).getConfiguration());
        try (GraphTraversalSource g = graph.newTransaction().traversal()) {
            sw.start();
            Entity entity = t.vertex(g, id);
            if (t.artifact.getEntities() == null)
                t.artifact.setEntities(new ArrayList<>(Arrays.asList(entity)));
            else
                t.artifact.getEntities().add(entity);

            List<Entity> entities = new ArrayList<>();
            entities = t.related(g, id, "related", 1);
            t.artifact.append(entities);

            entities = t.related(g, id, "related", 2);
            t.artifact.append(entities);

            entities = t.related(g, id, "synonym", 1);
            t.artifact.append(entities);

            entities = t.childParent(g, id, "is_a_type_of", 1);
            t.artifact.append(entities);

            entities = t.childParent(g, id, "is_a_type_of", 2);
            t.artifact.append(entities);

            entities = t.childParent(g, id, "has_type", 1);
            t.artifact.append(entities);

            entities = t.childParent(g, id, "has_type", 2);
            t.artifact.append(entities);
            sw.stop();
            logger.info("API exec time: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        graph.close();

        if (logger.isInfoEnabled())
            logger.info(gson.toJson(t.artifact));
    }
}
