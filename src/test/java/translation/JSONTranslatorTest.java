package translation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTranslatorTest {

    JSONTranslator jsonTranslator = new JSONTranslator();

    @Test
    public void getLanguageCodes() {
        List<String> countryLanguages = jsonTranslator.getLanguageCodes();
        assertEquals(35, countryLanguages.size(),
                "There should be 35 languages but got " + countryLanguages.size());
    }

    @Test
    public void getCountryCodes() {
        List<String> languages = jsonTranslator.getCountryCodes();
        assertEquals(193, languages.size(),
                "There should be 193 countries but got " + languages.size());

    }

    @Test
    public void translate() {
        assertEquals("Canada", jsonTranslator.translate("can", "en"));
    }
}