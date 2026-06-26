package com.docgen.prefill;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Registry of named transform functions applied to prefilled values.
 *
 * <p><b>Extension point:</b> register additional transforms here (or contribute beans) without
 * touching the prefill engine. Unknown names are treated as identity.
 */
@Component
public class TransformFunctions {

    private final Map<String, Function<String, String>> transforms = Map.ofEntries(
            Map.entry("capitalize", TransformFunctions::capitalize),
            Map.entry("upper", s -> s.toUpperCase(Locale.ROOT)),
            Map.entry("lower", s -> s.toLowerCase(Locale.ROOT)),
            Map.entry("trim", String::trim),
            Map.entry("ssnMask", TransformFunctions::ssnMask),
            Map.entry("digitsOnly", s -> s.replaceAll("\\D", "")),
            Map.entry("titleCase", TransformFunctions::titleCase)
    );

    /** Apply the named transform; identity if name is blank or unknown. */
    public String apply(String name, String value) {
        if (value == null || name == null || name.isBlank()) {
            return value;
        }
        return transforms.getOrDefault(name, Function.identity()).apply(value);
    }

    public boolean isKnown(String name) {
        return name != null && transforms.containsKey(name);
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String titleCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (String word : s.split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(capitalize(word)).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static String ssnMask(String s) {
        String digits = s.replaceAll("\\D", "");
        if (digits.length() != 9) {
            return s;
        }
        return "***-**-" + digits.substring(5);
    }
}
