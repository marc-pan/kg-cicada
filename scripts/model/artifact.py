#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"


class Artifact():
  def __init__(self, id=None, name=None, version=None, gid=None, rel=None) -> None:
      self.id = id
      self.name = name
      self.version = version
      self.abbreviations = []
      self.parent_category = gid
      self.relationships = rel
      self.categories = []
      self.has_type = []
      self.is_a_type_of = []
      self.synonym = []
      self.related = []

  @property
  def id(self):
    return self._id

  @id.setter
  def id(self, id):
    self._id = id

  @property
  def name(self):
    return self._name

  @name.setter
  def name(self, name):
    self._name = name

  def to_dict(self) -> dict:
    _artifact = dict()
    _artifact['name'] = self._name
    _artifact['parent_category'] = self.parent_category
    _artifact['categories'] = self.categories
    _artifact['has_type_terms'] = self.has_type
    _artifact['is_a_type_of_terms'] = self.is_a_type_of
    _artifact['synonym_terms'] = self.synonym
    _artifact['related_terms'] = self.related
    _artifact['abbreviations'] = self.abbreviations

    return _artifact
