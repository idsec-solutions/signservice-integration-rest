# An example signature flow using the Signature Service Integration REST-Service

This document shows how a client sends requests to the Signature Service Integration Service in order to:

1. Prepare a PDF document with a PDF signature page and directives on how to place a PDF signature image.

2. Create a `SignRequest` message. This is the message that is sent to the Signature Service when the user agent is directed there.

3. Process a received `SignResponse` message. This message is posted back to the client application from the Signature Service when the user has performed the signing operation. The Signature Service Integration Service will process the response and return an easy to use `SignatureResult` object.

This example illustrates how a PDF document is signed using the Sweden Connect reference signature service (and authentication is performed against the Sweden Connect Sandbox federation).

### Prepare PDF document

In the cases where a PDF signature page containing a visible PDF signature image should be used the client
may use the `prepare` method. This method accepts a PDF document that should be modified with a PDF signature page and information about the attributes that should be added to the PDF signature image.

```
1. POST /v1/prepare/default?returnDocReference=true

  {
2.  "pdfDocument" : "JVBERi0xLj...lJUVPRgo=",
3.    "signaturePagePreferences" : {
        "visiblePdfSignatureUserInformation" : {
          "signerName" : {
            "signerAttributes" : [ {
              "name" : "urn:oid:2.16.840.1.113730.3.1.241"
            } ]
          },
          "fieldValues" : {
            "idp" : "http://dev.test.swedenconnect.se/idp"
          }
        },
4.      "failWhenSignPageFull" : true,
5.      "insertPageAt" : 0
      }
  }
```

1. The URL for the method is `/v1/prepare/{profile}`, where {profile} is the Signature Service Integration profile policy that is to be used. In this example, the `default` profile is used.<br /><br />The `returnDocReference` parameter tells the server whether it should return a document reference instead of the entire document as the result. If the server is running in a stateful mode, this is the default.

2. The `pdfDocument` parameter contains the Base64-encoded PDF document to modify.

3. The `signaturePagePreferences` (see [PdfSignaturePagePreferences](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/document/pdf/PdfSignaturePagePreferences.java)) declares the preferences for the signature page. This example mainly uses the defaults of the profile policy. The `visiblePdfSignatureUserInformation` contains the information that should be included in the PDF signature image. This information is obtained from the user assertion from the authentication phase. Note that knowledge about the [PdfSignatureImageTemplate](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/document/pdf/PdfSignatureImageTemplate.java) that is configured in the profile policy is needed<sup>1</sup>.

4. A PDF document may be signed several times which means that the PDF signature page will contain several PDF signature images. However, a PDF signature page has a limit on how many signature images it can contain. The `failWhenSignPageFull` setting tells whether the `prepare`-method should fail with an error if there is no more place for a signature image (in an existing PDF signature page), or whether such errors should lead to a signature where no signature image is inserted.

5. The `insertPageAt` gives the page number of the supplied document where the PDF signature page should be inserted. 0 means "as last page".

See [PreparePdfSignaturePageInput](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/document/pdf/PreparePdfSignaturePageInput.java) for a complete definition of the input object.

> \[1\]: To obtain the definition of a given profile policy, use `GET /v1/policy/get/{profile}`.

**The response:**

```
  {
1.  "policy" : "default",
2.  "updatedPdfDocumentReference" : "163ea75d-1b19-4570-a791-40b07da53bfb",
3.  "visiblePdfSignatureRequirement" : {
      "signerName" : {
        "signerAttributes" : [ {
          "name" : "urn:oid:2.16.840.1.113730.3.1.241"
        } ]
      },
      "fieldValues" : {
        "idp" : "http://dev.test.swedenconnect.se/idp"
      },
      "templateImageRef" : "idsec-image",
      "scale" : -74,
      "page" : 2,
      "xposition" : 37,
      "yposition" : 165
    }
  }
```

1. The `policy` property gives the profile policy that was used.

2. The `updatedPdfDocumentReference` contains a reference to the updated document. 

3. The `visiblePdfSignatureRequirement` property contains the object that should be supplied in the `create` method. This gives information about where in the document the PDF signature image should be inserted.

See [PreparedPdfDocument](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/document/pdf/PreparedPdfDocument.java) for a complete definition of the response object.

### Create a SignRequest message

The `create` method is used by the client in order to create the DSS `SignRequest` message that is to be sent to the signature service.

