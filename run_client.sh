#!/bin/bash

java -classpath bin/:lib/json-20180813.jar -Djava.net.preferIPv4Stack=true turing.client.Client $1
