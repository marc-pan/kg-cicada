#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"

import pika
import json
import requests

iam_url = "https://iam.test.cloud.ibm.com/identity/token"

payload = ''
headers = {
  'Content-Type': 'application/x-www-form-urlencoded'
}

response = requests.request("POST", iam_url, headers=headers, data=payload)
access_token = response.json()['access_token']

rmq_server = "localhost"
rmq_port = 5672
rmq_username = "guest"
rmq_password = "guest"
rmq_url = "amqp://{0}:{1}@{2}:{3}/%2F".format(rmq_username, rmq_password, rmq_server, rmq_port)
rmq_queue = "gss_queue_cams"

parameters = pika.URLParameters(rmq_url)
credentials = pika.PlainCredentials(rmq_username, rmq_password)
connection = pika.BlockingConnection(
    pika.ConnectionParameters(virtual_host="/", host=rmq_server, port=rmq_port, credentials=credentials))
channel = connection.channel()

args = {"x-overflow": "reject-publish", "x-message-ttl": 3600000}
# args = {"x-message-ttl": 3600000}
channel.queue_declare(queue=rmq_queue, arguments=args, durable=True)

# import message
'''
{
  topic: 'v2.async_tasks.catalog_submit',
  message: {
    access_token: '',
    actor: { display_name: '1000330999', iam_id: '1000330999' },
    details: {
      catalog: [Object],
      catalog_id: '0b73f25f-ca0b-4c8a-9391-52f4efb86f2f',
      import_source_platform: 'icp',
      zip_file_object_key: 'catalog-import-b5e4fe91-80ff-46aa-8ab9-10d12f86c9bd.zip'
    },
    published: '2023-02-06T12:59:25.002Z',
    task_id: 'b5e4fe91-80ff-46aa-8ab9-10d12f86c9bd',
    type: 'import'
  },
  rabbitMsgObj: {
    fields: {
      consumerTag: 'portal-job-manager@48297330991d7351_2023-02-06T09:23:04.461Z',
      deliveryTag: 3,
      redelivered: false,
      exchange: 'dap',
      routingKey: 'v2.async_tasks.catalog_submit'
    },
    properties: {},
    content: <Buffer 7b 22 61 63 63 65 73 73 5f 74 6f 6b 65 6e 22 3a 22 65 79 4a 68 62 47 63 69 4f 69 4a 53 55 7a 49 31 4e 69 49 73 49 6e 52 35 63 43 49 36 49 6b 70 58 56 ... 2105 more bytes>
  }
}
'''
# message = json.dumps({
#     "access_token": "",
#     "task_id": "832deb79-8f33-4476-af9a-484b3b1f3faa",
#     "type": "import",
# })

# export message
'''
{
  topic: 'v2.async_tasks.catalog_submit',
  message: {
    access_token: '',
    actor: { display_name: '1000330999', iam_id: '1000330999' },
    details: {
      all_assets: true,
      assets: {},
      catalog: [Object],
      catalog_id: 'e4b47be8-bc7b-4b91-8e37-b831d0e73410',
      export_name: 'catalog-export-508435b8-f440-4d3c-9407-aa1bb4bb583b',
      skip_notification: true
    },
    published: '2023-02-06T12:36:11.677Z',
    task_id: '508435b8-f440-4d3c-9407-aa1bb4bb583b',
    type: 'export'
  },
  rabbitMsgObj: {
    fields: {
      consumerTag: 'portal-job-manager@48297330991d7351_2023-02-06T09:23:04.461Z',
      deliveryTag: 1,
      redelivered: false,
      exchange: 'dap',
      routingKey: 'v2.async_tasks.catalog_submit'
    },
    properties: {},
    content: <Buffer 7b 22 61 63 63 65 73 73 5f 74 6f 6b 65 6e 22 3a 22 65 79 4a 68 62 47 63 69 4f 69 4a 53 55 7a 49 31 4e 69 49 73 49 6e 52 35 63 43 49 36 49 6b 70 58 56 ... 2208 more bytes>
  }
}
'''

message = {
      "access_token": access_token,
      "details": {
        "all_assets": True,
        "assets": {},
        "catalog": {
          "metadata": {
                "guid": "832deb79-8f33-4476-af9a-484b3b1f3faa",
                "url": "https://10.171.154.58:49460/v2/catalogs/832deb79-8f33-4476-af9a-484b3b1f3faa",
                "creator_id": "IBMid-6620039NV8",
                "create_time": "2023-02-03T08:05:17Z",
                "update_time": "2023-02-03T08:05:58Z"
            },
            "entity": {
                "name": "panxiny_catatlog",
                "description": "this is test catalog",
                "generator": "default user",
                "bss_account_id": "",
                "capacity_limit": 0,
                "is_governed": False,
                "auto_profiling": False,
                "configurations": {
                    "duplicate_action": "UPDATE",
                    "default_duplicate_strategy": "DUPLICATE_DETECTION_BY_NAME",
                    "duplicate_strategies": [
                        {
                            "asset_type": "data_asset",
                            "strategy": "DUPLICATE_DETECTION_BY_NAME_AND_RESOURCE_KEY"
                        }
                    ]
                }
            }
        },
        "catalog_id": '832deb79-8f33-4476-af9a-484b3b1f3faa',
        "export_name": 'catalog-export-832deb79-8f33-4476-af9a-484b3b1f3faa',
        "skip_notification": True
      },
      "task_id": "832deb79-8f33-4476-af9a-484b3b1f3faa",
      "type": "export"
}

channel.basic_publish(exchange="dap",
                      routing_key="v2.async_tasks_local.submit",
                      body=json.dumps(message))
print(" [x] Sent {} type", message.get("type"))
connection.close()
