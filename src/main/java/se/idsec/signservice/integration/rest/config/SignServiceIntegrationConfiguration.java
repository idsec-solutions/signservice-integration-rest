/*
 * Copyright 2020-2023 IDsec Solutions AB
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
package se.idsec.signservice.integration.rest.config;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.Setter;
import se.idsec.signservice.integration.ExtendedSignServiceIntegrationService;
import se.idsec.signservice.integration.config.ConfigurationManager;
import se.idsec.signservice.integration.config.impl.DefaultConfigurationManager;
import se.idsec.signservice.integration.config.spring.NameToSigningCredentialConverter;
import se.idsec.signservice.integration.config.spring.PropertyToX509CertificateConverter;
import se.idsec.signservice.integration.core.DocumentCache;
import se.idsec.signservice.integration.document.SignedDocumentProcessor;
import se.idsec.signservice.integration.document.TbsDocumentProcessor;
import se.idsec.signservice.integration.document.pdf.PdfSignedDocumentProcessor;
import se.idsec.signservice.integration.document.pdf.PdfTbsDocumentProcessor;
import se.idsec.signservice.integration.document.pdf.TbsPdfDocumentIssueHandler;
import se.idsec.signservice.integration.document.pdf.signpage.DefaultPdfSignaturePagePreparator;
import se.idsec.signservice.integration.document.xml.XmlSignedDocumentProcessor;
import se.idsec.signservice.integration.document.xml.XmlTbsDocumentProcessor;
import se.idsec.signservice.integration.impl.DefaultSignServiceIntegrationService;
import se.idsec.signservice.integration.impl.PdfSignaturePagePreparator;
import se.idsec.signservice.integration.process.SignRequestProcessor;
import se.idsec.signservice.integration.process.SignResponseProcessingConfig;
import se.idsec.signservice.integration.process.SignResponseProcessor;
import se.idsec.signservice.integration.process.impl.DefaultSignRequestProcessor;
import se.idsec.signservice.integration.process.impl.DefaultSignResponseProcessor;
import se.idsec.signservice.integration.security.impl.OpenSAMLIdpMetadataResolver;
import se.idsec.signservice.integration.signmessage.SignMessageProcessor;
import se.idsec.signservice.integration.signmessage.impl.DefaultSignMessageProcessor;
import se.idsec.signservice.integration.state.IntegrationServiceStateCache;
import se.idsec.signservice.integration.state.SignatureStateProcessor;
import se.idsec.signservice.integration.state.impl.DefaultSignatureStateProcessor;
import se.idsec.signservice.security.certificate.CertificateUtils;
import se.swedenconnect.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import se.swedenconnect.opensaml.saml2.metadata.provider.MetadataProvider;

/**
 * Application main configuration.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
@Configuration
@EnableConfigurationProperties
public class SignServiceIntegrationConfiguration {

  @Setter
  @Value("${application.version:1.1.0}")
  private String version;

  @Setter
  @Value("${signservice.default-policy-name:default}")
  private String defaultPolicyName;

  /** Temporary directory for caches. */
  private ApplicationTemp tempDir = new ApplicationTemp();

  /**
   * Creates (and registers) the {@link NameToSigningCredentialConverter} converter.
   *
   * @return a NameToSigningCredentialConverter
   */
  @Bean("nameToSigningCredentialConverter")
  @ConfigurationPropertiesBinding
  NameToSigningCredentialConverter nameToSigningCredentialConverter() {
    return new NameToSigningCredentialConverter();
  }

  /**
   * Creates (and registers) the {@link PropertyToX509CertificateConverter} converter.
   *
   * @return a PropertyToX509CertificateConverter
   */
  @Bean("propertyToX509CertificateConverter")
  @ConfigurationPropertiesBinding
  PropertyToX509CertificateConverter propertyToX509CertificateConverter() {
    return new PropertyToX509CertificateConverter();
  }

  /**
   * Gets the bean for the SignService Integration Service.
   *
   * @return SignServiceIntegrationService bean
   */
  @Bean
  ExtendedSignServiceIntegrationService signServiceIntegrationService(
      final ConfigurationManager configurationManager,
      final SignatureStateProcessor signatureStateProcessor,
      final SignRequestProcessor signRequestProcessor,
      final SignResponseProcessor signResponseProcessor,
      final PdfSignaturePagePreparator pdfSignaturePagePreparator) {

    final DefaultSignServiceIntegrationService service = new DefaultSignServiceIntegrationService();
    service.setPdfSignaturePagePreparator(pdfSignaturePagePreparator);
    service.setConfigurationManager(configurationManager);
    service.setSignatureStateProcessor(signatureStateProcessor);
    service.setSignRequestProcessor(signRequestProcessor);
    service.setSignResponseProcessor(signResponseProcessor);
    service.setVersion(this.version);
    return service;
  }

  /**
   * Gets the {@link SignatureStateProcessor} bean.
   *
   * @param configurationManager the configuration manager
   * @return a SignatureStateProcessor bean
   */
  @Bean
  SignatureStateProcessor signatureStateProcessor(
      final ConfigurationManager configurationManager,
      final IntegrationServiceStateCache integrationServiceStateCache) {
    DefaultSignatureStateProcessor stateProcessor = new DefaultSignatureStateProcessor();
    stateProcessor.setStateCache(integrationServiceStateCache);
    stateProcessor.setBase64Encoded(true);
    stateProcessor.setConfigurationManager(configurationManager);
    return stateProcessor;
  }

  /**
   * Gets the {@link PdfSignaturePagePreparator} bean.
   *
   * @param documentCache the document cache
   * @return a PdfSignaturePagePreparator bean
   */
  @Bean
  PdfSignaturePagePreparator pdfSignaturePagePreparator(final DocumentCache documentCache) {
    DefaultPdfSignaturePagePreparator pdfPreparator = new DefaultPdfSignaturePagePreparator();
    pdfPreparator.setDocumentCache(documentCache);
    return pdfPreparator;
  }

  /**
   * Creates a sign request processor.
   *
   * @param tbsDocumentProcessors the document processors
   * @param signMessageProcessor the optional SignMessageProcessor bean
   * @return a SignRequestProcessor bean
   */
  @Bean
  SignRequestProcessor signRequestProcessor(
      @Autowired final List<TbsDocumentProcessor<?>> tbsDocumentProcessors,
      @Autowired(required = false) final SignMessageProcessor signMessageProcessor,
      final DocumentCache documentCache) {
    final DefaultSignRequestProcessor processor = new DefaultSignRequestProcessor();
    processor.setDefaultVersion("1.4");
    processor.setTbsDocumentProcessors(tbsDocumentProcessors);
    processor.setSignMessageProcessor(signMessageProcessor);
    processor.setDocumentCache(documentCache);
    return processor;
  }

  /**
   * Gets the {@code XmlTbsDocumentProcessor} bean.
   *
   * @return the XmlTbsDocumentProcessor bean
   */
  @Bean
  XmlTbsDocumentProcessor xmlTbsDocumentProcessor() {
    return new XmlTbsDocumentProcessor();
  }

  /**
   * Gets the {@code PdfTbsDocumentProcessor} bean.
   *
   * @return the PdfTbsDocumentProcessor bean
   */
  @Bean
  PdfTbsDocumentProcessor pdfTbsDocumentProcessor() {
    return new PdfTbsDocumentProcessor();
  }

  /**
   * Creates the {@code SignMessageProcessor} bean if SignMessage support is enabled.
   *
   * @param metadataProvider the metadata provider
   * @return a SignMessageProcessor bean
   */
  @Bean
  @ConditionalOnProperty(prefix = "signservice.sign-message", name = "enabled", havingValue = "true")
  SignMessageProcessor signMessageProcessor(@Autowired final MetadataProvider metadataProvider) {
    final DefaultSignMessageProcessor signMessageProcessor = new DefaultSignMessageProcessor();
    signMessageProcessor
        .setIdpMetadataResolver(new OpenSAMLIdpMetadataResolver(metadataProvider.getMetadataResolver()));
    return signMessageProcessor;
  }

  /**
   * If SignMessage support is enabled the method returns the {@code MetadataProvider} bean.
   *
   * @param federationMetadataUrl URL from where to download metadata
   * @param validationCertificate metadata validation certificate
   * @return a MetadataProvider bean
   * @throws Exception for init errors
   */
  @Bean(initMethod = "initialize", destroyMethod = "destroy")
  @ConditionalOnProperty(prefix = "signservice.sign-message", name = "enabled", havingValue = "true")
  MetadataProvider metadataProvider(
      @Value("${signservice.sign-message.metadata.url}") String federationMetadataUrl,
      @Value("${signservice.sign-message.metadata.validation-certificate}") Resource validationCertificate)
      throws Exception {

    final X509Certificate cert = CertificateUtils.decodeCertificate(validationCertificate.getInputStream());

    final File backupFile = new File(this.tempDir.getDir(), "metadata-cache.xml");
    final HTTPMetadataProvider provider = new HTTPMetadataProvider(federationMetadataUrl, backupFile.getAbsolutePath());
    provider.setPerformSchemaValidation(false);
    provider.setSignatureVerificationCertificate(cert);
    return provider;
  }

  /**
   * Gets the {@code SignResponseProcessor} processor.
   *
   * @param signResponseProcessingConfig the SignResponseProcessingConfig bean
   * @param signedDocumentProcessors all available SignedDocumentProcessor beans
   * @return the SignResponseProcessor bean
   */
  @Bean
  SignResponseProcessor signResponseProcessor(
      @Autowired final SignResponseProcessingConfig signResponseProcessingConfig,
      @Autowired final List<SignedDocumentProcessor<?, ?>> signedDocumentProcessors) {
    DefaultSignResponseProcessor processor = new DefaultSignResponseProcessor();
    processor.setProcessingConfiguration(signResponseProcessingConfig);
    processor.setSignedDocumentProcessors(signedDocumentProcessors);
    return processor;
  }

  /**
   * Gets the {@code XmlSignedDocumentProcessor} bean
   *
   * @param signResponseProcessingConfig the SignResponseProcessingConfig bean
   * @return the XmlSignedDocumentProcessor bean
   */
  @Bean
  XmlSignedDocumentProcessor xmlSignedDocumentProcessor(
      final SignResponseProcessingConfig signResponseProcessingConfig) {
    final XmlSignedDocumentProcessor xmlProcessor = new XmlSignedDocumentProcessor();
    xmlProcessor.setProcessingConfiguration(signResponseProcessingConfig);
    return xmlProcessor;
  }

  /**
   * Gets the {@code PdfSignedDocumentProcessor} bean
   *
   * @param signResponseProcessingConfig the SignResponseProcessingConfig bean
   * @return the PdfSignedDocumentProcessor bean
   */
  @Bean
  PdfSignedDocumentProcessor pdfSignedDocumentProcessor(
      final SignResponseProcessingConfig signResponseProcessingConfig) {
    final PdfSignedDocumentProcessor pdfProcessor = new PdfSignedDocumentProcessor();
    pdfProcessor.setProcessingConfiguration(signResponseProcessingConfig);
    return pdfProcessor;
  }

  /**
   * Gets the {@code SignResponseProcessingConfig} bean.
   *
   * @return the SignResponseProcessingConfig bean
   */
  @Bean
  @ConfigurationProperties(prefix = "signservice.response.config")
  SignResponseProcessingConfig signResponseProcessingConfig() {
    return new SignResponseProcessingConfig();
  }

  /**
   * Gets the SignService {@link ConfigurationManager} bean.
   *
   * @param the properties for the configuration
   * @return the ConfigurationManager bean
   */
  @Bean
  ConfigurationManager configurationManager(
      final IntegrationServiceConfigurationProperties properties) {
    final DefaultConfigurationManager mgr = new DefaultConfigurationManager(properties.getConfig());
    mgr.setDefaultPolicyName(this.defaultPolicyName);
    return mgr;
  }

  /**
   * Gets the {@link TbsPdfDocumentIssueHandler} bean.
   *
   * @return the {@link TbsPdfDocumentIssueHandler} bean
   */
  @Bean
  TbsPdfDocumentIssueHandler tbsPdfDocumentIssueHandler() {
    return new TbsPdfDocumentIssueHandler();
  }

}
