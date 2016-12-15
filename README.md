# Oracle Service Bus Transport for Apache Kafka

## Introduction
After have seen numerous requests from customers and partners about being able to integrate with Kafka, the A-Team decided to write a native transport for Oracle Service Bus (Service Bus for short) to allow the connection and data exchange with Kafka – supporting message consumption and production to Kafka topics. This is done in a way that allows Service Bus to scale jointly with Kafka, both vertically and horizontally.

This is an Open-Source project maintained by Oracle Corp.

## Features and Benefits:
The OSB Transport for Apache Kafka provides inbound and outbound connectivity with Apache Kafka. But this is definetely a oversimplification of what this transport can really do. The list below summarizes the most important features found in this implementation.

* Supports multiple Apache Kafka versions such as 0.9.X, 0.10.X and above.
* It is tested against OSB 12.1.3 and 12.2.1. Thus, compatible with SOACS as well.
* Supports inbound (Proxy Service) and outbound (Business Services) use cases.
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
The very first thing you need to do to start playing with the transport is building it from the source. The build process of this transport has been based on the best practices provided in the [product documentation section](https://docs.oracle.com/middleware/1221/osb/develop/GUID-F3574BDE-F053-4015-ACC2-4CE2473B39EA.htm#OSBDV1292) about custom transport providers development. Therefore, if you are familiar with the build process for custom transports then you should be OK following the steps below.

In a nutshell, the build process is based on Ant. The build.xml script provided has all the steps necessary to generate the implementation files (kafka-transport.ear and kafka-transport.jar) needed to deploy the transport into your Service Bus domain. However, in order to work the script relies on information of the environment about where to find the Fusion Middleware libraries necessary for the compilation steps. Thus, you will need to build the implementation files in a machine that has a Oracle Service Bus installation. You will also need some libraries from Apache Kafka - the Kafka Clients API to be more precise.

```
npm install -g yo grunt bower grunt-cli
npm install -g generator-oraclejet

yo oraclejet <app name>
```
For more information about how to install, deploy and use the transport - please read [this blog](http://www.ateam-oracle.com/osb-transport-for-apache-kafka-part-1/) which walkthrough the whole process with details and examples.

## Contributing
This project is an open source project. See [CONTRIBUTING](./CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to open source projects that have been made by the community.

## License
Copyright (c) 2014, 2016 Oracle and/or its affiliates
The Universal Permissive License (UPL), Version 1.0
