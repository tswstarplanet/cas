package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FCouchDbConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.u2f.U2FDeviceRegistrationCouchDbRepository;

import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * This is {@link U2FCouchDbDeviceRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(classes = {
    U2FCouchDbConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    AopAutoConfiguration.class,
    U2FConfiguration.class,
    RefreshAutoConfiguration.class},
    properties = {
        "cas.authn.mfa.u2f.couchDb.asynchronous=false",
        "cas.authn.mfa.u2f.couchDb.username=cas",
        "cas.authn.mfa.u2f.couchdb.password=password"
    })
@Getter
public class U2FCouchDbDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("u2fCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FCouchDbDeviceRepository deviceRepository;

    @Autowired
    @Qualifier("couchDbU2fDeviceRegistrationRepository")
    private U2FDeviceRegistrationCouchDbRepository couchDbRepository;

    @Before
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @After
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}
