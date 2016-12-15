# Oracle Service Bus Transport for Apache Kafka

## Introduction
After have seen numerous requests from customers and partners about being able to integrate with Kafka, the A-Team decided to write a native transport for Oracle Service Bus (Service Bus for short) to allow the connection and data exchange with Kafka – supporting message consumption and production to Kafka topics. This is done in a way that allows Service Bus to scale jointly with Kafka, both vertically and horizontally.

This is an Open-Source project maintained by Oracle Corp.

## Features and Benefits:
The OSB Transport for Apache Kafka provides inbound and outbound connectivity with Apache Kafka. But this is definetely a oversimplification of what this transport can really do. The list below summarizes the most important features found in this implementation.

* Supports multiple Apache Kafka versions such as 0.9.X, 0.10.X and above.
* It is tested against OSB 12.1.3 and 12.2.1. Thus, compatible with SOACS as well.
* Supports inbound (Proxy Service) and outbound (Business Services) use cases.
* Leverages popular open-source technologies
* Full lifecycle management for template based SPA
* Built in accessibility support
* Support for internationalization (28 languages and 160+ locales)
* Rich set of UI components
* Advanced two-way binding with a common model layer
* Powerful routing system supporting single-page application navigation
* Smart resource management
* For intermediate & advanced JS devs

## Gettting Started
Oracle Blah provides a XYZ. To install and build, type:

```
npm install -g yo grunt bower grunt-cli
npm install -g generator-oraclejet

yo oraclejet <app name>
```
For more information about how to install, deploy and use the transport - please read [this blog](http://www.ateam-oracle.com/osb-transport-for-apache-kafka-part-1/) which walkthrough the whole process with more details.

## Contributing
This project is an open source project. See [CONTRIBUTING](./CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to open source projects that have been made by the community.

## License
Copyright (c) 2014, 2016 Oracle and/or its affiliates
The Universal Permissive License (UPL), Version 1.0
