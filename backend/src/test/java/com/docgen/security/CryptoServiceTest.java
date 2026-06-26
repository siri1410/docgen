package com.docgen.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CryptoServiceTest {

    private final CryptoService crypto =
            new CryptoService("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");

    @Test
    void encryptThenDecryptRoundTrips() {
        String secret = "{\"token\":\"super-secret-123\"}";
        String encrypted = crypto.encrypt(secret);

        assertThat(encrypted).startsWith("enc:").doesNotContain("super-secret-123");
        assertThat(crypto.decrypt(encrypted)).isEqualTo(secret);
    }

    @Test
    void encryptionIsNonDeterministic() {
        String a = crypto.encrypt("same");
        String b = crypto.encrypt("same");
        assertThat(a).isNotEqualTo(b); // random IV per call
        assertThat(crypto.decrypt(a)).isEqualTo(crypto.decrypt(b)).isEqualTo("same");
    }

    @Test
    void decryptToleratesPlaintext() {
        assertThat(crypto.decrypt("not-encrypted")).isEqualTo("not-encrypted");
        assertThat(crypto.encrypt(null)).isNull();
        assertThat(crypto.decrypt(null)).isNull();
    }
}
