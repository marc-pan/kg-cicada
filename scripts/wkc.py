#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"

import configparser
import json
import logging

import click
import requests
from model.artifact import Artifact
from model.category import Category
from model.stopwatch import Stopwatch
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

logging.root.setLevel(logging.INFO)
logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
log_format = logging.Formatter(
    '%(asctime)s %(levelname)s [%(filename)s:%(lineno)s - %(funcName)8s() ] %(message)s')
handler.setFormatter(log_format)
logger.addHandler(handler)


class WKC:
  def __init__(self, username, password, env_name) -> None:
    self.config = configparser.ConfigParser()
    self.config.read('scripts/environments.ini')
    URI_SCHEME = "https://"
    self.api_server_uri = URI_SCHEME + self.config[env_name]['API_SERVER_URI']
    self.username = username
    self.password = password
    self.session = requests.Session()
    self.session.headers.update({'Content-Type': 'application/json'})
    self.FVT_PREFIX = "FVT_ERGO"
    self.CATEGORY_PREFIX = "category_"

  def get_token(self) -> None:
    endpoint = "/icp4d-api/v1/authorize"
    url = self.api_server_uri + endpoint
    payload = {'username': self.username, 'password': self.password}
    res = self.session.post(url, data=json.dumps(payload), verify=False)
    if (res.status_code == 200):
      self.access_token = res.json()['token']
      logger.info("retrieved the access token from {}.".format(url))
    else:
      logger.error(res.text)
      exit(-1)
    self.session.headers.update(
        {'Authorization': 'Bearer {}'.format(self.access_token)})

  def get_categories(self) -> json:
    endpoint = "/v3/categories"
    url = self.api_server_uri + endpoint

    params = {'limit': 1000}
    res = self.session.get(url, params=params)
    return [x['metadata'] for x in res.json()['resources']]

  def add_category(self, name=None, parent=None) -> Category:
    endpoint = "/v3/categories"
    url = self.api_server_uri + endpoint

    category = Category()
    category.name = name
    if parent is not None:
      category.parent_category_id = parent
    payload = json.dumps(category.to_dict())
    res = self.session.post(url, data=payload)
    if (res.status_code == 201):
      category_json = res.json()['resources'][0]
      category.id = category_json['artifact_id']
      category.version = category_json['version_id']
      category.gid = category_json['global_id']
      if 'parent_category_id' in category_json:
        category.parent_category_id = category_json['parent_category_id']
      logger.debug("new category is created.")
    else:
      logger.error(res.text)

    return res.status_code, category

  def delete_category(self, id=None, gid=None):
    endpoint = "/v3/categories/{}".format(id)
    url = self.api_server_uri + endpoint

    res = self.session.delete(url)
    if (res.status_code == 200):
      logger.debug("the category is deleted.")
    else:
      logger.debug(res.text)

    return res.status_code

  def add_term(self, postfix=0, **kwargs) -> Artifact:
    endpoint = "/v3/glossary_terms"
    url = self.api_server_uri + endpoint

    term = Artifact()
    term.name = "{}_term_{}".format(self.FVT_PREFIX, postfix)

    for key in kwargs.keys():
      if key == 'child':
        term.is_a_type_of.append({'id': kwargs.get(key).id})
      elif key == 'parent':
        term.has_type.append({'id': kwargs.get(key).id})
      elif key == 'related':
        term.related.append({'id': kwargs.get(key).id})
      elif key == 'synonym':
        term.synonym.append({'id': kwargs.get(key).id})
      elif key == 'category':
        term.parent_category = {"id": kwargs.get(key).id}

    payload = json.dumps([term.to_dict()])
    params = {"skip_workflow_if_possible": True}
    res = self.session.post(url, data=payload, params=params)
    if (res.status_code == 201):
      term.id = res.json()['resources'][0]['artifact_id']
      term.version = res.json()['resources'][0]['version_id']
      logger.info("new term: {} is created.".format(term.name))
    else:
      logger.error(res.text)

    return term

  def get_term_rels(self, term) -> Artifact:
    endpoint = "/v3/glossary_terms/"
    artifact_id = term.id
    version_id = term.version
    relationship_id = term.rel
    headers = {"limit": 10, "type": "all"}
    url = "{}/{}/versions/{}/relationships/{}".format(endpoint, artifact_id, version_id, relationship_id)
    res = self.session.get(url, headers=headers)
    if (res.status_code == 200):
      logger.info("the relationship getting done.")
      rels = res.json()
      for key in rels.keys():
        if key == "is_a_type_of_terms":
          term.is_a_type_of[0].rel = rels[key]['resources'][0]['metadata']['artifact_id']

  def delete_relationships_term(self, terms=None) -> bool:
    endpoint = "/v3/glossary_terms"
    url = self.api_server_uri + endpoint

    if not terms:
      return True

    for term in terms:
      artifact_id = term.id
      version_id = term.version
      relationship_id = term.rel
      headers = {"skip_workflow_if_possible": True}
      url = "{}/{}/versions/{}/relationships/{}".format(url, artifact_id, version_id, relationship_id)
      res = self.session.delete(url, headers=headers)
      if (res.status_code == 200):
        logger.info("the relationship removal done.")
        return True
      else:
        logger.error(res.text)
        return False

