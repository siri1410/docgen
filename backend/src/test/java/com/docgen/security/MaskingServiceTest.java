package com.docgen.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

class MaskingServiceTest {

    private final MaskingService masking = new MaskingService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void masksAllButLastFour() {
        assertThat(masking.mask("123-45-6789")).isEqualTo("*******6789");
        assertThat(masking.mask("MPI12345")).isEqualTo("****2345");
    }

    @Test
    void maskedWhenNoUnmaskRole() {
        assertThat(masking.maskUnlessAuthorized("123456789")).isEqualTo("*****6789");
    }

    @Test
    void rawWhenUnmaskRolePresent() {
        var auth = new UsernamePasswordAuthenticationToken("u", null,
                List.of(new SimpleGrantedAuthority("ROLE_DATA_UNMASK")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertThat(masking.maskUnlessAuthorized("123456789")).isEqualTo("123456789");
    }
}
