#producer
import pika
import json
import click

RMQ_USER = 'guest'
RMQ_PASS = 'guest'
RMQ_URL = 'localhost'
EXCHANGE = 'CatalogServiceMessageHub'
ROUTING_KEY = 'v3.gss.cams.#.action'
QUEUE = 'gss_queue_cams'

@click.command()
@click.option("--file", default=None, help="cams rabbitmq message file")
def main(file):
  # declaring the credentials needed for connection like host, port, username, password, exchange etc
  credentials = pika.PlainCredentials(RMQ_USER, RMQ_PASS)
  connection = pika.BlockingConnection(pika.ConnectionParameters(host=RMQ_URL, credentials=credentials))
  channel = connection.channel()
  channel.exchange_declare(EXCHANGE, durable=True, exchange_type='topic')
  args = {"x-overflow": "reject-publish", "x-message-ttl": 3600000}
  channel.queue_declare(queue=QUEUE, arguments=args, durable=True)
  channel.queue_bind(exchange=EXCHANGE, queue=QUEUE, routing_key=ROUTING_KEY)

  # messaging to queue
  message = {
    "provider_type_id_list": ["cams"],
    "artifact_guid": "7b18d84d-8964-4b1a-82b5-546cd7687786",
    "last_updated_at": 1708412621295,
    "message_type": "DELETE_MESSAGE",
    "tenant_id_list": ["999"]
  }

  if file is not None:
    with open(file, 'rb') as fp:
      message = json.load(fp)

  channel.basic_publish(exchange=EXCHANGE, routing_key=ROUTING_KEY, body= json.dumps(message))
  channel.close()


if __name__ == "__main__":
  main()
