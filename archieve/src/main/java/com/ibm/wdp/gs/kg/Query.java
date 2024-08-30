package com.ibm.wdp.gs.kg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.research.ergs.query.engine.ArtifactConstant.ArtifactType;
import com.ibm.research.ergs.query.engine.ArtifactException;
import com.ibm.research.ergs.query.engine.ArtifactQuerySPARQL;
import com.ibm.research.ergs.query.model.Artifact;
import com.ibm.wala.util.perf.Stopwatch;

public class Query {
    private static final Logger logger = LoggerFactory.getLogger(Query.class);

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private enum TYPE {
        TERM,
        CATEGORY;
    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("query", options, true);
    }

    private static void query4ergo(TYPE type, JanusGraph graph, String graphName, List<Long> ids, long loop, String label, boolean verbose) throws ArtifactException {
        while(loop!=0) {
            logger.info("Initial ArtifactQuerySPARQL object");
            ArtifactQuerySPARQL aq = new ArtifactQuerySPARQL(graph, graphName);
            var sw = new Stopwatch();
            sw.start();
            logger.info("Query vertex id list is {}", ids);
            List<Artifact> result = new ArrayList<>();
            switch (type) {
                case TERM:
                    result = aq.getArtifactList(ids, ArtifactType.TERM, label);
                    break;
                case CATEGORY:
                    // result = aq.getArtifactList(Arrays.asList(id), ArtifactType.BUSINESS_CATEGORY, "context");
                    break;
                default:
                    break;
            }
            sw.stop();
            if (verbose && logger.isInfoEnabled()) {
                logger.info("The result ===> {}", gson.toJson(result));
            }
            logger.info("The execution time is {}ms for vertex id {}.", sw.getElapsedMillis(), ids);
            logger.info("The query time: {}, result size: {}", loop, result.size());
            --loop;
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addRequiredOption("n", "name", true, "graph name")
                .addRequiredOption("i", "ids", true, "vertex id list separate by comma")
                .addOption("l", "loop", true, "query time")
                .addOption("v", "verbose", false, "print more information")
                .addOption("e", "label", true, "edge label")
                .addRequiredOption("t", "type", true, "artifact type");

        if (args.length < 1) {
            printUsage(options);
        }

        JanusGraph graph = null;
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            String graphName = null;
            String vId = null;
            String qLoop= null;
            List<Long> ids = new ArrayList<>();
            long loop = 0L;
            boolean verbose = false;
            String label = null;
            TYPE type = TYPE.TERM;
            if (cmd.hasOption("n")) {
                graphName = cmd.getOptionValue("n");
                logger.info("graph name: {}", graphName);
            }
            if (cmd.hasOption("i")) {
                vId = cmd.getOptionValue("i");
                logger.info("vertex id: {}", vId);
                if (vId.indexOf(",") != -1) {
                    var source = vId.split(",")[0];
                    var target = vId.split(",")[1];
                    ids.add(Long.parseLong(source));
                    ids.add(Long.parseLong(target));
                } else {
                    ids.add(Long.parseLong(vId));
                }
            }
            if (cmd.hasOption("l")) {
                qLoop = cmd.getOptionValue("l");
                logger.info("query time: {}", qLoop);
                loop = Integer.parseInt(qLoop);
            }

            if (cmd.hasOption("v")) {
                verbose = true;
                logger.info("verbose set to {}", verbose);
            }

            if (cmd.hasOption("e")) {
                label = cmd.getOptionValue("e");
                logger.info("edge label: {}", label);
            }

            if (cmd.hasOption("t")) {
                type = TYPE.valueOf(cmd.getOptionValue("t"));
                logger.info("query for type {}", type);
            }

            logger.info("Open JanusGraph graph instance.");
            graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(graphName).getConfiguration());
            query4ergo(type, graph, graphName, ids, loop, label, verbose);
        } catch (ParseException e) {
            printUsage(options);
        } catch (ArtifactException e) {
            e.printStackTrace();
            // Restore interrupt state...
            Thread.currentThread().interrupt();
        } finally {
            if (graph != null)
                graph.close();
        }
        System.exit(0);
    }
}
