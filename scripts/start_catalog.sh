#!/usr/env bash

TK=$(curl --location 'https://iam.test.cloud.ibm.com/identity/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'apikey=6VyEf22-6ZJdgZyO1HNmWhQu7MOIF-fMKLQ7sxV1aVYn' \
--data-urlencode 'grant_type=urn:ibm:params:oauth:grant-type:apikey' | jq .access_token)
TK=${TK#?}
TK=${TK%?}

curl --insecure --location --request POST 'https://pixel-vm.fyre.ibm.com:9443/v2/catalogs' \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $TK" \
--data-raw '{
  "name": "marc-catalog4",
  "description": "this is test catalog",
  "generator": "default user",
  "bss_account_id": "bf7333d098ff48cba84d4f179a20dd83",
  "capacity_limit": 1,
  "configurations": {
      "duplicate_action": "UPDATE",
      "default_duplicate_strategy": "DUPLICATE_DETECTION_BY_NAME",
      "duplicate_strategies": [
        {
          "asset_type": "data_asset",
          "strategy": "DUPLICATE_DETECTION_BY_NAME_AND_RESOURCE_KEY"
        }
      ]
  },
  "bucket": {
    "bucket_name": "marc-bucket4",
    "bucket_location": "us-geo",
    "endpoint_url": "https://s3.us-west.cloud-object-storage.test.appdomain.cloud",
    "resource_instance_id": "crn:v1:staging:public:cloud-object-storage:global:a/cbe9b2bc0a246fd141492afb11eff4c9:a9dfd8d1-d91b-4486-ab7f-5f6232c9f2d6::",
   "bluemix_cos_credentials": {
      "viewer": {
        "api_key": "xL1C02t9COXe2fNjJi9_j6X28l5lVi8o8RAMAvvavvJT",
        "service_id": "ServiceId-c696cab7-d936-4528-947a-bd9b0cc4d298",
        "access_key_id":"ad21c2bdc89840b5acb7191a3f2433e3",
        "secret_access_key":"7f30792843f3378bc9e804efee8d6f52ff207ad58a8351af"
      },
      "editor": {
        "api_key": "xL1C02t9COXe2fNjJi9_j6X28l5lVi8o8RAMAvvavvJT",
        "service_id": "ServiceId-c696cab7-d936-4528-947a-bd9b0cc4d298",
        "access_key_id":"ad21c2bdc89840b5acb7191a3f2433e3",
        "secret_access_key":"7f30792843f3378bc9e804efee8d6f52ff207ad58a8351af"
      },
      "admin": {
        "api_key": "xL1C02t9COXe2fNjJi9_j6X28l5lVi8o8RAMAvvavvJT",
        "service_id": "ServiceId-c696cab7-d936-4528-947a-bd9b0cc4d298",
        "access_key_id":"ad21c2bdc89840b5acb7191a3f2433e3",
        "secret_access_key":"7f30792843f3378bc9e804efee8d6f52ff207ad58a8351af"
      }
    }
  },
  "is_governed": false
}' | jq
