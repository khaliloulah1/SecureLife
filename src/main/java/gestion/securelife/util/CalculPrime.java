package gestion.securelife.util;

public final class CalculPrime {


    /**
     * Calcul de la prime annuelle pour un contrat Auto
     */
    public static double auto(double primeBase, double bonusMalus, int puissanceFiscale) {
        return primeBase * (bonusMalus / 100.0) * puissanceFiscale;
    }

    /**
     * Calcul de la prime annuelle pour un contrat Habitation
     */
    public static double habitation(double primeBase, double superficie, String zoneRisque) {

        double zoneFactor = switch (zoneRisque.toUpperCase()) {
            case "HAUT" -> 1.5;
            case "MOYEN" -> 1.2;
            default -> 1.0; // BAS ou inconnu
        };

        return primeBase * superficie * zoneFactor;
    }

    /**
     * Calcul de la prime annuelle pour un contrat Vie
     */
    public static double vie(double primeBase, double capitalGaranti, int ageAssure) {

        double ageFactor = ageAssure < 40 ? 1.0 : 1.5;

        return primeBase * (capitalGaranti / 10000.0) * ageFactor;
    }
}
