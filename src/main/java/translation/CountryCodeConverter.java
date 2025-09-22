package translation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CountryCodeConverter {

    private final Map<String, String> countryCodeToCountry = new HashMap<>();
    private final Map<String, String> countryToCountryCode = new HashMap<>();

    public CountryCodeConverter() {
        this("country-codes.txt");
    }

    public CountryCodeConverter(String filename) {
        // Try root and package-relative locations on the classpath.
        String[] candidates = { "/" + filename, "/translation/" + filename };
        InputStream in = null;
        String chosen = null;
        for (String cand : candidates) {
            in = CountryCodeConverter.class.getResourceAsStream(cand);
            if (in != null) { chosen = cand; break; }
        }
        if (in == null) {
            throw new RuntimeException("Resource not found on classpath. Tried: " + Arrays.toString(candidates) +
                    "\nPlace " + filename + " under src/main/resources/ (or resources/translation/).");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            // ---- Parse header to find indices ----
            String header = br.readLine();
            if (header == null) throw new RuntimeException("Empty " + filename);
            header = header.replace("\uFEFF", ""); // strip BOM if present
            String delim = header.contains("\t") ? "\t" : ",";

            String[] cols = header.split(delim, -1);
            int countryIdx = -1, alpha3Idx = -1;
            for (int i = 0; i < cols.length; i++) {
                String h = cols[i].trim().toLowerCase();
                if (h.equals("country") || h.equals("name")) countryIdx = i;
                if (h.startsWith("alpha-3")) alpha3Idx = i;
            }
            if (countryIdx < 0 || alpha3Idx < 0) {
                throw new RuntimeException("Header must contain 'Country' and 'Alpha-3 code' columns. Got: " + Arrays.toString(cols));
            }

            // ---- Read rows ----
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(delim, -1);
                if (parts.length <= Math.max(countryIdx, alpha3Idx)) continue;

                String name = parts[countryIdx].trim();
                String code = parts[alpha3Idx].replace("\uFEFF", "").trim().toLowerCase();

                if (!name.isEmpty() && !code.isEmpty()) {
                    countryCodeToCountry.put(code, name);
                    countryToCountryCode.put(name.toLowerCase(), code);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load/parse resource: " + chosen, e);
        }

        if (countryCodeToCountry.isEmpty()) {
            throw new RuntimeException("Parsed 0 countries from resource; check file format and location.");
        }
    }

    /** Return the name of the country for the given 3-letter code. */
    public String fromCountryCode(String code) {
        if (code == null) return null;
        return countryCodeToCountry.get(code.trim().toLowerCase());
    }

    /** Return the 3-letter code for the given country name. */
    public String fromCountry(String country) {
        if (country == null) return null;
        return countryToCountryCode.get(country.trim().toLowerCase());
    }

    /** Return how many countries are included. */
    public int getNumCountries() {
        return countryCodeToCountry.size();
    }
}
