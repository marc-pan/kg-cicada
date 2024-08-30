#!/usr/bin/env python3

import sys
import json, csv, os
import hashlib
import traceback
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

SEARCH_TIMEOUT = "search timed out"
FIELD_KEYWORD_SUFFIX = ".keyword"
JSON_LINE_SUFFIX = '.ndjson'
CHECKSUM_SUFFIX = '.checksums'
scroll_ids = []


def make_folders(settings):
  #uncompressed files
  if os.path.exists(settings['backup_folder']):
    if not os.path.exists(settings['fullpath']):
      print ("making folder : %s" % settings['fullpath'] )
      os.makedirs(settings['fullpath'])


def count_lines(filename):
  with open(filename) as f:
    for i, l in enumerate(f):
      continue
  return i + 1


def calc_checksum(filename):
  try:
    BLOCKSIZE = 65536
    hasher = hashlib.sha1()
    with open(filename, 'rb') as afile:
      buf = afile.read(BLOCKSIZE)
      while len(buf) > 0:
        hasher.update(buf)
        buf = afile.read(BLOCKSIZE)
      file_sha1 = hasher.hexdigest()
      filesize = os.path.getsize(filename)
      return file_sha1, filesize
  except:
    print ("Failed to calculate sha1 or filesize")
    sys.exit(1)


def finish_folder(settings, total_event_count):
  files = os.listdir(settings['fullpath'] )
  all_checksums = {}
  all_count = 0
  file_all_checksums = settings['fullpath'] + '/all.checksums'
  if not os.path.exists ( file_all_checksums ):
    for filename in files:
      if filename.endswith(".checksums"):
        fullfilename = settings['fullpath'] + '/' + filename
        with open (fullfilename, 'r') as f:
          contents = json.loads( f.read() )
          for item in contents.keys():
            all_count += contents[item]['events']
            all_checksums[item] = contents[item]
    print ("Total items : %s" % all_count)
    if all_count == total_event_count:
      print ("Exported every item in the index")
      print (all_checksums)
      with open(file_all_checksums, 'w') as f:
        f.write( json.dumps( all_checksums ))
    else:
      print (settings)
      print ("AllCount != TotalEventCount")
      print ("AllCount                : %s" % all_count)
      print ("TotalEventCount (index) : %s" % total_event_count)
  else:
    print ("found all.checksums, nothing to do")


def search_group(es, index_name, settings, field_filter, exclude_field = False, all_items = False):
  print ("Exporting : %s for index %s" % ( field_filter, index_name ) )

  search_q = {
    "size" : 10000,
    "query" : settings['query_filter'] }

  # apply field filter
  if 'field_filter' in settings.keys():
    search_q['query']['bool']['filter'] = [ { "match_phrase": { settings['field_name'] + FIELD_KEYWORD_SUFFIX  : settings['field_filter'] } } ]

    if exclude_field:
      #All items with no group
      search_q["query"]["bool"]["must_not"] =  [ { "exists": { "field": settings['field_name'] + FIELD_KEYWORD_SUFFIX } } ]
    elif not all_items:
      #select 1 group
      search_q["query"]["bool"]["filter"].append( { "match_phrase": { settings['field_name'] + FIELD_KEYWORD_SUFFIX : field_filter } }  )

  print("The query body: %s" % search_q)
  results = es.search(index=index_name, body=search_q, size=10000, rest_total_hits_as_int=True, scroll="10m")
  expected = results['hits']['total']
  scroll_id = results['_scroll_id']
  scroll_ids.append(scroll_id)

  print ("Total items to export : %s" % ( f'{expected:,}' ) )

  if expected == 0:
    print ("No events returned from search")
    return { "failed" : True, "message" : "search returned 0 results" }

  current_exported = 0
  search_after = False
  while True:
    scroll_body = {"scroll": "10m", "scroll_id": scroll_id}
    if search_after == True:
      print ("Continue scroll search.", end='\r', file=sys.stdout, flush=True)
      results = es.scroll(body=scroll_body, rest_total_hits_as_int=True)

    current_exported += len (results['hits']['hits'] )
    if all_items:
      print ("Exported : %s" % ( f'{current_exported:,}' ) )
    else:
      print ("%s : Exported : %s" % ( field_filter, f'{current_exported:,}' ) )

    msg_write_results = write_results(settings, field_filter, expected, results, ignore_count = True, exclude_field = exclude_field )
    if msg_write_results['failed']:
      return { "failed" : True, "message" : "Failed  writing file - (over 10k) export" }

    # break if got all results
    if len (results['hits']['hits'] ) == 0 or current_exported >= expected:
      return msg_write_results
    else:
      search_after = True


