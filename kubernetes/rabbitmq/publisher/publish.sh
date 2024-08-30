#!/bin/env bash

rmq_url=localhost
rmq_user=user
rmq_pass=aaa123
rmq_exchange=gss_queue_cams

for loop in {0..10}
do
  echo "Loop $loop :"
  for file in cams_delete_message.json cams_index_message.json cams_bulk_request_message.json
  do
    echo "Publish a message from $file"
    python3 cams_pub_msg.py --file $file
  done
done