@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--env", default="xbox", help="API Server")
def wkc_add_category(username, password, env):
  wkc = WKC(username=username, password=password, env_name=env)
  wkc.get_token()
  categories = list()

  name = "{}marc".format(wkc.CATEGORY_PREFIX)
  status, category = wkc.add_category(name)

  if status == 201:
    logger.info("New category is created, name: {}, id: {}, global id: {}".format(category.name, category.id, category.gid))
    categories.append(category)
    name = "{}marc_1".format(wkc.CATEGORY_PREFIX)
    status, category = wkc.add_category(name, parent=category.id)
    if status == 201:
      logger.info("New sub category is created, name: {}, id: {}, global id: {}".format(category.name, category.id, category.gid))
      categories.append(category)
    else:
      logger.error("Failed to create new sub category.")
  else:
    logger.error("Failed to create new category.")

  [logger.info(x.name) for x in categories]

@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--env", default="xbox", help="API Server")
def wkc_add_subcategory(username, password, env):
  wkc = WKC(username=username, password=password, env_name=env)
  wkc.get_token()
  categories = list()

  name = "{}marc_1".format(wkc.CATEGORY_PREFIX)
  status, category = wkc.add_category(name, parent="8b18262c-2a90-4842-b2b9-f52c2ca001b3")

  if status == 201:
    logger.info("New category is created, name: {}, id: {}, global id: {}".format(category.name, category.id, category.gid))
    categories.append(category)
  else:
    logger.error("Failed to create new category.")

  [logger.info(x.name) for x in categories]

@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--env", default="xbox", help="API Server")
def wkc_delete_category(username, password, env):
  wkc = WKC(username=username, password=password, env_name=env)
  wkc.get_token()

  status = wkc.delete_category(id="438a5eca-3d5e-4988-8bea-c074919056e3")

  if status == 200:
    logger.info("the category is deleted.")
  else:
    logger.error("Failed to delete category.")

@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--env", default="xbox", help="API Server")
def wkc_get_category(username, password, env):
  auth = WKC(username=username, password=password, env_name=env)
  auth.get_token()

  categories = auth.get_categories()
  [logger.info(x) for x in categories]

@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--loops", default=1, help="loop number")
def fvt(username, password, loops):
  fvt = WKC(username=username, password=password)
  fvt.get_token()
  categories = fvt.get_categories()
  category = Artifact()
  category.name = "{}_category_ergo".format(fvt.FVT_PREFIX)
  for x in categories:
    if category.name == x['name']:
      logger.info("the category: {} does exist already.".format(category.name))
      category.id = x['artifact_id']
      category.version = x['version_id']

  sw = Stopwatch()
  sw.start_time()
  if not category.id:
    category = fvt.add_category()

  for n in range(0, loops, 3):
    logger.info("create a root term: {} and child: {} and parent: {} terms.".format(n, n+1, n+2))
    child = fvt.add_term(postfix=n, category = category)
    parent = fvt.add_term(postfix=n+1, category = category)
    fvt.add_term(postfix=n+2, category=category, child=child, parent=parent)

  sw.stop_time()
  logger.info("All done, {}.".format(sw.get_elapsed()))

if __name__ == "__main__":
  fvt()
  wkc_add_category()
  wkc_add_subcategory()
  wkc_get_category()
  wkc_delete_category()
