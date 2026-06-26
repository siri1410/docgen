package com.docgen.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Local, in-process stand-ins for the external systems a connector would call in production
 * (a member directory, a state DMV record service). The seeded demo connectors point at these
 * endpoints so prefill works fully offline — no dependency on a reachable third-party API.
 *
 * <p>In a real deployment you would repoint the connector {@code baseUrl} at the agency's API and
 * delete this controller; the prefill engine, mappings and JSONPath sources stay identical because
 * these responses mirror the shapes the seeded mappings expect ({@code $.member.*}, {@code $.record.*}).
 */
@RestController
@RequestMapping("/api/mock")
@Tag(name = "Mock Directory", description = "Local stand-in data sources for demo connectors")
public class MockDirectoryController {

    /** Deterministic pick from a list based on the lookup key, so the same id always returns the same record. */
    private static <T> T pick(String key, T[] options) {
        int h = Math.floorMod(key == null ? 0 : key.hashCode(), options.length);
        return options[h];
    }

    private static final String[] FIRST = {"Jordan", "Maria", "Andre", "Priya", "Liam"};
    private static final String[] LAST = {"Carter", "Nguyen", "Robinson", "Patel", "Brooks"};
    private static final String[] CITY = {"Raleigh", "Charlotte", "Durham", "Asheville", "Greensboro"};

    /** Member directory record. Mirrors the {@code $.member.*} mappings on the Patient Intake form. */
    @Operation(summary = "Look up a member directory record by member id")
    @GetMapping("/members/{memberId}")
    public Map<String, Object> member(@PathVariable String memberId) {
        String first = pick(memberId, FIRST);
        String last = pick(memberId + "x", LAST);
        String city = pick(memberId + "c", CITY);
        return Map.of(
                "member", Map.of(
                        "firstName", first,
                        "lastName", last,
                        "ssn", "123-45-6789",
                        "mpi", "MPI-" + Math.floorMod(memberId.hashCode(), 100000),
                        "address", Map.of(
                                "line1", (100 + Math.floorMod(memberId.hashCode(), 900)) + " Main St",
                                "city", city,
                                "state", "NC",
                                "zip", "276" + String.format("%02d", Math.floorMod(memberId.hashCode(), 100)))));
    }

    /** State DMV record. Echoes the licence number under {@code $.args.dl} and returns {@code $.record.*}. */
    @Operation(summary = "Look up a state DMV driver record by licence number")
    @GetMapping("/dmv/{dlNumber}")
    public Map<String, Object> dmv(@PathVariable String dlNumber) {
        String first = pick(dlNumber, FIRST);
        String last = pick(dlNumber + "x", LAST);
        String city = pick(dlNumber + "c", CITY);
        String dlClass = pick(dlNumber + "k", new String[] {"C", "C", "M", "A"});
        return Map.of(
                // Echoes the input back, proving the connector substituted {{dlNumber}} into a real call.
                "args", Map.of("dl", dlNumber),
                "record", Map.of(
                        "firstName", first,
                        "lastName", last,
                        "dob", "1990-04-12",
                        "licenseClass", dlClass,
                        "address", (100 + Math.floorMod(dlNumber.hashCode(), 900)) + " Evergreen Terrace, " + city + ", NC 27601",
                        "expirationDate", "2026-08-31"));
    }
}
