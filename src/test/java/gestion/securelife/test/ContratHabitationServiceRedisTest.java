package gestion.securelife.test;

import gestion.securelife.dto.request.ContratHabitationRequest;
import gestion.securelife.service.interf.ContratHabitationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ContratHabitationServiceRedisTest {

    @Autowired
    private ContratHabitationService contratHabitationService;

    @Test
    void testCacheGetContratById() {
        ContratHabitationRequest request = new ContratHabitationRequest();
        request.setEmail("habitation@example.com");
        request.setPrimeBase(1000.0);
        request.setSuperficie(50.0);
        request.setZoneRisque("dakar");

        var created = contratHabitationService.createContrat(request);

        // Premier appel : va dans la BDD
        var fromDb = contratHabitationService.getContratById(String.valueOf(created.getId()));

        // Deuxième appel : devrait venir du cache
        var fromCache = contratHabitationService.getContratById(String.valueOf(created.getId()));

        Assertions.assertEquals(fromDb.getId(), fromCache.getId());
    }

    @Test
    void testCacheEvictAfterUpdate() {
        ContratHabitationRequest request = new ContratHabitationRequest();
        request.setEmail("update-hab@example.com");
        request.setPrimeBase(1000.0);
        request.setSuperficie(50.0);
        request.setZoneRisque("dakar");

        var created = contratHabitationService.createContrat(request);

        // Mise à jour du contrat
        request.setSuperficie(100.0);
        contratHabitationService.updateContrat(String.valueOf(created.getId()), request);

        // Cache doit être vidé : get recharge depuis BDD
        var updated = contratHabitationService.getContratById(String.valueOf(created.getId()));
        Assertions.assertEquals(100.0, updated.getSuperficie());
    }
}
