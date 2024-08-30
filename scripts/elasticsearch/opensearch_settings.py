from opensearchpy import OpenSearch
import pprint


def load_settings():

  #command to get the SHA-256 fingerprint from the elasticsearch server
  #openssl s_client --connect 192.168.1.1:9200 </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' | openssl x509 -noout -in - --fingerprint -sha256

  host = 'http://turquoise-vm.fyre.ibm.com:9200'
  # host = 'https://opensearch-wkc.apps.oc4-9.pl.eurolabs.ibm.com'
  # get the password with the command oc extract secret/elasticsearch-master-secret --to=-
  ELASTIC_PASSWORD = "fxjbtn1d" # for testing only

  client = OpenSearch(
      hosts = [host],
      http_auth = ("elastic", ELASTIC_PASSWORD),
      use_ssl = False,
      verify_certs = False,
      timeout=300
  )

  settings = { 'es' : client }

  # The filename used
  settings['FileName'] = 'WKC'

  # Enable debug logging
  settings['debug'] = True

  # Default bulk size
  settings['chunk_size'] = 10000

  # Default disable bulk request to OpenSearch
  settings['bulk_import'] = False

  return settings

if __name__ == '__main__':
  print ("This is the config and settings for OpenSearch Exporter")
  settings = load_settings()
  pprint.pprint(settings)

  es = settings['es']
  info = es.info()
  print(f"Welcome to {info['version']['distribution']} {info['version']['number']}!")
