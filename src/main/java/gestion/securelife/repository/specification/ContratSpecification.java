package gestion.securelife.repository.specification;

import gestion.securelife.entity.Contrat;
import gestion.securelife.entity.ContratAssuranceAuto;
import gestion.securelife.entity.ContratAssuranceHabitation;
import gestion.securelife.entity.ContratAssuranceVie;
import gestion.securelife.entity.enums.ContratStatus;
import org.springframework.data.jpa.domain.Specification;

public class ContratSpecification {

    public static Specification<Contrat> hasClientNom_complet(String nom_complet) {
        return (root, query, cb) -> {
            if (nom_complet == null || nom_complet.isBlank()) return null;
            return cb.like(cb.lower(root.get("nom_complet")), "%" + nom_complet.toLowerCase() + "%");
        };
    }

    public static Specification<Contrat> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.equal(root.get("email"), email);
        };
    }

    public static Specification<Contrat> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return null;

            switch (type.toUpperCase()) {
                case "AUTO":
                    return cb.equal(root.type(), ContratAssuranceAuto.class);
                case "HABITATION":
                    return cb.equal(root.type(), ContratAssuranceHabitation.class);
                case "VIE":
                    return cb.equal(root.type(), ContratAssuranceVie.class);
                default:
                    return null; // ou cb.disjunction() si tu veux exclure
            }
        };
    }

    public static Specification<Contrat> hasStatus(ContratStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Contrat> primeBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("primeAnnuelle"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("primeAnnuelle"), min);
            return cb.lessThanOrEqualTo(root.get("primeAnnuelle"), max);
        };
    }


}
