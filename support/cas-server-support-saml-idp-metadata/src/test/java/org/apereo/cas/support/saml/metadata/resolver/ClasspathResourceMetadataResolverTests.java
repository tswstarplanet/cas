package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This is {@link ClasspathResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class ClasspathResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Autowired
    @Qualifier("classpathResourceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    @Override
    public String getMetadataLocation() {
        return "classpath:sample-sp.xml";
    }
}
