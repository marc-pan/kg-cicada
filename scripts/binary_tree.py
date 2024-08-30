#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"


import logging
from queue import SimpleQueue
import click
from model.category import Category
from wkc import WKC

logging.root.setLevel(logging.INFO)
logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
log_format = logging.Formatter(
    '%(asctime)s %(levelname)s [%(filename)s:%(lineno)s - %(funcName)8s() ] %(message)s')
handler.setFormatter(log_format)
logger.addHandler(handler)

class BinaryTreeNode:
    def __init__(self, wkc=None, left=None, right=None, depth=1, parent=None)->None:
        self._wkc = wkc
        self._left = left
        self._right = right
        self._depth = depth
        self._parent = parent

    @property
    def wkc(self):
        return self._wkc

    @wkc.setter
    def wkc(self, wkc):
        self._wkc = wkc
    
    @property
    def parent(self):
        return self._parent

    @parent.setter
    def parent(self, node):
        self._parent = node

    @property
    def left(self):
        return self._left
    
    @left.setter
    def left(self, left):
        self._left = left

    @property
    def right(self):
        return self._right
    
    @right.setter
    def right(self, right):
        self._right = right

    @property
    def depth(self):
        return self._depth
    
    @depth.setter
    def depth(self, depth):
        self._depth = depth

    def create(self, head=Category):
        """
        create complete binary tree, each node has left and right node. the layers is equals to depth.
        1. create head node and then put head to queue
        2. when 1st node done, pull the head node from the queue and create left node and right node of the head node and then put the queue
        3. in step 2, assign head node as parent of left and right nodes
        4. increate layer number + 1
        5. repeat step 2, 3 untill layer number is equal to depth, stop create new left and right nodes and right now the complete binary tree is created.
        """
        q = SimpleQueue()
        q.put(head)
        layer = 1
        logger.info("BTree creation in progress with root node: {}, depth: {}".format(head.name, self.depth))
        while not q.empty():
            node = q.get()
            logger.debug("current layer: {}, queue size: {}".format(layer, q.qsize()))
            if layer <= self.depth:
                left_node_name = "{}_l{}".format(node.name, layer)
                status, left = self.wkc.add_category(name=left_node_name, parent=node.id)
                if status == 201:
                    logger.info("new node name: {} in layer: {}, queue size: {}".format(left.name, layer, q.qsize()))
                    q.put(left)
                
                right_node_name = "{}_r{}".format(node.name, layer)
                status, right = self.wkc.add_category(name=right_node_name, parent=node.id)
                if status == 201:
                    logger.info("new node name: {} in layer: {}, queue size: {}".format(right.name, layer, q.qsize()))
                    q.put(right)
            
            if q.qsize() == pow(2, layer):
                layer += 1

        def delete(self, head=Category):
            """
            delete the binary tree completely from the head node
            1. go to leaf node and put them into queue
            2. delete them one by one
            3. repeat step 1&2 until the head is deleted also
            """
            pass
    
@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--env", default="xbox", help="API Server")
def wkc_btree_creation(username, password, env):
    wkc = WKC(username=username, password=password, env_name=env)
    wkc.get_token()
    root_category_name = "{}_root".format(wkc.CATEGORY_PREFIX)
    status, root_category = wkc.add_category(root_category_name)
    if status == 201:
        bt = BinaryTreeNode(wkc=wkc, depth=10)
        bt.create(root_category)

@click.command()
@click.option("--username", default="admin", help="WKC username")
@click.option("--password", default="password", help="WKC password")
@click.option("--env", default="xbox", help="API Server")
def wkc_btree_deletion(username, password, env):
    wkc = WKC(username=username, password=password, env_name=env)
    wkc.get_token()
    #TODO 
    pass


if __name__ == "__main__":
    wkc_btree_creation()
