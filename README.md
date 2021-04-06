![Logo](https://idsec-solutions.github.io/signservice-integration-api/img/idsec.png)

# signservice-integration-rest

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A RESTful Web Service implementing the [SignService Integration API](https://github.com/idsec-solutions/signservice-integration-api).

---

## Table of contents

1. [**Introduction**](#introduction)

2. [**API and Endpoints**](#api-and-endpoints)

3. [**Configuration**](#configuration)

    3.1. [Application Settings](#application-settings)
    
    3.2. [Credentials Configuration](#credentials-configuration)
    
    3.3. [Policy Configuration](#policy-configuration)

---

<a name="introduction"></a>
## 1. Introduction

> TODO

<a name="api-and-endpoints"></a>
## 2. API and Endpoints

> TODO

<a name="configuration"></a>
## 3. Configuration

<a name="application-settings"></a>
### 3.1. Application Settings

| Property | Description | Default |
| :--- | :--- | :--- |
| `application.config.prefix` | A prefix that may be used to point at a common directory where configuration files such as keys and property files is found. Note that if a directory is given it must end with an `/`. <br /> Example: `file:///var/sign/config/`. | `classpath:` |
| `server.servlet.context-path` | The application context path. | `/` |
| `server.port` | The port for the application. | 8443 |
| `server.ssl.enabled` | Whether TLS is enabled. | `true` |
| `server.ssl.key-store` | The keystore holding the TLS server credential. | `classpath:localhost.jks` |
| `server.ssl.key-store-password` | The keystore password. | `secret` |
| `server.ssl.key-password` | The key password. | `${server.ssl.key-store-password}` |
| `server.ssl.key-store-type` | The keystore type. JKS or PKCS12. | `JKS` |
| `server.ssl.key-alias` | The keystore alias to the key entry. | `localhost` |
| `spring.servlet.`<br />`multipart.max-request-size` | Maximum size for requests. | `20MB` |
| `spring.servlet.`<br />`multipart.max-file-size | Maximum file size in multipart messages. | `20MB` |
| `management.endpoints.web.base-path` | Base path for management and health endpoints. | `/manage` |
| `management.server.port` | Management port. | `8449` |
| `management.ssl.enabled` | Is TLS enabled for management endpoints? | `true` |
| `management.ssl.key-store` | The keystore holding the TLS server credential. | `${server.ssl.key-store}` |
| `management.ssl.key-store-password` | The keystore password. | `${server.ssl.key-store-password}` |
| `management.ssl.key-password` | The key password. | `${server.ssl.key-password}` |
| `management.ssl.key-store-type` | The keystore type. JKS or PKCS12. | `${server.ssl.key-store-type}` |
| `management.ssl.key-alias` | The keystore alias to the key entry. | `${server.ssl.key-alias}` |
| `management.endpoints.`<br />`web.exposure.include` | A comma-separated list of the management endpoints to expose. | `health` |

<a name="credentials-configuration"></a>
### 3.2. Credentials Configuration

The service can be configured to use different signing credentials for different policies (see [3.3](#policy-configuration) below). Each credential needs to have a unique name that is later referenced in a policy.

For 

<a name="policy-configuration"></a>
### 3.3. Policy Configuration

This section describes how the signature policies are set up.



---

Copyright &copy; 2020-2021, [IDsec Solutions AB](http://www.idsec.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
