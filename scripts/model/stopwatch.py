#!/bin/env python3
# -*- coding: utf-8 -*-
__author__ = "panxiny@cn.ibm.com"

import time


class Stopwatch():
  def __init__(self) -> None:
      self.start_time
      self.stop_time

  def time_convert(self, sec):
    mins = sec // 60
    sec = sec % 60
    hours = mins // 60
    mins = mins % 60
    return "exec time: {0}:{1}:{2}".format(int(hours), int(mins), sec)

  def start_time(self):
    self.start_time = time.time()

  def stop_time(self):
    self.stop_time = time.time()

  def get_elapsed(self) -> str:
    elapsed_time = self.stop_time - self.start_time
    return self.time_convert(elapsed_time)
