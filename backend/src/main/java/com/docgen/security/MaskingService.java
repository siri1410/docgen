package com.docgen.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Field-level masking for sensitive values (SSN, Member ID, etc.). Callers holding the
 * {@code DATA_UNMASK} role receive raw values; everyone else receives a masked form.
 */
@Service
public class MaskingService {

    private static final String UNMASK_ROLE = "ROLE_DATA_UNMASK";

    /** Mask a value, keeping only the last {@code visibleSuffix} characters. */
    public String mask(String value) {
        return mask(value, 4);
    }

    public String mask(String value, int visibleSuffix) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String digitsOnly = value.replaceAll("\\s", "");
        if (digitsOnly.length() <= visibleSuffix) {
            return "*".repeat(digitsOnly.length());
        }
        int maskedLen = digitsOnly.length() - visibleSuffix;
        return "*".repeat(maskedLen) + digitsOnly.substring(maskedLen);
    }

    /** Apply masking only when the current principal lacks the unmask role. */
    public String maskUnlessAuthorized(String value) {
        return canUnmask() ? value : mask(value);
    }

    public boolean canUnmask() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> UNMASK_ROLE.equals(a.getAuthority()));
    }
}
