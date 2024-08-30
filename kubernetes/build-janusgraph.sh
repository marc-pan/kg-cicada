#!/bin/bash

set -euo pipefail

# JanugGraphDB version
JANUSGRAPH_VERSION="0.6.0"
# FoundationDB Adapter version
JANUS_FDB_VERSION="0.3.1"
# Define a working directory to an environment variable
JG_WORKING_DIR=${PWD}

# Make a temporary directory
JG_TEMP_DIR=$(mktemp -d)
pushd ${JG_TEMP_DIR}

function cleanup() {
  if [ -d ${JG_TEMP_DIR} ]; then
    rm -rf ${JG_TEMP_DIR}
  fi
}

trap 'cleanup; exit -1' INT

# Some of parameters of WGET command
WGET_OPTS="-q --show-progress --no-proxy -w 5s"

# Download janusgraph v0.6.0 package
wget ${WGET_OPTS} "https://github.com/JanusGraph/janusgraph/releases/download/v${JANUSGRAPH_VERSION}/janusgraph-${JANUSGRAPH_VERSION}.zip" -O "janusgraph.zip"

# Download janusgraph adaptor for foundationdb
git clone git@github.ibm.com:wdp-gov/wdp-kg-janusgraph-foundationdb.git
pushd wdp-kg-janusgraph-foundationdb
mvn clean package -DskipTests
[ ! -f target/janusgraph-foundationdb-${JANUS_FDB_VERSION}-local-distribution.zip ] && echo "JanusGraph adaptor for FoundationDB failed to compile" && cleanup && exit -1
popd

# Integrate the adaptor into janusgraph
unzip -q "./janusgraph.zip"
unzip -q "./wdp-kg-janusgraph-foundationdb/target/janusgraph-foundationdb-${JANUS_FDB_VERSION}-local-distribution.zip"
pushd "janusgraph-foundationdb-${JANUS_FDB_VERSION}-local"
./install.sh "../janusgraph-${JANUSGRAPH_VERSION}"
popd

# Pack new janusgraph package
rm -f janusgraph-${JANUSGRAPH_VERSION}/lib/groovy*.jar # remove all version 2.5.14 related to groovy library and use them from ext directory
cp -p ${JG_WORKING_DIR}/../scripts/init-graph.groovy janusgraph-${JANUSGRAPH_VERSION}/scripts/.
tar cf janusgraph-${JANUSGRAPH_VERSION}.tar janusgraph-${JANUSGRAPH_VERSION}/
popd
mv ${JG_TEMP_DIR}/janusgraph-${JANUSGRAPH_VERSION}.tar ${JG_WORKING_DIR}/.
cleanup

# Check out the package
[ -f ${JG_WORKING_DIR}/janusgraph-${JANUSGRAPH_VERSION}.tar ] && echo "The new janusgraph package is ready now!!!"
