package org.apereo.cas.ws.idp.authentication;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.Ordered;

import java.util.Optional;

/**
 * This is {@link WSFederationAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
public class WSFederationAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = 8035218407906419228L;

    private int order = Ordered.HIGHEST_PRECEDENCE;
    private final transient ServiceFactory webApplicationServiceFactory;

    public WSFederationAuthenticationServiceSelectionStrategy(final ServiceFactory webApplicationServiceFactory) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    private static Optional<NameValuePair> getRealmAsParameter(final Service service) {
        try {
            val builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(WSFederationConstants.WTREALM))
                .findFirst();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private static Optional<NameValuePair> getReplyAsParameter(final Service service) {
        try {
            if (service == null) {
                return Optional.empty();
            }

            val builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(WSFederationConstants.WREPLY))
                .findFirst();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val replyParamRes = getReplyAsParameter(service);
        if (replyParamRes.isPresent()) {
            val serviceReply = replyParamRes.get().getValue();
            LOGGER.debug("Located service id [{}] from service authentication request at [{}]", serviceReply, service.getId());
            return this.webApplicationServiceFactory.createService(serviceReply);
        }
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        return service != null && getRealmAsParameter(service).isPresent() && getReplyAsParameter(service).isPresent();
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
