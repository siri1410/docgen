package com.docgen.user;

import com.docgen.user.Repositories.OrganizationRepository;
import com.docgen.user.Repositories.UserRepository;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the acting organization and user for the current request.
 *
 * <p>When authenticated, the org/user come from the JWT principal. In the open dev profile
 * (no token), this falls back to the seeded {@code default} organization so the demo works.
 */
@Service
public class CurrentTenant {

    private final UserRepository users;
    private final OrganizationRepository organizations;

    public CurrentTenant(UserRepository users, OrganizationRepository organizations) {
        this.users = users;
        this.organizations = organizations;
    }

    public UUID organizationId() {
        UUID userId = currentUserId();
        if (userId != null) {
            return users.findById(userId)
                    .map(User::getOrganizationId)
                    .orElseGet(this::defaultOrgId);
        }
        return defaultOrgId();
    }

    /** Current user id (subject of the JWT), or {@code null} in the open dev profile. */
    public UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String currentActor() {
        UUID id = currentUserId();
        return id != null ? id.toString() : "system";
    }

    private UUID defaultOrgId() {
        return organizations.findBySlug("default")
                .map(Organization::getId)
                .orElseThrow(() -> new IllegalStateException("Default organization not seeded"));
    }
}