```
1. POST /v1/create/default

  {
2.  "correlationId" : "ce0f28a3-8bf9-4bdc-9fb6-a897539a29b7",
3.  "signRequesterID" : "http://sandbox.swedenconnect.se/testmyeid",
4.  "returnUrl" : "https://localhost:9445/testmyeid/sign/response",
5.  "authnRequirements" : {
      "authnServiceID" : "http://dev.test.swedenconnect.se/idp",
      "authnContextClassRefs" : [ "http://id.elegnamnden.se/loa/1.0/loa3" ],
      "requestedSignerAttributes" : [ {
        "name" : "urn:oid:1.2.752.29.4.13",
        "value" : "195207306886"
      }, {
        "name" : "urn:oid:2.5.4.42",
        "value" : "Majlis"
      }, {
        "name" : "urn:oid:2.5.4.4",
        "value" : "Medin"
      }, {
        "name" : "urn:oid:2.16.840.1.113730.3.1.241",
        "value" : "Majlis Medin"
      } ]
    },
6.  "tbsDocuments" : [ {
      "id" : "6bdf1a78-ffcc-475e-a795-f277bdd5f049",
      "contentReference" : "163ea75d-1b19-4570-a791-40b07da53bfb",
      "mimeType" : "application/pdf",
      "visiblePdfSignatureRequirement" : {
        "signerName" : {
          "signerAttributes" : [ {
            "name" : "urn:oid:2.16.840.1.113730.3.1.241"
          } ]
        },
        "fieldValues" : {
          "idp" : "http://dev.test.swedenconnect.se/idp"
        },
        "templateImageRef" : "idsec-image",
        "scale" : -74,
        "page" : 2,
        "xposition" : 37,
        "yposition" : 165
      }
    } ],
7.  "signMessageParameters" : {
      "signMessage" : "Hej Majlis! Detta är en testunderskrift.",
      "performEncryption" : true,
      "mimeType" : "text",
      "mustShow" : true
    }
  }
```
1. The URL for the method is `/v1/create/{profile}`, where {profile} is the Signature Service Integration profile policy that is to be used. In this example, the default profile is used.

2. The `correlationId` is an opaque string that may be supplied by the client. This enables logging consolidation between the client and the Signature Service Integration Service.

3. The `signRequesterID` is the SAML entityID of the SAML SP that authenticated the user, and who is the requesting entity of the signature operation.

4. The `returnUrl` is the URL where the client wants to receive the `SignResponse` message when the Signature Service posts the user back (along with the response message).

5. The `authnRequirements` property gives the authentication requirements, i.e., the requirements that the Signature Service should put on the user authentication when the user is "authenticated for signature". It contains:

  a. `authnServiceID` - The SAML entityID of the Identity Provider that should perform the "authentication for signature". In almost all cases this is the same IdP at which the user logged in.
  
  b. `authnContextClassRefs` - The AuthnContextClassRef URI(s) that we request that the user is authenticated under. In almost all cases this is the same AuthnContextClassRef as was received when the user logged in.
  
  c. `requestedSignerAttributes` - A list of SAML attribute names and values. These are attributes that we require the Identity Provider to provide in the assertion following the "authentication for signature". Note that any attribute that is listed as `signedAttributes` under the `visiblePdfSignatureRequirement` (see below) must also be present in the `requestedSignerAttributes` listing.
  
6. The `tbsDocuments` is a list of to-be-signed documents. In our example we sign the PDF document that was received in the `updatedPdfDocumentReference` property in the `prepare` response. Also note that we add the `visiblePdfSignatureRequirement` property that was also received in the `prepare` response.

7. The `signMessageParameters` property holds information about a text that should be displayed to the user during signing. It is the Identity Provider's responsibility to display this text. Only supported within the Sweden Connect federation.

See [SignRequestInput](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/SignRequestInput.java) for a complete definition of the input object.

**The response:**

```
  {
1.  "state" : {
      "id" : "2f725211-d572-4979-8fd4-678918136103"
    },
2.  "signRequest" : "PD94bWwg...xdWVzdD4=",
3.  "relayState" : "2f725211-d572-4979-8fd4-678918136103",
4.  "binding" : "POST/XML/1.0",
5.  "destinationUrl" : "https://sig.sandbox.swedenconnect.se/sigservice/request"
  }
```

1. When the client is using the Signature Service Integration Service it is responsible of keeping a handle to the state of the current operation. The `state` property should be saved in the client state so that it can be supplied in the `process` call.

2. The `signRequest` holds the Base64-encoded `SignRequest` that is to be posted to the signature service in the `EidSignRequest` form parameter.

3. The `relayState` is an identifier for this operation (and is always the same as the state ID). It is posted to the signature service in the `RelayState` form parameter.

4. The `binding` is the binding of the operation. It is posted to the signature service in the `Binding` form parameter.

5. The `destinationUrl` holds the signature service address to which we should post the request (`action`).

See [SignRequestData](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/SignRequestData.java) for a complete definition of the response object.

### Posting the SignRequest to the Signature Service

The data received in `create` response should be included in a XHTML form looking something like:

In this example we assume that we assigned the response message above to the variable `createResponse`.

```
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'>
<body onload='document.forms[0].submit()'>
  <noscript>
    <p><strong>Note:</strong> Since your browser does not support JavaScript,
    you must press the Continue button once to proceed.</p>
  </noscript>
  <form action='${createResponse.destinationUrl}' method='post'>
    <div>
      <input type='hidden' name='Binding' value='${createResponse.binding}'/>
      <input type='hidden' name='RelayState' value='${createResponse.relayState}'/>
      <input type='hidden' name='EidSignRequest' value='${createResponse.signRequest}'/>
    </div>
    <noscript>
      <div>
        <input type='submit' value='Continue'/>
      </div>
    </noscript>
  </form>
</body>
```


