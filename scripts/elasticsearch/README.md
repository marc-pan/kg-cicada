# How to migrate indexing documents from one ES cluster to another cluster

## Pre-conditions
1. Exporting the ES service can access outside the cluster
```bash
# oc create route passthrough opensearch --service=elasticsearch-master-ibm-elasticsearch-srv -n wkc --port=https
```
2. Get the ES username and password
```bash
# oc extract secret/elasticsearch-master-secret --to=-
```
3. Create a virutal environment and install all dependencies libraries
4. Switch to new virtual environment
5. Clone `kg-ergo-demo` repo in your local disk and then go to the top directory of the project
6. Create empty director to store the ES data
```bash
# mkdir -p backup/wkc/bulk
```

## Export data from ES cluster
1. Configure the import settings through opening opensearch_settings.py file
2. Modify the value of parameters, such as `host`, `ELASTIC_PASSWORD`
3. Export data from ES cluster locally
```bash
# python3 scripts/elasticsearch/opensearch_export_cli.py --index=wkc --backup-folder=backup/wkc --export-csv
```
4. Once the command in step 3 is done, there is a few files in backup/wkc directory
```bash
# ls -l backup/wkc
-rw-r--r-- 1 root root        107 Jan  1 23:42 WKC.checksums      <<=== checksum for WKC.ndjson
-rw-r--r-- 1 root root 4277126433 Jan  1 23:40 WKC.ndjson         <<=== exported data in ndjson format
-rw-r--r-- 1 root root 2599688667 Jan  2 00:08 WKC.ndjson.csv     <<=== exported data in csv format
```

## Import data into another ES cluster
1. Launch Global Search service locally to create new index `wkc` and mapping and settings
2. Convert the exported data to a new file that is supported by ES/OS Bulk API
```bash
# python3 scripts/elasticsearch/opensearch_import_cli.py --index=wkc --import-file=backup/wkc/WKC.ndjson --bulk-file=backup/wkc/wkc_bulk.ndjson
```
3. Split the exported data into several small files due to bulk api for the size of the request body
```bash
# python3 scripts/elasticsearch/opensearch_import_cli.py --index=wkc --bulk-file=backup/wkc/wkc_bulk.ndjson
```
4. Import the bulk data into new ES cluster
```bash
# python3 scripts/elasticsearch/opensearch_import_cli.py --index=wkc --bulk-file=backup/wkc/wkc_bulk.ndjson --bulk-import=True
```
5. Check out the index doc number in new ES cluster
```bash
# curl --silent http://localhost:9200/wkc/_count
```
Output:
```json
{
  "count": 2064359,     <<=== actual number of the index doc in wkc index of new ES cluster
  "_shards": {
    "total": 10,
    "successful": 10,
    "skipped": 0,
    "failed": 0
  }
}
```
6. Compare the number of both the command in step 5 and WKC.checksums, the number should be equal
```json
{
  'WKC.ndjson': {
    'sha1': '227cc518b31e5502545b7006cdf5751e991bcff6',
    'size': 3839650398,
    'events': 2833108       <<=== the number of the docs exported from ES cluster
  }
}
```
