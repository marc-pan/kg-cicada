FROM opensuse/leap

# JanugGraphDB version
ENV JANUSGRAPH_VERSION 0.3.0
# FoundationDB Adapter version
ENV JANUS_FDB_VERSION 0.1.0

# Install pre-requisite packages
RUN zypper up; \
    zypper --no-refresh -n in java-1_8_0-openjdk wget unzip iproute2 java-1_8_0-openjdk-devel which

# Download JanusGraphDB & FoundationDB Adapter
RUN mkdir -p /root/downloads; \
    cd /root/downloads; \
    wget -q "https://github.com/JanusGraph/janusgraph/releases/download/v$JANUSGRAPH_VERSION/janusgraph-$JANUSGRAPH_VERSION-hadoop2.zip" -O "janusgraph.zip"; \
    wget -q "https://github.com/experoinc/janusgraph-foundationdb/releases/download/v$JANUS_FDB_VERSION/janusgraph-foundationdb-$JANUS_FDB_VERSION-distribution.zip" -O "janusgraph-foundationdb.zip";

# Extract JanusGraphDB and FoundationDB Adapter
RUN mkdir -p /root/install; \
    cd /root/install; \
    unzip -q "/root/downloads/janusgraph.zip"; \
    unzip -q "/root/downloads/janusgraph-foundationdb.zip";

# Remove downloaded archives to save disk space
#RUN    rm "/root/downloads/janusgraph.zip" "/root/downloads/janusgraph-foundationdb.zip";

# Install FoundationDB storage adapter in JanusGraph Installation
RUN cd /root/install; \
    cd "janusgraph-foundationdb-$JANUS_FDB_VERSION"; \
    ./install.sh "/root/install/janusgraph-$JANUSGRAPH_VERSION-hadoop2";

COPY "start.sh" /root/install/

ENTRYPOINT ["/root/install/start.sh"]
