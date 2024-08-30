import requests
import json
import sys
import os
from itertools import islice
import pprint

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
  print("The request failed with message {} and status code {}".format(res.text, res.status_code))
  sys.exit(-1)

base_url = "https://api.dataplatform.dev.cloud.ibm.com"
dp_endpoint = "/data_product_exchange/v1/data_products?limit=200"

headers = {
   'Authorization': token,
   'Accept': 'application/json',
   'Content-Type': 'application/json'
}

res = requests.request("GET", base_url + dp_endpoint, headers=headers)
if (res.status_code == 200):
  dp_list = [dp['id'] for dp in res.json()['data_products']]
else:
  print("The request failed with message {} and status code {}".format(res.text, res.status_code))
  sys.exit(-1)


dp_endpoint = "/data_product_exchange/v1/data_products/{}/releases"

dps_dict = dict()
for dp_id in dp_list:
  res = requests.request("GET", base_url + dp_endpoint.format(dp_id), headers=headers)
  dps_list = [s['id'] for s in res.json()['releases']]
  dps_dict[dp_id] = dps_list
  # print("The DP {} has a number of releases is {}".format(dp_id, len(dps_list)))

dph_asset_list = set()
dps_endpoint = "/data_product_exchange/v1/data_products/{}/releases/{}?state=available,retire&limit=200"
for id, dps_list in dps_dict.items():
  for dps in dps_list:
    res = requests.request("GET", base_url + dps_endpoint.format(id, dps), headers=headers)
    if (res.status_code == 200):
      dph_asset_list.add(res.json()['asset']['id'])
      for asset in res.json()['parts_out']:
        dph_asset_list.add(asset['asset']['id'])
    else:
      continue

# print("The total number in DPH instance: {}".format(len(asset_list)))

catalog_id='2e79006e-12d4-4aac-b01c-0cce610281ab'
service_id='iam-ServiceId-fe83e2ce-d3b9-44d3-a980-83518eefefeb'

def chunk_list(iterable, n):
    it = iter(iterable)
    return iter(lambda: list(islice(it, n)), [])

cams_endpoint = "/v2/assets/bulk?catalog_id={}&asset_ids={}"
asset_chunks = list(chunk_list(dph_asset_list, 20))

for chunk in asset_chunks:
  cams_url = base_url + cams_endpoint.format(catalog_id, ','.join([str(item) for item in chunk]))
  res = requests.request("GET", cams_url, headers=headers)
  if (res.status_code == 200):
    for asset in res.json()['resources']:
      member_roles = asset['asset']['metadata']['rov']['member_roles']
      if service_id not in member_roles.keys():
        dph_asset_list.remove(asset['asset_id'])
      else:
        pass
        # print(asset['asset_id'])
  else:
    continue

# print(dph_asset_list)

search_endpoint = "/v2/asset_types/asset/search?catalog_id={}&exclude=columns"
payload = json.dumps({
   "query": "asset.memberIds:iam-ServiceId-fe83e2ce-d3b9-44d3-a980-83518eefefeb",
   "limit": 200,
   "sort": "-asset.created_at<string>"
})
res = requests.request("POST", base_url + search_endpoint.format(catalog_id), headers=headers, data=payload)

cams_asset_list = set()
non_support_types = ['connection', 'connection_credentials']
if (res.status_code == 200):
  # print("Total assets: {}".format(res.json()['total_rows']))
  for asset in res.json()['results']:
    if (asset['metadata']['asset_type'] not in non_support_types):
      # print("Asset id: {}, type: {}".format(asset['metadata']['asset_id'], asset['metadata']['asset_type']))
      cams_asset_list.add(asset['metadata']['asset_id'])
else:
  print("The request failed with message {} and status code {}".format(res.text, res.status_code))
  sys.exit(-1)

# print(cams_asset_list)
unmatched_asset_list = []

def get_asset_detail(asset_id):
  asset_endpoint = "/v2/assets/{}?catalog_id={}".format(asset_id, catalog_id)
  res = requests.request("GET", base_url + asset_endpoint, headers=headers)
  if (res.status_code == 200):
    return res.json()
  else:
    return res.text

def statictics(dph_list, cams_list):
  print("Total dph asset: {}".format(len(dph_list)))
  for dph in dph_list:
    if dph not in cams_list:
      print("{}: DPH {}, not matched".format(dph_list.index(dph), dph))
      unmatched_asset_list.append(dph)
    else:
      print("{}: DPH {}, matched".format(dph_list.index(dph), dph))

  print("Total cams assets: {}".format(len(cams_list)))
  for cams in cams_list:
    if cams not in dph_asset_list:
      print("{}: CAMS {}, not matched".format(cams_list.index(cams), cams))
      unmatched_asset_list.append(cams)
    else:
      print("{}: CAMS {}, matched".format(cams_list.index(cams), cams))

dph_assets = list(dph_asset_list)
cams_assets = list(cams_asset_list)
statictics(dph_list=dph_assets, cams_list=cams_assets)

asset_list = [get_asset_detail(id) for id in unmatched_asset_list]

for asset in asset_list:
  pprint.pprint(asset)
