#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"

import os
import sys
import pika
import logging

logger = logging.getLogger('sink')
rmqu = os.environ.get('RMQ_USER', "user")
rmqp = os.environ.get('RMQ_PASS', "aaa123")
vhost = os.environ.get('RMQ_VHOST', "/")
service = os.environ.get('RMQ_SVC', 'localhost')
rmq_port = os.environ.get("RMQ_PORT", 5672)

def main():
  _cred = pika.PlainCredentials(rmqu, rmqp, False);
  connection = pika.BlockingConnection(
      pika.ConnectionParameters(host=service, port=rmq_port, virtual_host=vhost, credentials=_cred))
  channel = connection.channel()

  exchange_name = 'global_search_failover'
  exchange_type = 'direct'
  channel.exchange_declare(exchange=exchange_name, exchange_type=exchange_type, durable=True)

  def callback1(ch, method, properties, body):
      print(" == [x] Received %r from queue %s" % (body, queue1_name))
      logger.info(" [x] Received %r from queue %s" % (body, queue1_name)) # Don't know why this message is not flushed to stdout

  queue1_name = 'global_search_failover'
  # Make sure it has same feature definition for each queue
  # refer to https://blog.csdn.net/liman65727/article/details/102520614?spm=1001.2101.3001.6650.4&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-4-102520614-blog-124615895.pc_relevant_recovery_v2&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-4-102520614-blog-124615895.pc_relevant_recovery_v2&utm_relevant_index=9
  channel.queue_declare(queue=queue1_name, durable=True, arguments={'x-message-ttl':3600000, 'x-overflow':'reject-publish'})
  channel.queue_bind(exchange=exchange_name, queue=queue1_name)

  channel.basic_consume(queue=queue1_name,
                        auto_ack=True,
                        on_message_callback=callback1)
                        # consumer_tag=consumer_tag) // Don't specify consumer_tag for an existing queue

  # def callback2(ch, method, properties, body):
  #     print(" == [x] Received %r from queue %s" % (body, queue2_name))
  #     logger.info(" [x] Received %r from queue %s" % (body, queue2_name))

  # queue2_name = 'portal-job-manager'
  # channel.queue_declare(queue=queue2_name, durable=True, arguments={'x-message-ttl':3600000})
  # channel.queue_bind(exchange=exchange_name, queue=queue2_name)
  # channel.basic_consume(queue=queue2_name,
  #                       auto_ack=True,
  #                       on_message_callback=callback2)

  # def callback3(ch, method, properties, body):
  #     print(" == [x] Received %r from queue %s" % (body, queue3_name))
  #     logger.info(" [x] Received %r from queue %s" % (body, queue3_name))

  # queue3_name = 'portal-job-manager-api'
  # channel.queue_declare(queue=queue3_name, durable=True, arguments={'x-message-ttl':3600000})
  # channel.queue_bind(exchange=exchange_name, queue=queue3_name)
  # channel.basic_consume(queue=queue3_name,
  #                       auto_ack=True,
  #                       on_message_callback=callback3)

  # def callback4(ch, method, properties, body):
  #     print(" == [x] Received %r from queue %s" % (body, queue4_name))
  #     logger.info(" [x] Received %r from queue %s" % (body, queue4_name))

  # queue4_name = 'portal-projects-job-manager'
  # channel.queue_declare(queue=queue4_name, durable=True, arguments={'x-message-ttl':3600000})
  # channel.queue_bind(exchange=exchange_name, queue=queue4_name)
  # channel.basic_qos(prefetch_count=1)
  # channel.basic_consume(queue=queue4_name,
  #                       auto_ack=True,
  #                       on_message_callback=callback4)

  print(' == [*] Waiting for messages. To exit press CTRL+C')
  logger.info(' [*] Waiting for messages. To exit press CTRL+C')
  channel.start_consuming()


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        logger.info('Interrupted')
        try:
            sys.exit(0)
        except SystemExit as e:
            raise e
        except:
            os._exit(0)
