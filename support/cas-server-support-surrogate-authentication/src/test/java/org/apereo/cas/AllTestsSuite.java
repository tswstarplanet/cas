
package org.apereo.cas;

import org.apereo.cas.authentication.SurrogateAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.authentication.SurrogateAuthenticationPostProcessorTests;
import org.apereo.cas.authentication.SurrogatePrincipalElectionStrategyTests;
import org.apereo.cas.authentication.SurrogatePrincipalResolverTests;
import org.apereo.cas.authentication.audit.SurrogateAuditPrincipalIdProviderTests;
import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationServiceTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Auto-generated by Gradle Build
 * @since 6.0.0-RC3
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SurrogateAuthenticationPostProcessorTests.class,
    SurrogateAuthenticationMetaDataPopulatorTests.class,
    SurrogatePrincipalResolverTests.class,
    SurrogateAuditPrincipalIdProviderTests.class,
    SimpleSurrogateAuthenticationServiceTests.class,
    JsonResourceSurrogateAuthenticationServiceTests.class,
    SurrogatePrincipalElectionStrategyTests.class
})
public class AllTestsSuite {
}
