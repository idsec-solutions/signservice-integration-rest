![Logo](https://idsec-solutions.github.io/signservice-integration-api/img/idsec.png)

# signservice-integration-rest - Release Notes

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

## 2.4.1

**Date:** 2026-08-24

### Configurable JSON parsing limit for large documents

Documents are sent to the service Base64-encoded inside a single JSON string value. Jackson, the JSON parser used by
the service, limits such a value to 20,000,000 characters by default, and since Base64 inflates content by roughly 4/3
this meant that documents larger than about 15 MB were rejected while the request was still being parsed — before any
signature logic saw them. The failure surfaced as an unexplained HTTP 400 `Failed to read request` and could not be
worked around through configuration, since raising `spring.servlet.multipart.max-request-size` has no effect on this
limit.

This limit is now configurable. To accept documents of up to about 50 MB with a stateless policy:

```
signservice.json.max-string-length=90000000
```

The setting also applies to the signature state that is parsed back when a sign response is processed, so both
`/v1/create` and `/v1/process` accept the larger document. A deployment that sets nothing keeps the previous
behaviour.

Note when picking a value that for a stateless policy the state posted back to `/v1/process` is Base64-encoded a
second time, making it about 16/9 of the raw document size rather than 4/3. Size against that figure — otherwise
`/v1/create` succeeds and the matching `/v1/process` call fails. See the configuration page for the full sizing rule.

Size the value to the largest document the deployment intends to accept and check it against the container's heap —
setting it to `Integer.MAX_VALUE` removes the guard rather than raising it and turns an oversized request into an
`OutOfMemoryError`.

**Configuration changes:**

The new `signservice.json.max-string-length` setting is described in
[JSON Parsing Limits](configuration.html#json-parsing-limits) in the
[signservice-integration-rest - Configuration](configuration.html) page.

## 2.4.0

**Date:** 2026-06-17

### Document size increased significantly on first sign

The bug described in [Issue #85](https://github.com/idsec-solutions/signservice-integration/issues/85) was fixed.

## 2.3.2

**Date:** 2025-03-14

### Formatting of signing time indications in PDF signature pages

It is now possible to specify the time zone and the date format for the signing time strings included in PDF signing pages. See documentation for `time-zone-id` and `date-format` in section [3.1](configuration.html#pdf-signature-image-templates) in the [signservice-integration-rest - Configuration](configuration.html) page.

## 2.3.1

**Date:** 2025-01-16

### Bugfixes

The application would fail to start if values (that have sensisble defaults) were missing. This has been fixed.

PolicyPermissionEvaluator did not load as bean by Spring. This has been fixed by appropriate annotation settings.

## 2.3.0

**Date:** 2024-12-20

### Java 21 and updated dependencies

Updated to latest versions of all dependencies and now runs on Java 21

### Solved issue with PDF/A and PDF AcroForms

PDF related issues were addressed:

- A PDF document being signed that have a PDF AcroForm (form open for user input) will not validate in some PDF tools like Acrobat Reader. If an AcroFrom is found in a PDF document that is signed for the first time, it can now be rendered (locked) with a warning returned to the requester to provide information about the change of the prepared PDF document.

- A PDF document being signed that has an encryption dictionary can't be updated and saved by the signing process. Any such dictionary can now be removed with a warning returned to the requester to provide information about the change of the prepared PDF document.

- If the document being signed was PDF/A, and the signpage being inserted was not, the entire document will no longer be a compliant PDF/A document.

---

**Configuration changes:**

A new policy configuration setting, `pdf-prepare-settings`, has been introduced for how to handle PDF/A consistency and issues concerning open PDF forms, see [PDF Document Prepare Settings](#configuration.html#pdf-document-prepare-settings).

**API changes:**

The PDF prepare call, `/v1/prepare/{policy}`, has been changed so that the `returnDocReference` is set as a query parameter (with a boolean value), instead of as a `returnDocumentReference` field of the `signaturePagePreferences` element in the input data passed to the prepare-method.

The service is backwards compatible and will accept the old, but deprecated way, of passing whether a document reference should be used. However, it is recommended that clients are updated according to the new way of invoking the prepare-method.

Also, the response object returned by the PDF prepare method, `/v1/prepare/{policy}`, has been extended to contain the `prepareReport` element. This element may contain a list of actions that were performed on the PDF, and also a list of warnings.

```json
   ...
   "prepareReport" : {
     "actions" : [ "flattened-acroform", "removed-encryption-dictionary" ]
   },
   ...
```

Possible actions are:

- `flattened-acroform` - The document was unsigned but had an AcroForm with user input form fields. This AcroForm was flattened.

- `removed-encryption-dictionary` - The document contained an encryption dictionary. This was removed.

If the `pdf-prepare-settings` of the profile configuration (see [PDF Document Prepare Settings](#configuration.html#pdf-document-prepare-settings)), has its `enforce-pdfa-consistency` setting set to `false`, a warning about PDF/A inconsistency may be inserted. 

```json
   ...
   "prepareReport" : {
     ...
     "warnings" : [ "pdfa-inconsistency" ]
   },
   ...
```
The `pdfa-inconsistency` states that the document being prepared in PDF/A, but the sign page is not. The result is that the document being signed will be reverted into a non-PDF/A document

> This can be prohibited by setting the policy value `enforce-pdfa-consistency` to `true`. In these cases an error will be reported instead of a warning.

The API has also been extended with new error codes, see [Signature Service Integration Service - Error Codes](https://idsec-solutions.github.io/signservice-integration-api/errors.html). The new codes are:

- `error.document.pdfa-consistency-check-failed` - PDF/A consistency check failed. Typically this happens when a document being signed is in PDF/A, but the sign page is not and the `enforce-pdfa-consistency` policy setting is set.

- `error.document.pdf-contains-acroform` - PDF document contains an Acroform (and policy is not configured to flatten such forms - `allow-flatten-acro-forms` is `false`).

- `error.document.pdf-flatten-acroform-failed` - Failed to flatten existing Acroform in document.

- `error.document.pdf-contains-encryption-dictionary` - PDF document contains an encryption dictionary (and policy is not configured to remove that - `allow-remove-encryption-dictionary` is `false`).
