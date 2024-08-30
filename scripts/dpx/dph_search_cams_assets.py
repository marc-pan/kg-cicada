import requests
import sys
import os
import json
from itertools import islice

iam_url = "https://iam.test.cloud.ibm.com/identity/token"
apikey=os.environ['DEV_CLOUD_API_KEY']

payload='apikey={}&grant_type=urn%3Aibm%3Aparams%3Aoauth%3Agrant-type%3Aapikey'.format(apikey)
headers = {
   'Content-Type': 'application/x-www-form-urlencoded',
   'Accept': 'application/json',
}

res = requests.request("POST", iam_url, headers=headers, data=payload)

if (res.status_code == 200):
  token = 'Bearer ' + res.json()['access_token']
else:
  sys.exit(-1)

base_url = "https://api.dataplatform.dev.cloud.ibm.com"

headers = {
   'Authorization': token,
   'Accept': 'application/json',
   'Content-Type': 'application/json'
}

catalog_id='2e79006e-12d4-4aac-b01c-0cce610281ab'
service_id='iam-ServiceId-fe83e2ce-d3b9-44d3-a980-83518eefefeb'

search_endpoint = "/v2/asset_types/asset/search?catalog_id={}&exclude=columns"
payload = json.dumps({
   "query": "asset.memberIds:iam-ServiceId-fe83e2ce-d3b9-44d3-a980-83518eefefeb",
   "limit": 200,
   "sort": "-asset.created_at<string>"
})
res = requests.request("POST", base_url + search_endpoint.format(catalog_id), headers=headers, data=payload)

asset_list = []
non_support_types = ['connection', 'connection_credentials']
if (res.status_code == 200):
  print("Total assets: {}".format(res.json()['total_rows']))
  for asset in res.json()['results']:
    if (asset['metadata']['asset_type'] not in non_support_types):
      # print("Asset id: {}, type: {}".format(asset['metadata']['asset_id'], asset['metadata']['asset_type']))
      asset_list.append(asset['metadata']['asset_id'])
else:
  sys.exit(-1)

print("Total assets: {}".format(len(asset_list)))
print(asset_list)
