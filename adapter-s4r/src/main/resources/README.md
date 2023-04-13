# Overview

This NB Starlight for RabbitMQ Adapter allows publishing messages to or consuming messages from
* a Pulsar cluster with [S4R](https://docs.datastax.com/en/streaming/streaming-learning/use-cases-architectures/starlight/rabbitmq/index.html) AMQP Protocol handler for Pulsar.


## Example NB Yaml


# Usage

```bash
## RabbitMQ S4R Producer
$ <nb_cmd> run driver=s4r -vv cycles=100M threads=2 num_clnt=2 yaml=s4r_producer.yaml config=s4r_config.properties bootstrap_server=PLAINTEXT://localhost:5672

## RabbitMQ S4R Consumer
$ <nb_cmd> run driver=s4r -vv cycles=100M threads=2 num_clnt=2 yaml=s4r_consumer.yaml config=s4r_config.properties bootstrap_server=PLAINTEXT://localhost:5672
```

## NB S4R Adapter specific CLI parameters


