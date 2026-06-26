package com.docgen.prefill;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransformFunctionsTest {

    private final TransformFunctions transforms = new TransformFunctions();

    @Test
    void appliesNamedTransforms() {
        assertThat(transforms.apply("capitalize", "john")).isEqualTo("John");
        assertThat(transforms.apply("upper", "nc")).isEqualTo("NC");
        assertThat(transforms.apply("lower", "NC")).isEqualTo("nc");
        assertThat(transforms.apply("trim", "  x  ")).isEqualTo("x");
        assertThat(transforms.apply("titleCase", "new york city")).isEqualTo("New York City");
        assertThat(transforms.apply("digitsOnly", "123-45-6789")).isEqualTo("123456789");
        assertThat(transforms.apply("ssnMask", "123456789")).isEqualTo("***-**-6789");
    }

    @Test
    void unknownOrBlankTransformIsIdentity() {
        assertThat(transforms.apply("nope", "value")).isEqualTo("value");
        assertThat(transforms.apply(null, "value")).isEqualTo("value");
        assertThat(transforms.apply("upper", null)).isNull();
    }
}
