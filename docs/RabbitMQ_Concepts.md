# RabbitMQ Basic Concepts
## Overview
To better understand how RabbitMQ works, we need to dive into its core components.
In this article, we’ll take a look into exchanges, queues, and bindings, and how we can declare them programmatically within a Java application.

## Setup
As usual, we’ll use the Java client and the official client for the RabbitMQ server.
First, let’s add the Maven dependency for the RabbitMQ client:

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.12.0</version>
</dependency>
```

Next, let’s declare the connection to the RabbitMQ server and open a communication channel:

```java
ConnectionFactory factory = new ConnectionFactory();
factory.setHost("localhost");
Connection connection = factory.newConnection();
Channel channel = connection.createChannel();
```

Also, a more detailed example of the setup can be found in our Introduction to RabbitMQ.

## Exchange
In RabbitMQ, **a producer never sends a message directly to a queue**. Instead, it uses an exchange as a routing mediator.
Therefore, the exchange decides if the message goes to one queue, to multiple queues, or is simply discarded.
For instance, depending on the routing strategy, we have **four exchange types to choose from**:
- **Direct** – the exchange forwards the message to a queue based on a routing key
- **Fanout** – the exchange ignores the routing key and forwards the message to all bounded queues
- **Topic** – the exchange routes the message to bounded queues using the match between a pattern defined on the exchange and the routing keys attached to the queues
- **Headers** – in this case, the message header attributes are used, instead of the routing key, to bind an exchange to one or more queues

Moreover, **we also need to declare properties of the exchange**:
- **Name** – the name of the exchange
- **Durability** – if enabled, the broker will not remove the exchange in case of a restart
- **Auto-Delete** – when this option is enabled, the broker deletes the exchange if it is not bound to a queue
- Optional arguments

All things considered, let’s declare the optional arguments for the exchange:

```java
Map<String, Object> exchangeArguments = new HashMap<>();
exchangeArguments.put("alternate-exchange", "orders-alternate-exchange");
```

**When passing the alternate-exchange argument, the exchange redirects unrouted messages to an alternative exchange**, as we might guess from the argument name.
Next, **let’s declare a direct exchange with durability enabled and auto-delete disabled**:

```java
channel.exchangeDeclare("orders-direct-exchange", BuiltinExchangeType.DIRECT, true, false, exchangeArguments);
```

## Queue
Similar to other messaging brokers, the RabbitMQ queues **deliver messages to consumers based on a FIFO model**.
In addition, when creating a queue, **we can define several properties of the queue**:
- **Name** – the name of the queue. If not defined, the broker will generate one
- **Durability** – if enabled, the broker will not remove the queue in case of a restart
- **Exclusive** – if enabled, the queue will only be used by one connection and will be removed when the connection is closed
- **Auto-delete** – if enabled, the broker deletes the queue when the last consumer unsubscribes
- Optional arguments

Further, we’ll declare the optional arguments for the queue.
Let’s add two arguments, the message TTL and the maximum number of priorities:

```java
Map<String, Object> queueArguments = new HashMap<>();
queueArguments.put("x-message-ttl", 60000);
queueArguments.put("x-max-priority", 10);
```

Now, **let’s declare a durable queue with the exclusive and auto-delete properties disabled**:

```java
channel.queueDeclare("orders-queue", true, false, false, queueArguments);
```

## Bindings
**Exchanges use bindings to route messages to specific queues**.
Sometimes, they have a routing key attached to them, used by some types of exchanges to filter specific messages and route them to the bounded queue.
Finally, let’s **bind the queue that we created to the exchange using a routing key**:

```java
channel.queueBind("orders-queue", "orders-direct-exchange", "orders-routing-key");
```

## Conclusion
In this article, we covered the core components of RabbitMQ – exchanges, topics, and bindings. We also learned about their role in message delivery and how we can manage them from a Java application.
As always, the complete source code for this tutorial is available over on [GitHub](https://github.com/eugenp/tutorials/tree/master/messaging-modules/rabbitmq).
