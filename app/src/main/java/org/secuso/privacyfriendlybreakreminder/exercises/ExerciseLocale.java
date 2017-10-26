package org.secuso.privacyfriendlybreakreminder.exercises;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

/**
 * This class saves the available languages for the exercises.
 * @author Christopher Beckmann
 * @version 2.0
 */
public final class ExerciseLocale {

    private ExerciseLocale() {}

    private static final HashSet<String> AVAILABLE_LOCALE = new HashSet<>();

    static {
        AVAILABLE_LOCALE.addAll(
                Arrays.asList(
                        "en", "de"
                )
        );
    };

    /**
     * @return the available language. If the default language of the device is not available. {@code "en"} will be returned.
     */
    public static String getLocale() {
        String locale = Locale.getDefault().getLanguage();
        return AVAILABLE_LOCALE.contains(locale) ? locale : "en";
    }
}
