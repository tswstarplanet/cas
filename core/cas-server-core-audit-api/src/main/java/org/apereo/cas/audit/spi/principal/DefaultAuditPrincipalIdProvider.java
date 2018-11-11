package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link DefaultAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public class DefaultAuditPrincipalIdProvider implements AuditPrincipalIdProvider {
    private int order = Integer.MAX_VALUE;

    @Override
    public String getPrincipalIdFrom(final Authentication authentication, final Object returnValue, final Exception exception) {
        return authentication != null ? authentication.getPrincipal().getId() : null;
    }

    @Override
    public boolean supports(final Authentication authentication, final Object resultValue, final Exception exception) {
        return authentication != null;
    }
}
