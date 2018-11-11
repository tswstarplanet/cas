package org.apereo.cas.aup;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasAcceptableUsagePolicyCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;

import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CouchDbAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Import({CasCouchDbCoreConfiguration.class, CasAcceptableUsagePolicyCouchDbConfiguration.class})
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.couchDb.asynchronous=false",
    "cas.acceptableUsagePolicy.couchDb.username=cas",
    "cas.acceptableUsagePolicy.couchdb.password=password"

})
@Category(CouchDbCategory.class)
@Getter
public class CouchDbAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    @Autowired
    @Qualifier("aupCouchDbFactory")
    private CouchDbConnectorFactory aupCouchDbFactory;

    @Autowired
    @Qualifier("aupCouchDbRepository")
    private ProfileCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Before
    public void setUp() {
        aupCouchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(aupCouchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @After
    public void tearDown() {
        aupCouchDbFactory.getCouchDbInstance().deleteDatabase(aupCouchDbFactory.getCouchDbConnector().getDatabaseName());
    }

}
