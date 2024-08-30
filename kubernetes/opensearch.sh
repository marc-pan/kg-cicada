#!/bin/bash

set -e

# Create new route for Elasticsearch
oc create route passthrough opensearch --service=elasticsearch-master-ibm-elasticsearch-srv -n wkc --port=https

# Extract the password of ES service
oc extract secret/elasticsearch-master-secret --to=-

# Check out the ES health
es_host=$(oc get route/opensearch -o json -n wkc | jq .spec.host | tr -d '"')
es_passwd=$(oc get secret/elasticsearch-master-secret -o json |jq -r .data.elastic |base64 -d)
curl -k -X GET -uelastic:${es_passwd} https://${es_host}/_cluster/health -k | jq