### Receiving and Processing the SignResponse

Once the signature service has processed the request it will post back the response to the URL given in the `returnUrl` parameter of the `create` call. The POST form at the signature service typically looks something like:

```
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'>
<body onload='document.forms[0].submit()'>
  <noscript>
    <p><strong>Note:</strong> Since your browser does not support JavaScript,
    you must press the Continue button once to proceed.</p>
  </noscript>
  <form action='https://sp.example.com/sigResponseHandler' method='post'>
    <div>
      <input type='hidden' name='Binding' value='POST/XML/1.0'/>
      <input type='hidden' name='RelayState' value='2f725211-d572-4979-8fd4-678918136103'/>
      <input type='hidden' name='EidSignResponse' value='PD94bWwgdmV...c3BvbnNlPg=='/>
    </div>
    <noscript>
      <div>
        <input type='submit' value='Continue'/>
      </div>
    </noscript>
  </form>
</body>
```

The controller receiving this post data now needs to process it in order to get the signed document. This is done using the `process` call.

```
POST /v1/process

{
  "signResponse" : "PD94bWwgdmV...c3BvbnNlPg==",
  "relayState" : "2f725211-d572-4979-8fd4-678918136103",
  "state" : {
    "id" : "2f725211-d572-4979-8fd4-678918136103"
  }
}
```

Note that the `state` parameter MUST be exactly as the object received in the `state` property of the `create` response.

See [ProcessSignResponseInput](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/ProcessSignResponseInput.java) for a complete definition of the input object.

**The response:**

```
  {
1.  "id" : "2f725211-d572-4979-8fd4-678918136103",
2.  "correlationId" : "ce0f28a3-8bf9-4bdc-9fb6-a897539a29b7",
3.  "signedDocuments" : [ {
      "id" : "6bdf1a78-ffcc-475e-a795-f277bdd5f049",
      "signedContent" : "JVBERi0xLjMKJ...cxMjkyCiUlRU9GCg==",
      "mimeType" : "application/pdf"
    } ],
    "signerAssertionInformation" : {
4.     "signerAttributes" : [ {
        "type" : "saml",
        "name" : "urn:oid:1.2.752.201.3.14",
        "value" : "http://www.w3.org/2001/04/xmlenc#sha256;fGJwNzjp+Y04Z1TR4pH9VolEfZ1OnLvXqb6ZZJMnp3s=",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      }, {
        "type" : "saml",
        "name" : "urn:oid:1.2.752.29.4.13",
        "value" : "195207306886",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      }, {
        "type" : "saml",
        "name" : "urn:oid:2.16.840.1.113730.3.1.241",
        "value" : "Majlis Medin",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      }, {
        "type" : "saml",
        "name" : "urn:oid:2.5.4.42",
        "value" : "Majlis",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      }, {
        "type" : "saml",
        "name" : "urn:oid:1.2.752.201.3.12",
        "value" : "eyJ0eXAiOi...mhs5fTBxXf0",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      }, {
        "type" : "saml",
        "name" : "urn:oid:1.3.6.1.5.5.7.9.1",
        "value" : "1952-07-30",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      }, {
        "type" : "saml",
        "name" : "urn:oid:2.5.4.4",
        "value" : "Medin",
        "nameFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      } ],
5.    "authnInstant" : 1603355834117,
6.    "authnServiceID" : "http://dev.test.swedenconnect.se/idp",
7.    "authnContextRef" : "http://id.elegnamnden.se/loa/1.0/loa3",
      "authnType" : "saml",
8.    "assertionReference" : "_e0a3b0cda2259923bdb88ca871ef78eb",
9.    "assertion" : "PD94bWwgdmVyc2...XNzZXJ0aW9uPg=="
  }
}
```

1. The ID of the operation.

2. The `correlationId` is the same value as was passed in the `create` call. Useful for log correlation.

3. The `signedDocuments` contains all the signed documents. In this example it contains the signed PDF document.

4. The `signedAttributes` contains the identity attributes delivered by the Identity Provider during "authentication for signature". Note that a signature service can choose to only include the attributes that were used to validate the signature operation.

5. The `authnInstant` holds the authentication instant (in millis since epoch).

6. The `authnServiceID` holds the SAML entityID of the Identity Provider that was used to authenticate the user for the signature operation.

7. The `authnContextRef` holds the AuthnContextClassRef URI for the authentication.

8. The `assertionReference` holds the ID of the SAML assertion that was issued during the authentication for signature operation.

9. A signature service can also be configured to include the entire SAML assertion in the `SignResponse`. If this is available it is set in the `assertion` property.

See [SignatureResult](https://github.com/idsec-solutions/signservice-integration-api/blob/master/src/main/java/se/idsec/signservice/integration/SignatureResult.java) for a complete definition of the response object.