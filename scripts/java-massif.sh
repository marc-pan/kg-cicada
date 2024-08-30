#!/bin/sh

valgrind --trace-children=yes --tool=massif $*
