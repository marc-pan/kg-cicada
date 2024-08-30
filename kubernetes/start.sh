#!/bin/sh
cd /root/install/janusgraph*
: ${ELASTICSEARCH_IP:=127.0.0.1}
: ${ELASTICSEARCH_PORT:=9200}
./bin/janusgraph.sh start

tailf ./log/gremlin-server.log
