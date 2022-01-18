#!/bin/bash

docker run \
  -v ${PWD}/docker/components.conf:/components.conf \
  -e FLINK_REST_URL=http://localhost \
  -e FLINK_QUERYABLE_STATE_PROXY_URL=localhost:1234 \
  -e INFLUXDB_URL=http://localhost \
  -e KAFKA_ADDRESS=localhost:9092 \
  -e SCHEMA_REGISTRY_URL=http://localhost:1234 \
  -e CONFIG_FILE=conf/application.conf,/components.conf \
  -p 8085:8080 \
  nussknacker-sample-components:latest
