# Oracle Service Bus Transport for Apache Kafka

## Introduction
This project provides a native transport for [OSB](http://www.oracle.com/technetwork/middleware/service-bus/overview/index-096326.html) (Oracle Service Bus) that allows connectivity with [Apache Kafka](https://kafka.apache.org/). By using native APIs, the transport allows resilient and high speed access to Apache Kafka clusters. Integration developers can benefit from this transport in the implementation of use cases that requires the integration to/from Apache Kafka with applications (SaaS and On-Premise) supported by OSB, as well as technologies such as JMS, HTTP, MSMQ, Coherence, Tuxedo, FTP, etc.

#### Using the Kafka Transport for Inbound Processing (From Kafka to the World)

![Alt text](./images/kafka-transport-inbound.png?raw=true)

#### Using the Kafka Transport for Outbound Processing (From the World to Kafka)

![Alt text](./images/kafka-transport-outbound.png?raw=true)

This is an Open-Source project maintained by Oracle.

## Features and Benefits:
The OSB Transport for Apache Kafka provides inbound and outbound connectivity with Apache Kafka. But this is definetely a oversimplification of what this transport can really do. The list below summarizes the most important features found in this implementation.

* Supports multiple Apache Kafka versions such as 0.9.X, 0.10.X and above.
* It is tested against OSB 12.1.3/12.2.1. Thus, compatible with SOACS as well.
* Supports inbound (Proxy Service) and outbound (Business Service) use cases.
* Allows both text/binary payload types to flow through Service Bus pipelines.
* Allows inbound processing to be spread out over multiple concurrent threads.
* Completely integrated with the OSB lifecycle. Smart start of the endpoints.
* Allows sync/async commits when the option 'enable.auto.commit' is disabled.
* Allows association with native WebLogic Work Managers for maximum work control.
* Allows message level partitioning using Transport Headers for outbound scenarios.
* Allows fine tuning over delivery semantics by supporting multiple ack modes.
* Provides native response headers during outbound scenarios for better control.
* Allows the implementation of native Kafka properties using custom properties.
* Allows the development of OSB projects using both OSB Console and JDeveloper.
* Provides JVM properties that controls some behaviors and allows log debugging.
* Intelligently detects which Apache Kafka version is available on the classpath.

## Gettting Started
The very first thing you need to do to start playing with the transport is building it from the sources. The build process of this transport has been completely based on the best practices described in the [product documentation section](https://docs.oracle.com/middleware/1221/osb/develop/GUID-F3574BDE-F053-4015-ACC2-4CE2473B39EA.htm#OSBDV1292) about custom transports development. Therefore, if you are familiar with the build process for custom transports then you should be OK following the steps below.

In a nutshell, the build process is based on Ant. The [build.xml](./build.xml) script provided encapsulates all the necessary steps to generate the implementation files (kafka-transport.ear and kafka-transport.jar) needed to deploy the transport into your Service Bus domain. But in order to work, the script relies on information from the environment. Especifically, information about where to find the Fusion Middleware JAR files necessary for the code compilation. Thus, you will need to build the implementation files in a machine that has Oracle Service Bus.

The quickest way to load all the Fusion Middleware information into the environment is sourcing the setDomainEnv.sh script from your domain:

```
source $FMW_HOME/user-projects/domains/<DOMAIN_NAME>/bin/setDomainEnv.sh <ENTER>
```

Next, you will need to specify in the [build.properties](./build.properties) file the location of the Kafka Clients API JAR file:

```
### Apache Kafka Clients API
kafka.clients.api=/opt/kafka_2.11-0.10.0.1/libs/kafka-clients-0.10.1.0.jar
```

Now you can simply execute the script by typing 'ant' in the command-line. Once the build finishes, the implementation files will be generated under the newly created 'build' folder. Alternatively, the implementation files will also be proactively copied into your Fusion Middleware installation.

The last step is the deployment of the implementation files into your Service Bus domain. To make things easier, the [install.py](./install/install.py) script encapsulates the details about how to connect to the WebLogic domain, perform the deployment and commiting the changes. Therefore, get into the 'install' folder and type:

```
java weblogic.WLST install.py <ENTER>
```

The script will ask information about the location of the implementation files and connection details of the WebLogic domain.

For a deeper introduction into the Kafka transport, please read a series of [two blogs](http://www.ateam-oracle.com/osb-transport-for-apache-kafka-part-1/) written in the Oracle A-Team chronicles website. They will provide details about how to better use the transport and how to configure it to afford more complex scenarios.

## License
Copyright (c) 2014, 2016 Oracle and/or its affiliates
The Universal Permissive License (UPL), Version 1.0
