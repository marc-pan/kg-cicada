#!/usr/bin/env python3

import sys
import json, os
import hashlib
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


def checksum_verify(fp):
  # TODO document why this method is empty
  pass

def file_info(filename):
  with open(filename) as f:
    for i, _ in enumerate(f):
      continue
  return i + 1


def split_file(settings :dict, dirname :str):
  chunk_size = settings['chunk_size']

  with open(settings['bulk_file'], 'r') as infile:
    file_number = 0
    chunk = []
    for line in infile:
      chunk.append(line)
      if len(chunk) == chunk_size:
        fullpath = dirname + "/bulk/" + 'wkc_bulk_{}.ndjson'.format(file_number)
        with open(fullpath, 'w') as outfile:
          outfile.writelines(chunk)
        file_number += 1
        chunk = []

    if chunk:
      fullpath = dirname + "/bulk/" + 'wkc_bulk_{}.ndjson'.format(file_number)
      with open(fullpath, 'w') as outfile:
        outfile.writelines(chunk)


def process_index(settings: dict):
  line_no = 0
  bulkfile = settings['bulk_file']
  dirname = os.path.dirname(settings['bulk_file'])
  target_index_name = settings['index_name']

  if not os.path.exists(bulkfile):
    with open(bulkfile, 'w', newline='\n') as output_file:
      line_no = 0
      filejson = settings['import_file']

      with open(filejson, 'r') as f:
        for line in f:
          line_dict = json.loads(line)
          metadata = {"index": { "_index": target_index_name, "_id": line_dict['_id']}}
          output_file.write(json.dumps(metadata) + u'\n')
          source = line_dict['_source']
          output_file.write(json.dumps(source) + u'\n')
          output_file.flush()
          line_no += 1
          print("Already load %s line" % line_no, end='\r', file=sys.stdout, flush=True)
  else:
    print("File already exists!")

  print("%s number of line are loaded" % line_no)

  chunk_size = settings['chunk_size']
  if line_no > chunk_size:
    print("Going to split the large file into smaller")
    split_file(settings, dirname)
    print("Split done.")


def bulk_import(settings :dict):
  es = settings['es']
  if not es.indices.exists(settings['index_name']):
    print("Target index not found!!!")
    return

  bulk_dir = os.path.dirname(settings['bulk_file']) + "/bulk"
  bulk_filelist = [os.path.join(bulk_dir, name) for name in os.listdir(bulk_dir) if os.path.isfile(os.path.join(bulk_dir, name))]
  file_count = 0
  for file in bulk_filelist:
    with open(file, 'r') as f:
      data = [json.loads(l) for l in f.readlines()]
      rc = es.bulk(data)
      if rc["errors"]:
        print("There were errors:")
        for item in rc["items"]:
          print(f"{item['index']['status']}: {item['index']['error']['type']}")
      else:
        file_count += 1
        print(f"Bulk-inserted {len(rc['items'])} items, {file_count}/{len(bulk_filelist)}", end='\r', file=sys.stdout, flush=True)


def doc_count(settings :dict) -> any:
  search_q = {"query": {"match_all": {}}}
  es = settings['es']
  if not es.indices.exists(settings['index_name']):
    print("Target index not found!!!")
    return -1

  retries = 0
  retry_limit = 3

  while retries < retry_limit:
      try:
        if retries != 0:
          print("Trying call index doc count with %s times." % retries)
        doc_count = es.count(body=search_q, index='wkc')
        break
      except Exception as e:
        retries += 1
        if retries == retry_limit:
          raise e

  return doc_count['count']

