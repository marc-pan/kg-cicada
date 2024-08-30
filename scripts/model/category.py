#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"


class Category():
  def __init__(self, id=None, name=None, gid=None, parent=None) -> None:
      self.id = id
      self.name = name
      self.gid = gid
      self.parent_category_id = parent

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

  @property
  def gid(self):
    return self._gid
  
  @gid.setter
  def gid(self, gid):
    self._gid = gid
  
  @property
  def parent_category_id(self):
    return self._parent_category_id

  @parent_category_id.setter
  def parent_category_id(self, parent):
    self._parent_category_id = parent

  def to_dict(self) -> dict:
    _category = dict()
    _category['name'] = self._name
    _category['parent_category_id'] = self._parent_category_id

    return _category
