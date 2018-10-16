package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataHealthIndicator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceDefaultCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.MetadataQueryProtocolMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurator;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdPMetadataController;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;

import java.net.URL;

/**
 * This is {@link SamlIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPMetadataConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Lazy
    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @DependsOn("samlIdPMetadataGenerator")
    @SneakyThrows
    @Autowired
    public MetadataResolver casSamlIdPMetadataResolver(@Qualifier("samlIdPMetadataLocator") final SamlIdPMetadataLocator samlMetadataLocator) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val resolver = new InMemoryResourceMetadataResolver(samlMetadataLocator.getMetadata(), openSamlConfigBean.getObject());
        resolver.setParserPool(this.openSamlConfigBean.getIfAvailable().getParserPool());
        resolver.setFailFastInitialization(idp.getMetadata().isFailFast());
        resolver.setRequireValidMetadata(idp.getMetadata().isRequireValidMetadata());
        resolver.setId(idp.getEntityId());
        return resolver;
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataController samlIdPMetadataController() {
        return new SamlIdPMetadataController(samlIdPMetadataGenerator(), samlIdPMetadataLocator());
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataGenerator")
    @Bean(initMethod = "initialize")
    @SneakyThrows
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new FileSystemSamlIdPMetadataGenerator(samlIdPMetadataLocator(),
            samlSelfSignedCertificateWriter(),
            idp.getEntityId(), this.resourceLoader,
            casProperties.getServer().getPrefix(), idp.getScope());
    }

    @ConditionalOnMissingBean(name = "samlSelfSignedCertificateWriter")
    @Bean
    @SneakyThrows
    public SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter() {
        val url = new URL(casProperties.getServer().getPrefix());
        val generator = new DefaultSamlIdPCertificateAndKeyWriter();
        generator.setHostname(url.getHost());
        generator.setUriSubjectAltNames(CollectionUtils.wrap(url.getHost().concat("/idp/metadata")));
        return generator;
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataLocator")
    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new FileSystemSamlIdPMetadataLocator(idp.getMetadata().getLocation());
    }

    @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader() {
        return new SamlRegisteredServiceMetadataResolverCacheLoader(
            openSamlConfigBean.getIfAvailable(),
            httpClient.getIfAvailable(),
            samlRegisteredServiceMetadataResolvers());
    }

    @ConditionalOnMissingBean(name = "metadataQueryProtocolMetadataResolver")
    @Bean
    public SamlRegisteredServiceMetadataResolver metadataQueryProtocolMetadataResolver() {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getIfAvailable();
        return new MetadataQueryProtocolMetadataResolver(samlIdp, cfgBean);
    }

    @ConditionalOnMissingBean(name = "fileSystemResourceMetadataResolver")
    @Bean
    public SamlRegisteredServiceMetadataResolver fileSystemResourceMetadataResolver() {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getIfAvailable();
        return new FileSystemResourceMetadataResolver(samlIdp, cfgBean);
    }

    @ConditionalOnMissingBean(name = "urlResourceMetadataResolver")
    @Bean
    public SamlRegisteredServiceMetadataResolver urlResourceMetadataResolver() {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getIfAvailable();
        return new UrlResourceMetadataResolver(samlIdp, cfgBean);
    }

    @ConditionalOnMissingBean(name = "classpathResourceMetadataResolver")
    @Bean
    public SamlRegisteredServiceMetadataResolver classpathResourceMetadataResolver() {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getIfAvailable();
        return new ClasspathResourceMetadataResolver(samlIdp, cfgBean);
    }

    @ConditionalOnMissingBean(name = "groovyResourceMetadataResolver")
    @Bean
    public SamlRegisteredServiceMetadataResolver groovyResourceMetadataResolver() {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getIfAvailable();
        return new GroovyResourceMetadataResolver(samlIdp, cfgBean);
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataResolvers")
    @Bean
    public SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers() {
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();

        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getIfAvailable();
        plan.registerMetadataResolver(metadataQueryProtocolMetadataResolver());
        plan.registerMetadataResolver(fileSystemResourceMetadataResolver());
        plan.registerMetadataResolver(urlResourceMetadataResolver());
        plan.registerMetadataResolver(classpathResourceMetadataResolver());
        plan.registerMetadataResolver(groovyResourceMetadataResolver());

        val configurers =
            this.applicationContext.getBeansOfType(SamlRegisteredServiceMetadataResolutionPlanConfigurator.class, false, true);

        configurers.values().forEach(c -> {
            val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring saml metadata resolution plan [{}]", name);
            c.configureMetadataResolutionPlan(plan);
        });
        return plan;
    }

    @ConditionalOnMissingBean(name = "defaultSamlRegisteredServiceCachingMetadataResolver")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() {
        return new SamlRegisteredServiceDefaultCachingMetadataResolver(
            casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes(),
            chainingMetadataResolverCacheLoader()
        );
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataHealthIndicator")
    @Bean
    public HealthIndicator samlRegisteredServiceMetadataHealthIndicator() {
        return new SamlRegisteredServiceMetadataHealthIndicator(samlRegisteredServiceMetadataResolvers(),
            servicesManager.getIfAvailable());
    }
}
