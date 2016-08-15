package org.mercycorps.translationcards.txcmaker.service;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LanguageServiceTest {

    private LanguageService languageService;
    private LanguagesImportUtility languagesImportUtility;

    @Before
    public void setup() throws IOException {
        languagesImportUtility = mock(LanguagesImportUtility.class);
        Map<String, List<String>> languageMap = new HashMap<>();
        languageMap.put("fa", Arrays.asList("Persian", "Farsi"));
        languageMap.put("en", Collections.singletonList("English"));
        languageMap.put("ar", Collections.singletonList("Arabic"));
        when(languagesImportUtility.getLanguageMap()).thenReturn(languageMap);
        languageService = new LanguageService(languagesImportUtility);
    }

    @Test
    public void shouldReturnCorrectIsoCodeForArabic() throws Exception {
        assertEquals("fa", languageService.getIsoForLanguage("Persian"));
    }

    @Test
    public void shouldReturnCorrectIsoCodeForFarsi() throws Exception {
        assertEquals("fa", languageService.getIsoForLanguage("Farsi"));
    }

    @Test
    public void shouldReturnCorrectIsoCodeForUncapitalizedLanguageName() throws Exception {
        assertEquals( "fa", languageService.getIsoForLanguage("farsi"));
    }

    @Test
    public void shouldReturnInvalidIsoCodeForEmptyLanguageName() throws Exception {
        assertEquals(LanguageService.INVALID_ISO_CODE, languageService.getIsoForLanguage(""));
    }

    @Test
    public void shouldReturnInvalidIsoCodeForNullLanguageName() throws Exception {
        assertEquals(LanguageService.INVALID_ISO_CODE, languageService.getIsoForLanguage(null));
    }

    @Test
    public void shouldReturnInvalidLanguageNameForNullIsoCode() throws Exception {
        assertEquals(LanguageService.INVALID_LANGUAGE_NAME, languageService.getLanguageDisplayName(null));
    }

    @Test
    public void shouldReturnListOfAllLanguageNames() {
        List<String> languageNames = languageService.getLanguageNames();

        assertEquals(true, languageNames.contains("Farsi"));
        assertEquals(true, languageNames.contains("Arabic"));
        assertEquals(true, languageNames.contains("English"));
        assertEquals(true, languageNames.contains("Persian"));
    }

    @Test
    public void shouldReturnATitleCasedString() {
        String titleCasedString = LanguageService.getTitleCaseName("A title cased string");
        assertEquals("A Title Cased String", titleCasedString);
    }

    @Test
    public void shouldReturnEmptyStringIfNullPassedToGetTitleCaseName() {
        assertEquals("", LanguageService.getTitleCaseName(null));
    }

    @Test
    public void shouldReturnTitleCasedStringWhenSpaceAtFront() {
        String titleCasedString = LanguageService.getTitleCaseName(" A title cased string");
        assertEquals("A Title Cased String", titleCasedString);
    }
}