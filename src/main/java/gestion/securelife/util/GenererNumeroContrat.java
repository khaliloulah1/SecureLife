package gestion.securelife.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GenererNumeroContrat {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String generate(String type) {


        return "Secure-" + type.toUpperCase() + "-" + LocalDateTime.now().format(FORMAT);
    }
}