def write_results(settings, field_filter, expected, results, ignore_count = False, exclude_field = False):
  if not results['timed_out']:
    if results['_shards']['failed'] == 0:
      if results['hits']['total'] == expected or ignore_count or exclude_field:
        export_file = open ( settings['fullpath'] + '/' + field_filter + JSON_LINE_SUFFIX, 'a')
        for item in results['hits']['hits']:
          export_file.write(json.dumps(item))
          export_file.write('\n')
        export_file.close()

        message = { "failed" : False, "message" : "completed" }

        return message
      else:
        print ("Got a different count of results")
        print (results['hits']['total']['value'])
        print (expected)
        return { "failed" : True, "message" : "Got a different number of results" }
    else:
      print ("some shards failed")
      return { "failed" : True, "message" : "some shards failed" }
  else:
    print (SEARCH_TIMEOUT)
    return { "failed" : True, "message" : SEARCH_TIMEOUT }


def process_group(es, index_name, settings, group, exclude_field = False, all_items = False ):
  #search and write results to disk
  message = search_group(es, index_name, settings, group, exclude_field = exclude_field, all_items = all_items )

  source = settings['fullpath'] + '/' + group + JSON_LINE_SUFFIX

  if message['failed']:
    print (message)
    if os.path.exists(source):
      print ("Failed, removing file")
      os.remove(source)
  else:
    #This can happen when there are no items when exclude field
    if message['message'] == "search returned 0 results":
      return

    #write checksum file
    file_sha1, filesize = calc_checksum(source)
    file_lc = count_lines(source)
    checksums = { group + ".ndjson" : { "sha1" : file_sha1, "size" : filesize, "events" : file_lc }}
    print ("Exported file stats : %s" % checksums, end='\r', file=sys.stdout, flush=True)
    with open (settings['fullpath'] + '/' + group + CHECKSUM_SUFFIX, 'w') as f:
      f.write(json.dumps(checksums))
      f.close()


def export_index(es, settings, exclude_field = False, all_items = True):
  settings['fullpath'] = settings['backup_folder'] + '/' + settings['index_name']

  if 'NoGroup' in settings.keys() and settings['NoGroup'] != False:
    print ("No time field - based on NoGroup in settings")
    exclude_field = False #All in one export
    all_items = True

  if not es.indices.exists( index = settings['index_name'] ):
    print ("Index does not exist : %s" % settings['index_name'] )

  #Create a check to see if all documents in this index have been exported
  if settings['debug']:
    print ("Export index : %s" % settings)

  make_folders(settings)

  group = settings['FileName']
  file_ndjson = settings['fullpath'] + '/' + group + JSON_LINE_SUFFIX
  file_sha = settings['fullpath'] + '/' + group + CHECKSUM_SUFFIX
  if not os.path.exists( file_sha ):
    if os.path.exists( file_ndjson ):
      print ("Removing file %s and re-exporting results" % file_ndjson)
      os.remove( file_ndjson )
    process_group(es, settings['index_name'], settings, group, exclude_field = exclude_field, all_items = all_items )
    if settings['export-csv']:
      convert_csv(file_ndjson)

