package org.apereo.cas.support.events.couchdb;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbEventsConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.events.EventCouchDbRepository;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;

import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;

/**
 * This is {@link CouchDbCasEventRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(classes = {
    CasCouchDbCoreConfiguration.class,
    CouchDbEventsConfiguration.class,
    RefreshAutoConfiguration.class
    },
    properties = {
        "cas.events.couchDb.asynchronous=false",
        "cas.events.couchDb.username=cas",
        "cas.events.couchdb.password=password"
    })
@Getter
public class CouchDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Autowired
    @Qualifier("couchDbEventRepository")
    private EventCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
    
    @Autowired
    @Qualifier("eventCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

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
