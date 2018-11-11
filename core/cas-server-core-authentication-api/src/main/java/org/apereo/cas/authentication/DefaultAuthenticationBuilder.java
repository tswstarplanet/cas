package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Constructs immutable {@link Authentication} objects using the builder pattern.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
public class DefaultAuthenticationBuilder implements AuthenticationBuilder {

    private static final long serialVersionUID = -8504842011648432398L;
    /**
     * Credential metadata.
     */
    private final List<CredentialMetaData> credentials = new ArrayList<>();
    /**
     * Authentication metadata attributes.
     */
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    /**
     * Map of handler names to authentication successes.
     */
    private final Map<String, AuthenticationHandlerExecutionResult> successes = new LinkedHashMap<>();
    /**
     * Map of handler names to authentication failures.
     */
    private final Map<String, Throwable> failures = new LinkedHashMap<>();
    /**
     * Authenticated principal.
     */
    private Principal principal;
    /**
     * Authentication date.
     */
    private ZonedDateTime authenticationDate;

    /**
     * Creates a new instance using the current date for the authentication date.
     */
    public DefaultAuthenticationBuilder() {
        this.authenticationDate = ZonedDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Creates a new instance using the current date for the authentication date and the given
     * principal for the authenticated principal.
     *
     * @param p Authenticated principal.
     */
    public DefaultAuthenticationBuilder(final Principal p) {
        this();
        this.principal = p;
    }

    /**
     * Creates a new builder initialized with data from the given authentication source.
     *
     * @param source Authentication source.
     * @return New builder instance initialized with all fields in the given authentication source.
     */
    public static AuthenticationBuilder newInstance(final Authentication source) {
        val builder = new DefaultAuthenticationBuilder(source.getPrincipal());
        builder.setAuthenticationDate(source.getAuthenticationDate());
        builder.setCredentials(source.getCredentials());
        builder.setSuccesses(source.getSuccesses());
        builder.setFailures(source.getFailures());
        builder.setAttributes(source.getAttributes());
        return builder;
    }

    /**
     * Creates a new builder.
     *
     * @return New builder instance
     */
    public static AuthenticationBuilder newInstance() {
        return new DefaultAuthenticationBuilder();
    }

    @Override
    public AuthenticationBuilder setAuthenticationDate(final ZonedDateTime d) {
        if (d != null) {
            this.authenticationDate = d;
        }
        return this;
    }

    @Override
    public AuthenticationBuilder addCredentials(final List<CredentialMetaData> credentials) {
        this.credentials.addAll(credentials);
        return this;
    }

    @Override
    public AuthenticationBuilder setPrincipal(final Principal p) {
        this.principal = p;
        return this;
    }

    /**
     * Sets the list of metadata about credentials presented for authentication.
     *
     * @param credentials Non-null list of credential metadata.
     * @return This builder instance.
     */
    public AuthenticationBuilder setCredentials(final @NonNull List<CredentialMetaData> credentials) {
        this.credentials.clear();
        this.credentials.addAll(credentials);
        return this;
    }

    @Override
    public AuthenticationBuilder addCredential(final CredentialMetaData credential) {
        this.credentials.add(credential);
        return this;
    }

    @Override
    public AuthenticationBuilder setAttributes(final Map<String, Object> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public AuthenticationBuilder mergeAttribute(final String key, final Object value) {
        val currentValue = this.attributes.get(key);
        if (currentValue == null) {
            return addAttribute(key, value);
        }
        val collection = CollectionUtils.toCollection(currentValue);
        collection.addAll(CollectionUtils.toCollection(value));
        return addAttribute(key, collection);
    }

    @Override
    public boolean hasAttribute(final String name, final Predicate<Object> predicate) {
        if (this.attributes.containsKey(name)) {
            val value = this.attributes.get(name);
            val valueCol = CollectionUtils.toCollection(value);
            return valueCol.stream().anyMatch(predicate);
        }
        return false;
    }

    @Override
    public AuthenticationBuilder addAttribute(final String key, final Object value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public AuthenticationBuilder setSuccesses(final @NonNull Map<String, AuthenticationHandlerExecutionResult> successes) {
        this.successes.clear();
        return addSuccesses(successes);
    }

    @Override
    public AuthenticationBuilder addSuccesses(final Map<String, AuthenticationHandlerExecutionResult> successes) {
        successes.forEach(this::addSuccess);
        return this;
    }

    @Override
    public AuthenticationBuilder addSuccess(final String key, final AuthenticationHandlerExecutionResult value) {
        LOGGER.trace("Recording authentication handler result success under key [{}]", key);
        if (this.successes.containsKey(key)) {
            LOGGER.trace("Key mapped to authentication handler result [{}] is already recorded in the list of successful attempts. Overriding...", key);
        }
        this.successes.put(key, value);
        return this;
    }


    @Override
    public AuthenticationBuilder setFailures(final @NonNull Map<String, Throwable> failures) {
        this.failures.clear();
        return addFailures(failures);
    }

    @Override
    public AuthenticationBuilder addFailures(final Map<String, Throwable> failures) {
        failures.forEach(this::addFailure);
        return this;
    }

    @Override
    public AuthenticationBuilder addFailure(final String key, final Throwable value) {
        LOGGER.trace("Recording authentication handler failure under key [{}]", key);
        if (this.successes.containsKey(key)) {
            val newKey = key + System.currentTimeMillis();
            LOGGER.trace("Key mapped to authentication handler failure [{}] is recorded in the list of failed attempts. Overriding with [{}]", key, newKey);
            this.failures.put(newKey, value);
        } else {
            this.failures.put(key, value);
        }
        return this;
    }

    @Override
    public Authentication build() {
        return new DefaultAuthentication(this.authenticationDate, this.credentials, this.principal, this.attributes, this.successes, this.failures);
    }
}