#export events for an index
def process_index(settings, all_items = True):
    es = settings['es']

    settings['fullpath'] = settings['backup_folder'] + '/' + settings['index_name']
    settings['all_checksum']   = settings['backup_folder'] + '/' + settings['index_name'] + '/all.checksums'

    # Tests to see if script should be called
    # 1. check if the index exists
    # 2. check for all.checksums in the folder
    if es.indices.exists( index = settings['index_name'] ):
      if not os.path.exists(settings['all_checksum']):
        print ("Found index : %s" % settings['index_name'] )
        count_items = es.count( index = settings['index_name'] )
        print ("Index %s contains %s documents" % ( settings['index_name'], f'{count_items["count"]:,}' ))
        try:
          export_index(es, settings, 'none', all_items = all_items)
          #writes 'all.checksums' file
          #If the script is run again, it won't re-export a 2nd time
          finish_folder(settings, count_items['count'])
        except Exception:
          print ("Export failed in ProcessIndex")
          traceback.print_exc()
        finally:
          if len(scroll_ids) != 0:
            print("Clean up all scrolls in the current search context", flush=True)
            es.clear_scroll(scroll_id=scroll_ids[0])
      else:
        print ("found an all.checksums file, skipping folder %s" % settings['fullpath'] )
    else:
      print ("Index does not exist : %s" % settings['index_name'] )

#convertCSV functions for reading ndjson and writing csv
#nested objects are converted to dotted notation
# { "ip" : { "address" : "" }}
# to { "ip.address" : "" }
def convert_csv_flatten_dict(item, basename):
  newitem = {}
  for item2 in item.keys():
    if basename != '':
      itemkey = basename + "." + item2
    else:
      itemkey = item2
    if isinstance(item[item2], dict):
      newitem.update( convert_csv_flatten_dict(item[item2], itemkey) )
    else:
      newitem[itemkey] = repr(item[item2])
  return newitem

def convert_csv_flatten_item(item):
  newitem = {}
  for item2 in item.keys():
    if isinstance(item[item2], dict):
      if item2 == '_source':
        newitem.update ( convert_csv_flatten_dict(item[item2], '') )
      else:
        newitem.update ( convert_csv_flatten_dict(item[item2], item2) )
    else:
      newitem[item2] = repr(item[item2])
  return newitem

def convert_csv_write_csv_file(filejson, filecsv, eventkeys):
  with open(filecsv, 'w', newline='')  as output_file:
    dict_writer = csv.DictWriter(output_file, fieldnames=eventkeys)
    dict_writer.writeheader()

    #read the JSON file again and write csv file
    with open(filejson, 'r') as f:
      for line in f.readlines():
        linejson = convert_csv_flatten_item ( json.loads(line) )
        dict_writer.writerow(linejson)

#reads JSON file and finds all field names
#used for the first line of the csv file
def convert_csv_read_json_file(filename):
  event_keys = []
  with open(filename, 'r') as f:
    for line in f.readlines():
      line_json = convert_csv_flatten_item ( json.loads(line) )
      for i in line_json.keys():
        if i not in event_keys:
          event_keys.append(i)
  event_keys.sort()
  return event_keys

def convert_csv(file_json):
  file_csv  = file_json + '.csv'
  print ("converting file %s to csv" % file_json)
  event_keys = convert_csv_read_json_file(file_json)
  convert_csv_write_csv_file(file_json, file_csv, event_keys)

def process_multiindexes(settings):
  res = settings['es'].indices.get(index=settings['index_name'])
  #print list of indexes to be exported
  print ("Selected %s indices to export" % len(res.keys()))
  for index_name in res.keys():
    count_items = settings['es'].count( index = index_name )
    print ("Found index %s which contains %s documents" % ( index_name, f'{count_items["count"]:,}' ))

  for index_name in res.keys():
    settings['index_name'] = index_name
    process_index(settings)

if __name__ == "__main__":
  print ("This is the OpenSearch Export library - please use opensearch_export_cli.py instead")
