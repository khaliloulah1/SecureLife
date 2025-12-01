package gestion.securelife.util;

import java.time.LocalDateTime;

public final class DateUtils {

    private DateUtils() {}

    /**
     * Retourne la date/heure actuelle (serveur)
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Formatage générique si tu veux afficher les dates dans les réponses
     */
    public static String format(LocalDateTime date) {
        return date != null ? date.toString() : null;
    }
}
