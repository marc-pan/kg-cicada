package com.ibm.wdp.gs.kg;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.ibm.research.ergs.query.engine.ArtifactConstant.ArtifactType;
import com.ibm.research.ergs.query.engine.ArtifactException;
import com.ibm.research.ergs.query.engine.ArtifactQuerySPARQL;
import com.ibm.research.ergs.query.model.Artifact;

public class QueryThread {
    private static final Logger logger = LoggerFactory.getLogger(QueryThread.class);

    static final int MAX_T = 10;

    public static void main(String[] args) {
        logger.info("Create a thread pool with MAX_T no. of threads as the fixed pool size.");
        ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
        for (int i = 0; i < 100; i++) {
            Runnable r = new Task("task " + i);
            pool.execute(r);
        }

        pool.shutdown();
    }
}

class Task implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    private Thread t;
    private String name;

    Task(String name) {
        this.name = name;
    }

    public void run() {
        query();
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, name);
            t.start();
        }
    }

    private void query() {
        String graphName = "kg__fbdabb6bdf8442ea815f02770c31f2dd__datalineage"; // Query ERGO time: 107279 ms
        JanusGraph graph = JanusGraphFactory.open(BuildFDBConfiguration.getConfiguration(graphName).getConfiguration());

        try {
            ArtifactQuerySPARQL aq = new ArtifactQuerySPARQL(graph, graphName);
            Stopwatch sw = Stopwatch.createStarted();
            List<Artifact> result = aq.getArtifactList(Arrays.asList(82026568L), ArtifactType.TERM);
            sw.stop();
            logger.debug("The execution time is {}", sw.elapsed(TimeUnit.MILLISECONDS));
            if (!result.isEmpty())
                logger.info(result.get(0).getEntity().getName());
        } catch (ArtifactException e) {
            logger.error(e.getMessage());
        } finally {
            graph.tx().rollback();
            graph.close();
        }
    }
}
