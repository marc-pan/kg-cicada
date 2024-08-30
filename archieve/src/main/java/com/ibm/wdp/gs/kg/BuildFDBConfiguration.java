package com.ibm.wdp.gs.kg;

import static org.janusgraph.diskstorage.foundationdb.FoundationDBConfigOptions.CLUSTER_FILE_PATH;
import static org.janusgraph.diskstorage.foundationdb.FoundationDBConfigOptions.DIRECTORY;
import static org.janusgraph.diskstorage.foundationdb.FoundationDBConfigOptions.GET_RANGE_MODE;
import static org.janusgraph.diskstorage.foundationdb.FoundationDBConfigOptions.ISOLATION_LEVEL;
import static org.janusgraph.diskstorage.foundationdb.FoundationDBConfigOptions.VERSION;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.DROP_ON_CLEAR;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.STORAGE_BACKEND;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.buildGraphConfiguration;

import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildFDBConfiguration {

	protected static final Logger log = LoggerFactory.getLogger(BuildFDBConfiguration.class);

    private BuildFDBConfiguration() {}
	private static String getClusterFile() {
    	String os = System.getProperty("os.name").toLowerCase();
    	if(os.indexOf("mac") >= 0) {
    		return "/usr/local/etc/foundationdb/fdb.cluster";
    	}else {
    		return "/etc/foundationdb/fdb.cluster";
    	}
    }

	public static ModifiableConfiguration getConfiguration(final String graphName) {
        return buildGraphConfiguration()
            .set(STORAGE_BACKEND, "org.janusgraph.diskstorage.foundationdb.FoundationDBStoreManager")
            .set(DIRECTORY, graphName)
            .set(DROP_ON_CLEAR, false)
            .set(CLUSTER_FILE_PATH, getClusterFile())
            .set(ISOLATION_LEVEL, "read_committed_with_write")
            .set(GET_RANGE_MODE, getAndCheckRangeModeFromTestEnvironment())
            .set(VERSION, 620);
    }

	private static String getAndCheckRangeModeFromTestEnvironment() {
        String mode = System.getProperty("getrangemode");
        if (mode == null) {
            log.warn("No getrangemode property is chosen, use default value: list to proceed");
            return "list";
        }
        else if (mode.equalsIgnoreCase("iterator")){
            log.info("getrangemode property is chosen as: iterator");
            return "iterator";
        }
        else if (mode.equalsIgnoreCase("list")){
            log.info("getrangemode property is chosen as: list");
            return "list";
        }
        else {
            log.warn("getrange mode property chosen: {} does not match supported modes: iterator or list, choose default value: list to proceed", mode);
            return "list";
        }
    }

}
