"""
Upload an OpenSearch index to ndjson using a bulk api

Usage:
  opensearch_import_cli.py --index=<indexname> [--import-file=<import_file>] [--bulk-file=<bulk_file>] [--chunk-size=<chunk_size>] [--bulk-import=<BOOL>]

Options:
  --index=<indexname>  The target index
  --import-file=<import_file>  The file path is a set of the index docs
  --bulk-file=<bulk_file>  The bulk file is used to import into the index
  --chunk-size=<chunk>  The number of line of the import file is used to split the large data into small chunk file [default: 10000]
  --bulk-import=BOOL  A flag is indicated to start a bulk import [default: False]
"""

import os
from docopt import docopt

# Load import library
import opensearch_import as os_import

# Local config
import opensearch_settings


def check_file(settings: dict):
  file_ndjson = settings['import_file']
  if not os.path.isfile(file_ndjson):
    print ("Not found the file %s" % file_ndjson)
    exit(-1)


def main():
  # Load local config
  settings = opensearch_settings.load_settings()

  if settings.get('debug'):
    print ("Loaded settings : %s" % settings)

  options = docopt(__doc__)

  if options.get('--index'):
    settings['index_name'] = options['--index']

  if options.get('--import-file'):
    settings['import_file'] = options['--import-file']

  if options.get('--bulk-file'):
    settings['bulk_file'] = options['--bulk-file']

  if options.get('--chunk-size') and options['--chunk-size'].isdigit():
    settings['chunk_size'] = int(options['--chunk-size'])

  if options.get('--bulk-import') == 'True':
    settings['bulk_import'] = True

  if settings.get('debug'):
    print (settings)

  check_file(settings)
  os_import.process_index(settings)

  if settings.get('bulk_import'):
    os_import.bulk_import(settings)
    doc_count = os_import.doc_count(settings)
    print("There are {} documents in index wkc".format(doc_count))

if __name__ == "__main__":
  main()
