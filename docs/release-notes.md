![Logo](https://idsec-solutions.github.io/signservice-integration-api/img/idsec.png)

# signservice-integration-rest - Release Notes

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 


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
