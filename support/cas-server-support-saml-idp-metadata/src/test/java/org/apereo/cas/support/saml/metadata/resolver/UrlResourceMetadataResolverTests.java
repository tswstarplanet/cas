package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import lombok.Getter;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link UrlResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */

@Getter
@Category(FileSystemCategory.class)
@TestPropertySource(properties = {"cas.authn.samlIdp.metadata.location=file:/tmp"})
public class UrlResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Autowired
    @Qualifier("urlResourceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    @Override
    public String getMetadataLocation() {
        return "http://www.testshib.org/metadata/testshib-providers.xml";
    }
}
