from opensearchpy import OpenSearch
import json

host = 'http://turquoise-vm.fyre.ibm.com:9200'
# get the password with the command oc extract secret/elasticsearch-master-secret --to=-
ELASTIC_PASSWORD = ""

client = OpenSearch(
    hosts = [host],
    # http_auth = ("elastic", ELASTIC_PASSWORD),
    use_ssl = False,
    verify_certs = False
)

info = client.info()
print(f"Welcome to {info['version']['distribution']} {info['version']['number']}!")

# bulk_file = "backup/wkc/test/wkc_bulk_41.ndjson"

# with open(bulk_file, 'r') as f:
#     data = [json.loads(l) for l in f.readlines()]
#     rc = client.bulk(data, params={"timeout": 1000})
#     if rc["errors"]:
#       print("There were errors:")
#       for item in rc["items"]:
#           print(f"{item['index']['status']}: {item['index']['error']['type']}")
#     else:
#       print(f"Bulk-inserted {len(rc['items'])} items.")

search_q = {"query": {"match_all": {}}}
if not client.indices.exists('wkc'):
  print("Target index not found!!!")
  exit(-1)

retries = 0
retry_limit = 3

while retries < retry_limit:
    try:
        doc_count = client.count(body=search_q, index='wkc')
        print("There are {} documents in index wkc".format(doc_count['count']))
        break
    except Exception as e:
        retries += 1
        if retries == retry_limit:
            raise e


