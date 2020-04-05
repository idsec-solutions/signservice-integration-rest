/*
 * Copyright 2019-2020 IDsec Solutions AB
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
package se.idsec.signservice.integration.rest.converters;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import se.idsec.signservice.security.certificate.CertificateUtils;

/**
 * Converts property strings into {@link X509Certificate} objects.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
@Component
@ConfigurationPropertiesBinding
public class PropertyToX509CertificateConverter implements Converter<String, X509Certificate> {
  
  @Autowired
  private ApplicationContext applicationContext;  

  /** {@inheritDoc} */
  @Override
  public X509Certificate convert(final String source) {
    
    final Resource resource = this.applicationContext.getResource(source);
    
    try {
      return CertificateUtils.decodeCertificate(resource.getInputStream());
    }
    catch (CertificateException | IOException e) {
      throw new IllegalArgumentException(String.format("Failed to convert %s to a X509Certificate", source));
    }
  }

}
