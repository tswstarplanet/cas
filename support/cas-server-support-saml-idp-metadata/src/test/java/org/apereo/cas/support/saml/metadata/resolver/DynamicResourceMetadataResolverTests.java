package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import lombok.Getter;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This is {@link DynamicResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */

@Getter
@Category(FileSystemCategory.class)
public class DynamicResourceMetadataResolverTests extends BaseSamlIdPServicesTests {

    @Autowired
    @Qualifier("dynamicResourceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    @Override
    public String getMetadataLocation() {
        return "http://mdq-beta.incommon.org/global/entities/{0}";
    }
}
