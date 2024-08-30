"""
Download an OpenSearch index to ndjson using a scroll search

Usage:
  opensearch_export_cli.py --index=<indexname> [--multiple-indexes] [--backup-folder=<backup_folder>] [--query-file=<query_file>] [--export-csv]

Options:
  --index=<indexname>  Set the index to export
  --multiple-indexes   Export multiple indexes at once. use a wildcard for --index=
                       e.g. --index=logstash*
  --backup-folder=<backup_folder>
                       Sets the folder to save the export to
  --query-file=<query_file>
                       Sets a query filter to limit what is exported
  --export-csv         Also convert the json file to csv.
"""

import json
from docopt import docopt

# library for OpenSearch Export
import opensearch_export

# Local config
import opensearch_settings

def main():
  # Load local config
  settings = opensearch_settings.load_settings()

  if settings.get('debug'):
    print ("Loaded settings : %s" % settings)

  options = docopt(__doc__)

  if options.get('--index'):
    settings['index_name'] = options['--index']

  if options.get('--query-file'):
    with open (options['--query-file'], 'rb') as f:
      settings['query_filter'] = json.load(f)
    if settings.get('debug'):
      print ("Loaded Filter : %s" % settings['query_filter'])
  else:
    settings['query_filter'] = {"match_all": {}}

  # Folder to save exported ndjson files
  if options.get('--backup-folder'):
    settings['backup_folder'] = options['--backup-folder']

  if options.get('--export-csv'):
    settings['export-csv'] = True
  else:
    settings['export-csv'] = False

  if settings.get('debug'):
    print (settings)

  if options.get('--multiple-indexes'):
    opensearch_export.process_multiindexes(settings)
    return

  if '*' in settings['index_name']:
    print ("Found wildcard in index name.")
    print ("Use --multiple-indexes to export multiple indexes")
    return

  opensearch_export.process_index(settings)


if __name__ == "__main__":
  main()
