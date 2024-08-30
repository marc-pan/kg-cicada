#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"

import click
import configparser
import json
import logging
from time import sleep
import requests
import secrets
import concurrent.futures

logging.root.setLevel(logging.INFO)
logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
log_format = logging.Formatter('%(asctime)s %(levelname)s [%(filename)s:%(lineno)s - %(funcName)8s() ] %(message)s')
handler.setFormatter(log_format)
logger.addHandler(handler)

config = configparser.ConfigParser()
config.read('scripts/environments.ini')
app_uri = config['default']['APP_SERVER_URI']

# TODO: automatically generate the vertex id list through query from graph database
global_datasets = [12456, 12392, 12400, 16528, 16552, 16568, 24800, 28896, 20496, 32784, 20528, 24624, 32816, 36912, 8272, 24664, 8296, 28776, 20592, 4240, 8336]
session = requests.Session()
session.headers.update({'Content-Type': 'application/json'})
query_time_list = []


class Entity:
  def __init__(self, id=0) -> None:
      self.id = id
      self.amap = dict()

  def get_dict(self) -> any:
    self.amap['id'] = self.id
    return self.amap


def prepare_data(number=1) -> list:
    if number <= 0:
      number = 1
    elif number > len(global_datasets):
      number = len(global_datasets)

    logger.debug("the sample number is {}".format(number))

    secure_random = secrets.SystemRandom()
    list_of_random_items = secure_random.sample(global_datasets, number)

    return [Entity(x).get_dict() for x in list_of_random_items]


def query(sample_number) -> any:
    data = prepare_data(sample_number)
    payload = json.dumps(data)
    response = session.get(app_uri + "/terms", data=payload)
    result = [ int(x['entity']['lastUpdatedAt']) for x in response.json() if 'lastUpdatedAt' in x['entity'].keys() ]
    global query_time_list
    for x in result:
        query_time_list.append(x)
    ret = sum(result)/len(result) if len(result) != 0 else 0
    logger.debug("the average query time is {}".format(ret))

    return response.status_code


def asyc_execution(workers, sample_number, query) -> None:
    global query_time_list
    with concurrent.futures.ThreadPoolExecutor(workers) as executor:
        future_to_query = { executor.submit(query, sample_number): i for i in range(workers) }
        try:
          for future in concurrent.futures.as_completed(future_to_query):
              number = future_to_query[future]
              try:
                  data = future.result()
              except Exception as exc:
                  logger.error("generated an exception: %s in %d" % (exc, number))
              else:
                  logger.debug("the call is success, the number of result: [%d]." % data)
        except KeyboardInterrupt:
          logger.error("executor shutdown gracefully.")
          executor.shutdown(True)
          return

        if len(query_time_list) != 0:
          max_time = max(query_time_list)
          min_time = min(query_time_list)
          logger.info("there are %d asyc execution is running and %d total sample data" % (len(future_to_query), len(query_time_list)))
          logger.info("the average query %s in time is %d, the max time is %d, the min time is %d" % (query, sum(query_time_list)/len(query_time_list), max_time, min_time))
          query_time_list.clear()


@click.command()
@click.option("--total", default=1, help="total number of sampling vertex.")
@click.option("--workers", default=1, help="worker number that will be executing concurrently.")
def main(total, workers):
  sample_number = int(total / workers)
  asyc_execution(workers, sample_number, query)

if __name__ == "__main__":
    while True:
      try:
        main()
        sleep(3)
      except Exception as ex:
        logger.error("got an exception " + ex)
      finally:
        exit(-1)
