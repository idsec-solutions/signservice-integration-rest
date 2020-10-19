/*
 * Copyright 2020 Litsec AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.idsec.signservice.integration.rest.config.support;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.idsec.signservice.security.sign.SigningCredential;
import se.idsec.signservice.security.sign.impl.KeyStoreSigningCredential;
import se.idsec.signservice.security.sign.impl.OpenSAMLSigningCredential;

/**
 * Properties for representing a signing credential.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
@Slf4j
public class SigningCredentialProperties implements InitializingBean {

  /**
   * Tells how the credential is represented.
   */
  public static enum CredentialFormat {
    /** Credential is stored in a Java KeyStore. */
    KEYSTORE,

    /** Credential is represented using a certificate and a PEM-encoded private key. */
    OPENSAML
  }

  /** The format (tells how the credential is represented). */
  @Setter
  private CredentialFormat format;

  /** The credential name. */
  @Setter
  private String name;

  /** Holds the keystore file (KEYSTORE) or private key file (OPENSAML). */
  @Setter
  private Resource file;

  /** Holds the keystore password (KEYSTORE). */
  @Setter
  private char[] password;

  /** Holds the type of keystore (KEYSTORE). Defaults to JKS. */
  @Setter
  private String storeType;

  /** Holds the keystore alias (KEYSTORE). */
  @Setter
  private String alias;

  /**
   * Holds the key password for a keystore (KEYSTORE) and optionally the password for the PKCS#8 private key file
   * (OPENSAML). If not set it defaults to {@link #password} (KEYSTORE).
   */
  @Setter
  private char[] keyPassword;

  /** Holds the certificate of the credential (OPENSAML). */
  @Setter
  private X509Certificate certificate;
  
  public SigningCredential getSigningCredential() throws Exception {
    if (CredentialFormat.KEYSTORE.equals(this.format)) {
      final KeyStoreSigningCredential cred =
          new KeyStoreSigningCredential(this.file, this.password, this.storeType, this.alias, this.keyPassword);
      cred.setName(this.name);
      return cred;
    }
    else if (CredentialFormat.OPENSAML.equals(this.format)) {
      final PrivateKey privateKey = KeySupport.decodePrivateKey(this.file.getInputStream(), this.keyPassword);
      final BasicX509Credential x509Credential = new BasicX509Credential(this.certificate, privateKey);
      final OpenSAMLSigningCredential cred = new OpenSAMLSigningCredential(x509Credential);
      cred.setName(this.name);
      return cred;
    }
    else {
      throw new IllegalArgumentException("Unsupported format - " + this.format);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.format == null) {
      this.format = CredentialFormat.KEYSTORE;
      log.info("Credential format is not set, defaulting to {}", this.format);
    }

    Assert.notNull(this.name, "Property 'name' must be set");

    if (CredentialFormat.KEYSTORE.equals(this.format)) {
      Assert.notNull(this.file, "Property 'file' must be set");
      Assert.notNull(this.password, "Property 'password' must be set");
      Assert.hasText(this.alias, "Property 'alias' must be set");

      if (this.storeType == null) {
        this.storeType = KeyStore.getDefaultType();
        log.info("Keystore type is not set, defaulting to {}", this.storeType);
      }
      if (this.keyPassword == null) {
        this.keyPassword = this.password;
        log.info("Key password is not set, using the value of 'password'");
      }
    }
    else if (CredentialFormat.OPENSAML.equals(this.format)) {
      Assert.notNull(this.file, "Property 'file' must be set");
      Assert.notNull(this.certificate, "Property 'certificate' must be set");
    }
    else {
      throw new IllegalArgumentException("Unsupported credential format: " + this.format);
    }
  }

}
