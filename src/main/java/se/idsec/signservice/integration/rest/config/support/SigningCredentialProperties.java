/*
 * Copyright 2020-2024 IDsec Solutions AB
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.factory.PkiCredentialConfigurationProperties;
import se.swedenconnect.security.credential.factory.PkiCredentialFactoryBean;

/**
 * Properties for representing a signing credential.
 *
 * @author Martin Lindström
 */
@Slf4j
public class SigningCredentialProperties extends PkiCredentialConfigurationProperties implements InitializingBean {

  /**
   * Tells how the credential is represented.
   */
  public enum CredentialFormat {
    /** Credential is stored in a Java KeyStore. */
    KEYSTORE,

    /** Credential is represented using a certificate and a PEM-encoded private key. */
    OPENSAML
  }

  /**
   * The format (tells how the credential is represented). Deprecated.
   */
  private CredentialFormat format;

  /**
   * The keystore file or private key file. Deprecated - use {@code resource} for a keystore and {@code private-key} for
   * a private key file.
   */
  private Resource file;

  /**
   * Deprecated format for credential.
   *
   * @param format the format
   */
  public void setFormat(final CredentialFormat format) {
    if (format != null) {
      log.warn("The 'format' property is deprecated");
      this.format = format;
    }
  }

  /**
   * The keystore file or private key file. Deprecated - use {@code resource} for a keystore and {@code private-key} for
   * a private key file.
   *
   * @param file the resource
   */
  public void setFile(final Resource file) {
    if (file != null) {
      if (this.format != null && CredentialFormat.KEYSTORE == this.format) {
        log.warn("The 'file' property is deprecated - use the 'resource' property instead");
        this.setResource(file);
      }
      else if (this.format != null && CredentialFormat.OPENSAML == this.format) {
        log.warn("The 'file' property is deprecated - use the 'private-key' property instead");
        this.setPrivateKey(file);
      }
      else {
        log.warn(
            "The 'file' property is deprecated - use the 'resource' property for keystores and the 'private-key' property for key files");
        this.file = file;
      }
    }
  }

  /**
   * Maps to {@code type}.
   *
   * @param storeType the keystore type
   */
  public void setStoreType(final String storeType) {
    if (storeType != null) {
      log.warn("The 'store-type' property is deprecated - use 'type'");
      this.setType(storeType);
    }
  }

  /**
   * Gets a {@link PkiCredential}.
   *
   * @return a {@link PkiCredential}
   * @throws Exception for errors creating the credential
   */
  public PkiCredential getSigningCredential() throws Exception {
    final PkiCredentialFactoryBean factory = new PkiCredentialFactoryBean(this);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.format == null) {
      this.format = CredentialFormat.KEYSTORE;
    }

    if (CredentialFormat.OPENSAML == this.format) {
      if (this.getPrivateKey() == null) {
        Assert.notNull(this.file, "Format is OPENSAML but privateKey (or file) is not set");
        this.setPrivateKey(this.file);
      }
    }
    else {
      if (this.getResource() == null) {
        Assert.notNull(this.file, "Format is KEYSTORE but resource (or file) is not set");
        this.setResource(this.file);
      }
    }
  }

}
