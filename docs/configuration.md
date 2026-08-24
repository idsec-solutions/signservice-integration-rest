![Logo](https://idsec-solutions.github.io/signservice-integration-api/img/idsec.png)

# signservice-integration-rest - Configuration

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

---

## Table of contents

1. [**Application Settings**](#application-settings)

    1.1. [SignMessage Settings](#signmessage-settings)
    
2. [**Credentials Configuration**](#credentials-configuration)
    
3. [**Policy Configuration**](#policy-configuration)
    
    3.1. [PDF Signature Image Templates](#pdf-signature-image-templates)

    3.2. [PDF Signature Pages](#pdf-signature-pages)
    
    3.3. [PDF Document Prepare Settings](#pdf-document-prepare-settings)

4. [**User Configuration**](#user-configuration)
    
5. [**Cache Configuration**](#cache-configuration)
    
    5.1. [Redis Configuration](#redis-configuration)

6. [**JSON Parsing Limits**](#json-parsing-limits)

    6.1. [Sizing the Limit](#sizing-the-limit)

---

<a name="application-settings"></a>
## 1. Application Settings

This section covers the Spring Boot- and generic application settings.

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

<a name="signmessage-settings"></a>
### 1.1. SignMessage Settings

In order to enable including SignMessage elements in the requests the `signservice.sign-message.enabled` setting 
must be set to `true`.

If sending encrypted SignMessage elements should be supported the `signservice.sign-message.metadata.url` and
`signservice.sign-message.metadata.validation-certificate` must be assigned. The `url` setting points at the metadata
location where the IdP:s metadata can be found and the `validation-certificate` setting is a resource to the
signing certificate for the metadata.

```
signservice.sign-message.enabled=true
signservice.sign-message.metadata.url=https://eid.svelegtest.se/metadata/mdx/role/idp.xml
signservice.sign-message.metadata.validation-certificate=file:/opt/signservice/keys/sandbox-metadata.crt
```

<a name="credentials-configuration"></a>
## 2. Credentials Configuration

The service can be configured to use different signing credentials for different policies (see [3](#policy-configuration) below). Each credential needs to have a unique name that is later referenced in a policy.

A credential is configured with the prefix `signservice.credentials[index].`.

The [credentials-support](https://github.com/swedenconnect/credentials-support) library
is used to configure and handle credentials. See [PkiCredential Configuration](https://docs.swedenconnect.se/credentials-support/old-readme.html#generic-pkicredentialfactorybean-for-springboot-users) for how to configure a credential.

**Example:**

```
signservice.credentials[0].name=TestMySignature
signservice.credentials[0].resource=file:/etc/keys/test-my-signature.jks
signservice.credentials[0].type=JKS
signservice.credentials[0].password=secret
signservice.credentials[0].alias=test-sign
signservice.credentials[0].key-password=secret

signservice.credentials[1].name=eduSignTestSigning
signservice.credentials[1].resource=file:/etc/edusign-test/keys/sp-keystore.jks
signservice.credentials[1].store-type=JKS
signservice.credentials[1].password=Test1234
signservice.credentials[1].alias=uploadsign-sp
signservice.credentials[1].key-password=Test1234
```

<a name="policy-configuration"></a>
## 3. Policy Configuration

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
| `default-return-url` | The default URL to which the user agent along with the sign response message should be directed after a signature operation. |
| `sign-service-certificates[index]` | The sign service certificate(s). The reason that a list is specified is to enable a smooth transition if the sign service changes key/certificate. The parameter is given as a resource (prefixed with `classpath:` or `file:`).|
| `trust-anchors[index]` | The trust anchor(s). for the sign service CA. The parameter is given as a resource (prefixed with `classpath:` or `file:`). |
| `signing-credential` | The sign service integration signing credential. The value MUST be the `name` of one of the credentials (see section [3.2](#credentials-configuration) above. |
| `default-certificate-requirements.`<br />`certificate-type` | The type of certificate to request from the sign service. Should be set to `PKC`. |
| `default-certificate-requirements.`<br />`attribute-mappings[index].*` | A list of how SAML attributes are mapped to certificate contents. See [CertificateAttributeMapping](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/certificate/CertificateAttributeMapping.java). |
| `pdf-signature-image-templates[index]` | A list of PDF signature image templates. See [3.1](#pdf-signature-image-templates) below. |
| `pdf-signature-pages[index]` | A list of PDF signature pages. See [3.2](#pdf-signature-pages) below. |
| `pdf-prepare-settings` | Settings for preparing PDF documents. See [3.3](#pdf-document-prepare-settings) below. |
| `stateless` | Whether the sign service integration should be stateless or not. |
 

**Important:** There is a lot of information to be supplied for a signature policy. If several policies, that are similar to each other, it is possible to define a new policy and point at a "parent" policy, and only supply those settings that should be changed.

For this purpose the setting `parent-policy` can be used. The value should be the name of an already existing policy.

<a name="pdf-signature-image-templates"></a>
### 3.1. PDF Signature Image Templates

| Property | Description |
| :--- | :--- |
| `reference` | A unique reference for this template. |
| `svg-image-file.resource` | A resource to the SVG-file holding the template. The parameter is given as a resource (prefixed with `classpath:` or `file:`). |
| `svg-image-file.eagerly-load-contents` | Don't ask. Just set to `true`. |
| `width` | Width of SVG file in pixels. |
| `height` | Height of SVG file in pixels. |
| `include-signer-name` | Whether to include the signer's name in the image (true/false). |
| `include-signing-time` | Whether to include the signing time in the image (true/false). |
| `fields.<key>` | Dynamic fields to be included. |
| `time-zone-id` | The time zone to use when including signing time (`include-signing-time`). The default is the system's default time zone. Use Java's constants for zone ID:s, for example "Europe/Stockholm" or "UTC". |
| `date-format` | The Java date format template to use when formatting signing time. The default is `yyyy-MM-dd HH:mm z`. See Java documentation for valid format strings. |

<a name="pdf-signature-pages"></a>
### 3.2. PDF Signature Pages

| Property | Description |
| :--- | :--- |
| `id` | The unique ID for the signature page. |
| `pdf-document.resource` | A resource to the PDF-document to use as signature page. The parameter is given as a resource (prefixed with `classpath:` or `file:`). |
| `pdf-document.eagerly-load-contents` | Don't ask. Just set to `true`. |
| `rows` | The number of rows holding signature images. |
| `columns` | The number of columns holding signature images. |
| `signature-image-reference` | A reference to the signature image to insert. See `reference` in [3.1](#pdf-signature-image-templates) above. |
| `image-placement-configuration.x-position` | The X position placement for the first sign image. |
| `image-placement-configuration.y-position` | The Y position placement for the first sign image. |
| `image-placement-configuration.x-increment` | The X increment for inserting additional sign images. |
| `image-placement-configuration.y-increment` | The Y increment for inserting additional sign images. |
| `image-placement-configuration.scale` | The sign images scaling factor (-100 to 100). |

<a name="pdf-document-prepare-settings"></a>
### 3.3. PDF Document Prepare Settings

| Property | Description |
| :--- | :--- |
| `enforce-pdfa-consistency` | Defines if PDF/ A consistency check is done when adding a sign page to a document to be signed. If this setting is enabled, and an attempt to add a sign page that is not PDF/A to a PDF/A document an error will be raised. If the setting is not enabled, the document being signed will be reverted into a non-PDF/A document. The default is `false`. |
| `allow-flatten-acro-forms` | Defines whether the prepare PDF functions should flatten PDF acroforms in the document being signed. The default is `false`. |
| `allow-remove-encryption-dictionary` | Defines if documents with an encryption dictionary should have it removed. The default is `false` |

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

Note: As of Spring Boot 3, the property names have changed from `spring.redis` to `spring.data.redis`. When upgrading from an old
version of this service running under Spring Boot 2.x, make sure to update these property settings to the new names.

| Property | Description | Default |
| :--- | :--- | :--- |
| `spring.data.redis.cluster.nodes` | Comma-separated list of "host:port" pairs to bootstrap from. This represents an "initial" list of cluster nodes and is required to have at least one entry. | - |
| `spring.data.redis.database` | Database index used by the connection factory. | `0.0` |
| `spring.data.redis.host` | Redis server host. | - |
| `spring.data.redis.port` | Redis server port. | `6379` |
| `spring.data.redis.password` | `Login password of the redis server. | - |
| `spring.data.redis.timeout`  | Connection timeout (in millis). | *No timeout* |
| `spring.data.redis.ssl` | Whether to enable TLS support. | `false` |
| `spring.data.redis.url` | Connection URL. Overrides host, port, and password. User is ignored. Example: `redis://user:password@example.com:6379` | - |

<a name="json-parsing-limits"></a>
### 3.6. JSON Parsing Limits

Clients post the documents to be signed as Base64-encoded **single JSON string values** (see `SignRequestInput`,
`PreparePdfSignaturePageInput.pdfDocument` and `ProcessSignResponseInput`). The JSON parser used by the service,
Jackson, guards against oversized input with a set of streaming read limits, and the one that matters here is the
maximum length of a single string value. Its built-in default is 20,000,000 characters.

Base64 inflates content by roughly 4/3, so a raw document of about 15 MB already produces a JSON string value longer
than 20,000,000 characters — and for a stateless policy the effective ceiling is lower still, see
[3.6.1](#sizing-the-limit). When a limit is exceeded the request is rejected while it is still being parsed — before
any service logic sees it — and the client receives an HTTP 400 that does *not* use this service's normal error body:

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"Failed to read request","instance":"/v1/prepare/sandbox"}
```

Note that the response gives no indication that a size limit was the cause, and nothing is written to the service log
either. An unexplained `Failed to read request` on a request carrying a large document is therefore the symptom to
look for.

Note that this is a completely separate limit from `spring.servlet.multipart.max-request-size` and the Tomcat size
settings; raising those has no effect on it.

The setting below lets a deployment raise (or lower) this limit.

| Property | Description | Default |
| :--- | :--- | :--- |
| `signservice.json.max-string-length` | The maximum length, in characters, of a single JSON string value. This is what limits the largest document the service will accept. See the sizing rule below. | `20000000` (the jackson-core default) |

The limit applies both when a request is parsed and when the signature state is parsed back during `/v1/process`, so a
raised value covers a full signature flow. No other Jackson setting is affected — the remaining read limits keep their
jackson-core defaults, as do serialization and deserialization features, registered modules and date handling.

<a name="sizing-the-limit"></a>
#### 3.6.1. Sizing the Limit

A signature flow parses the document twice, and for a **stateless** policy the second parse is the larger of the two.
`/v1/create` returns the session state to the client, and that state contains the Base64-encoded document — but the
state as a whole is then Base64-encoded again before it is handed over. The `encodedState` string that the client
posts back to `/v1/process` therefore carries a *second* 4/3 inflation on top of the first:

| Parse | String that must fit | Size relative to the raw document |
| :--- | :--- | :--- |
| `/v1/create` request body | the document, Base64-encoded | × 4/3 |
| `/v1/process` request body | `encodedState` — Base64 of a state that contains the Base64 document | × 16/9 (≈ 1.78) |

**Size against the second row.** The rule is:

> `max-string-length` ≥ *(largest document in bytes)* × 16/9, plus headroom.

Getting this wrong is easy to miss, because the two parses fail at different document sizes and the first one keeps
working. With the default of 20,000,000 the request parse accepts documents up to roughly 15 MB, but a stateless
`/v1/process` starts failing at roughly 11 MB — so a document between those sizes produces a successful `/v1/create`
followed by a `Failed to read request` on `/v1/process`.

For a **stateful** policy the state is held server-side and the client posts back only an identifier, so only the
× 4/3 row applies.

Whether a policy is stateless is controlled by the `stateless` setting of the policy configuration, i.e.,
`signservice.config.<p>.stateless`, see [3. Policy Configuration](#policy-configuration). **The eduSign deployment is
stateless**, so the × 16/9 rule is the one to size against there.

**Example** — accepting documents of up to about 50 MB with a stateless policy:

```
signservice.json.max-string-length=90000000
```

> **A warning about oversized values.** Do not set `max-string-length` to `Integer.MAX_VALUE` in order to "turn the
> limit off". The value is accepted, but it removes the guard rather than raising it: instead of a clean parse error,
> an oversized payload then fails part-way through parsing with an `OutOfMemoryError` or a VM array-size error, which
> is far harder to diagnose and can take the service down rather than failing a single request. Decoded text is held
> as a `char[]` at two bytes per character, so a value of 2 billion characters alone requires roughly 4 GB of heap.
> Size the limit to the largest document the deployment actually intends to accept, add headroom, and check the
> result against the heap available to the container.

---

Copyright &copy; 2020-2026, [IDsec Solutions AB](http://www.idsec.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
