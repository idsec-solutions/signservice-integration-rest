![Logo](https://idsec-solutions.github.io/signservice-integration-api/img/idsec.png)

# signservice-integration-rest

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

<!--
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.idsec.signservice.integration/signservice-integration-rest/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.idsec.signservice.integration/signservice-integration-rest)
-->

A RESTful Web Service implementing the [SignService Integration API](https://github.com/idsec-solutions/signservice-integration-api).

---

## Table of contents

1. [**Introduction**](#introduction)

2. [**API and Endpoints**](#api-and-endpoints)

3. [**Configuration**](#configuration)

    3.1. [Application Settings](#application-settings)
    
    3.2. [Credentials Configuration](#credentials-configuration)
    
    3.3. [Policy Configuration](#policy-configuration)
    
    3.3.1. [PDF Signature Image Templates](#pdf-signature-image-templates)

    3.3.2. [PDF Signature Pages](#pdf-signature-pages)

    3.4. [User Configuration](#user-configuration)
    
    3.5. [Cache Configuration](#cache-configuration)
    
    3.5.1. [Redis Configuration](#redis-configuration)

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
| `spring.servlet.`<br />`multipart.max-file-size` | Maximum file size in multipart messages. | `20MB` |
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

A credential is configured with the prefix `signservice.credentials[index].`.

There are two different formats supported, `KEYSTORE` (a JKS or PKCS12) and `OPENSAML` (PKCS#8 encoded private key and the DER-encoded certificate).

The table below declares all settings for a credential instance:

| Property | Description |
| :--- | :--- |
| `format` | The format on the credential, `KEYSTORE` or `OPENSAML`. |
| `name` | The unique name for the credential. |
| `file` | Holds the keystore (KEYSTORE) or the private key (OPENSAML). |
| `password` | The keystore password (for KEYSTORE only). |
| `store-type` | Holds the type of keystore (KEYSTORE). Defaults to JKS. |
| `alias` | Holds the keystore alias (KEYSTORE). |
| `key-password` | Holds the key password for a keystore (KEYSTORE) and optionally the password for the PKCS#8 private key file (OPENSAML). If not set it defaults to `password` (KEYSTORE). |
| `certificate` | Holds the certificate of the credential (OPENSAML). |


Example:

```
signservice.credentials[0].format=KEYSTORE
signservice.credentials[0].name=TestMySignature
signservice.credentials[0].file=${application.config.prefix}sandbox/keys/test-my-signature.jks
signservice.credentials[0].store-type=JKS
signservice.credentials[0].password=secret
signservice.credentials[0].alias=test-sign
signservice.credentials[0].key-password=secret

signservice.credentials[1].format=KEYSTORE
signservice.credentials[1].name=eduSignTestSigning
signservice.credentials[1].file=${application.config.prefix}edusign-test/keys/sp-keystore.jks
signservice.credentials[1].store-type=JKS
signservice.credentials[1].password=Test1234
signservice.credentials[1].alias=uploadsign-sp
signservice.credentials[1].key-password=Test1234
```

<a name="policy-configuration"></a>
### 3.3. Policy Configuration

This section describes how the signature policies are set up.

In the main configuration there must be a property that points at a "policy configuration file", `signservice.integration.policy-configuration-resource=<policy config file>`.

This file is described in the table below.

Also, since there may be more than one policy defined, the default policy must also be given. This is done using the `signservice.default-policy-name` setting, e.g.:

```
signservice.default-policy-name=eduSign
```

Below follows a description of a policy configuration file.

Each entry is prefixed with `signservice.config.<p>` where `<p>` is the policy name.

| Property | Description |
| :--- | :--- |
| `policy` | The name of the policy. Must be the same as `<p>`. |
| `default-signature-algorithm` | Default signature algorithm. The default is `http://www.w3.org/2001/04/xmldsig-more#rsa-sha256`. |
| `default-authn-context-ref` | The default Authentication Context Class Ref to use if not supplied by the client. |
| `sign-service-id` | The entityID for the sign service that we are communicating with. |
| `default-destination-url` | Default URL where to send SignRequest messages. |
| `sign-service-certificates[index]` | The sign service certificate(s). The reason that a list is specified is to enable a smooth transition if the sign service changes key/certificate. The parameter is given as a resource (prefixed with `classpath:` or `file://`).|
| `trust-anchors[index]` | The trust anchor(s). for the sign service CA. The parameter is given as a resource (prefixed with `classpath:` or `file://`). |
| `signing-credential` | The sign service integration signing credential. The value MUST be the `name` of one of the credentials (see section [3.2](#credentials-configuration) above. |
| `default-certificate-requirements.`<br />`certificate-type` | The type of certificate to request from the sign service. Should be set to `PKC`. |
| `default-certificate-requirements.`<br />`attribute-mappings[index].*` | A list of how SAML attributes are mapped to certificate contents. See [CertificateAttributeMapping](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/certificate/CertificateAttributeMapping.java). |
| `pdf-signature-image-templates[index]` | A list of PDF signature image templates. See [3.3.1](#pdf-signature-image-templates) below. |
| `pdf-signature-pages[index]` | A list of PDF signature pages. See [3.3.2]() below. |
| `stateless` | Whether the sign service integration should be stateless or not. |
 

**Important:** There is a lot of information to be supplied for a signature policy. If several policies, that are similar to each other, it is possible to define a new policy and point at a "parent" policy, and only supply those settings that should be changed.

For this purpose the setting `parent-policy` can be used. The value should be the name of an already existing policy.

<a name="pdf-signature-image-templates"></a>
#### 3.3.1. PDF Signature Image Templates

| Property | Description |
| :--- | :--- |
| `reference` | A unique reference for this template. |
| `svg-image-file.resource` | A resource to the SVG-file holding the template. The parameter is given as a resource (prefixed with `classpath:` or `file://`). |
| `svg-image-file.eagerly-load-contents` | Don't ask. Just set to `true`. |
| `width` | Width of SVG file in pixels. |
| `height` | Height of SVG file in pixels. |
| `include-signer-name` | Whether to include the signer's name in the image (true/false). |
| `include-signing-time` | Whether to include the signing time in the image (true/false). |
| `fields.<key>` | Dynamic fields to be included. |

<a name="pdf-signature-pages"></a>
#### 3.3.2. PDF Signature Pages

| Property | Description |
| :--- | :--- |
| `id` | The unique ID for the signature page. |
| `pdf-document.resource` | A resource to the PDF-document to use as signature page. The parameter is given as a resource (prefixed with `classpath:` or `file://`). |
| `pdf-document.eagerly-load-contents` | Don't ask. Just set to `true`. |
| `rows` | The number of rows holding signature images. |
| `columns` | The number of columns holding signature images. |
| `signature-image-reference` | A reference to the signature image to insert. See `reference` in [3.3.1](#pdf-signature-image-templates) above. |
| `image-placement-configuration.x-position` | The X position placement for the first sign image. |
| `image-placement-configuration.y-position` | The Y position placement for the first sign image. |
| `image-placement-configuration.x-increment` | The X increment for inserting additional sign images. |
| `image-placement-configuration.y-increment` | The Y increment for inserting additional sign images. |
| `image-placement-configuration.scale` | The sign images scaling factor (-100 to 100). |

<a name="user-configuration"></a>
### 3.4. User Configuration

The SignService Integration service requires users to authenticate when sending requests to the service. Currently only basic authentication is implemented.

In the main configuration file the following setting needs to be present.

```
signservice.security.user-configuration=file:///var/app/config/signservice-users.properties
```

The stand-alone configuration file for users is on the format:

```
signservice.user.<user-name>.roles=<A comma-separated list of roles>
signservice.user.<user-name>.policies=<A comma-separated list of policies>
signservice.user.<user-name>.password=<the password>
```

Currently, the are only two roles, ADMIN and USER. The ADMIN has access to everything.

The `policies` setting is a comma-separated list of names of signature policies (as described above).

The `password` setting supports two types of passwords:

**Cleartext passwords:** Prefixed with `{noop}`, for example: `signservice.user.client1.password={noop}not-so-secret`.

**Hashed password:** Prefixed with `{bcrypt}`, for example: `signservice.user.client2.password={bcrypt}$2y$05$SfhUgUJGpxi6LbF/r3Ja9uilZiTsL.OV1/PrDV3QIzqHRlUGwrkYG`.

The passwords should be hashed using:

```
> htpasswd -nbB <USER> <PASSWORD>
```

<a name="cache-configuration"></a>
### 3.5. Cache Configuration

If the SignService Integration service is running in a stateful mode it needs to keep a cache. This section describes the cache settings for the application.

| Property | Description | Default | 
| :--- | :--- | :--- |
| `signservice.cache.state.max-age` | The maximum time that state should be kept in the cache. Value is given in milliseconds. | `3600000` (one hour) |
| `signservice.cache.state.cleanup-interval` | How often should the clean-up deamon clean the cache from expired state objects? Value is given in milliseconds. | `300000` (every 5 minutes) |
| `signservice.cache.document.max-age` | The maximum time that uploaded documents should be kept in the cache. Value is given in milliseconds. | `240000` (4 minutes) |
| `signservice.cache.document.cleanup-interval` | How often should the clean-up deamon clean the cache from expired state objects? Value is given in milliseconds. | `300000` (every 5 minutes) |
| `spring.redis.enabled` | Is Redis enabled? If `true` see [3.5.1](#redis-configuration) below. If `false` in-memory caches are used. | `false` |
    
<a name="redis-configuration"></a>
#### 3.5.1. Redis Configuration

| Property | Description | Default | 
| :--- | :--- | :--- |
| `spring.redis.cluster.nodes` | Comma-separated list of "host:port" pairs to bootstrap from. This represents an "initial" list of cluster nodes and is required to have at least one entry. |
| `spring.redis.database` | Database index used by the connection factory. | `0.0` |
| `spring.redis.host` | Redis server host. | - |
| `spring.redis.port` | Redis server port. | `6379` |
| `spring.redis.password` | `Login password of the redis server. | - |
| `spring.redis.timeout` | Connection timeout (in millis). | *No timeout* |
| `spring.redis.ssl` | Whether to enable TLS support. | `false` |
| `spring.redis.url` | Connection URL. Overrides host, port, and password. User is ignored. Example: `redis://user:password@example.com:6379` | - |

---

Copyright &copy; 2020-2021, [IDsec Solutions AB](http://www.idsec.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
