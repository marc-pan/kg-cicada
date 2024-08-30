#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"

import configparser
import json
import logging
import os
import glob

import click
import requests
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

logging.root.setLevel(logging.INFO)
logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
log_format = logging.Formatter(
    '%(asctime)s %(levelname)s [%(filename)s:%(lineno)s - %(funcName)8s() ] %(message)s'
)
handler.setFormatter(log_format)
logger.addHandler(handler)


class WKC:
    def __init__(self, apikey, env_name) -> None:
        self.config = configparser.ConfigParser()
        self.config.read('scripts/environments.ini', encoding="utf-8")
        self.api_server_uri = self.config.get(env_name, 'CAMS_SERVER_URI')
        self.catalog_id = self.config.get(env_name, "CATALOG_ID")
        self.api_key = apikey
        self.env_name = env_name
        self.session = requests.Session()
        self.session.headers.update({'Content-Type': 'application/json'})

    def get_access_token(self) -> None:
        endpoint = "/identity/token"
        url = self.config.get(self.env_name, 'IAM_AUTH_URI') + endpoint
        self.session.headers.update(
            {'Content-Type': 'application/x-www-form-urlencoded'})
        payload = "apikey={}&grant_type=urn:ibm:params:oauth:grant-type:apikey".format(
            self.api_key)
        res = self.session.post(url, data=payload, verify=False)
        if (res.status_code == 200):
            self.access_token = res.json()['access_token']
            self.session.headers.update(
                {'Authorization': 'Bearer {}'.format(self.access_token)})
            logger.info("retrieved the access token from {}.".format(url))
        else:
            logger.error(res.text)
            exit(-1)

    def get_catalog(self) -> None:
        endpoint = "/v2/catalogs/" + self.catalog_id
        url = self.api_server_uri + endpoint
        self.session.headers.update({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        })
        res = self.session.get(url, verify=False)
        if (res.status_code == 200):
            self.catalog = res.json()
            logger.info("retrieve catalog with id {}".format(self.catalog_id))
            pretty_json = json.dumps(self.catalog, indent=4)
            logger.info("the catalog entity is \n{}".format(pretty_json))
        else:
            logger.error(res.text)
            exit(-1)

    def get_asset_type_files(self) -> list:
        file_path = self.config.get(self.env_name, "ASSET_TYPE_RESOURCE_PATH")
        if (os.path.exists(file_path) != True):
            logger.error("the director {} is not existed".format(file_path))
            exit(-1)

        file_list = []
        for filename in glob.glob(r"{}/*.json".format(file_path)):
            file_list.append(filename)
            file_list.sort()
            logger.info(filename)

        logger.info("Total file number is {}".format(len(file_list)))
        return file_list

    def import_asset_type(self, file) -> bool:
        if (os.path.isfile(file) != True):
            logger.error("the file {} is not existed".format(file))
            exit(-1)

        endpoint = "/v2/asset_types"
        url = self.api_server_uri + endpoint
        params = {'catalog_id': self.catalog_id}
        with open(file, "r") as json_file:
            payload = json.load(json_file)
            res = self.session.post(url,
                                    params=params,
                                    data=json.dumps(payload),
                                    verify=False)
            if (res.status_code == 201):
                logger.info("import asset type successful")
                return True
            elif (res.status_code == 409):
                logger.info("the asset type is already existed, reason: {}".format(res.text))
                return True
            else:
                logger.error(
                    "import failure with the file {} and reason {}".format(
                        file, res.text))
                return False


@click.command()
@click.option("--apikey", default="", help="IAM API Key")
@click.option("--env", default="local", help="API Server")
def wkc_import(apikey, env) -> None:
    wkc = WKC(apikey=apikey, env_name=env)
    wkc.get_access_token()
    wkc.get_catalog()
    file_list = wkc.get_asset_type_files()
    for file in file_list:
      logger.info(file_list.index(file))
      ret = wkc.import_asset_type(file=file)
      if (ret != True):
        continue


if __name__ == "__main__":
    wkc_import()
