package com.docgen.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositories for the user/organization aggregate.
 */
public final class Repositories {

    private Repositories() {}

    public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByEmail(String email);

        boolean existsByEmail(String email);
    }

    public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
        Optional<Organization> findBySlug(String slug);
    }
}
