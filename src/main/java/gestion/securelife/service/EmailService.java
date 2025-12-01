package gestion.securelife.service;

import gestion.securelife.dto.response.*;

import gestion.securelife.entity.Contrat;
import gestion.securelife.exception.ResourceNotFoundException;
import gestion.securelife.repository.ContratAssuranceVieRepository;
import gestion.securelife.repository.ContratRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ContratRepository  contratRepository;

    // -------------------------------
    // ENVOI EMAIL CREATION CONTRAT AUTO
    // -------------------------------
    public void sendContractCreatedEmail(ContratAutoResponse contrat) {
        try {
            Context context = new Context();

            // variables communes
            mapBaseVariables(context, contrat);

            // variables spécifiques
            context.setVariable("immatriculation", contrat.getImmatriculation());
            context.setVariable("puissanceFiscale", contrat.getPuissanceFiscale());
            context.setVariable("bonusMalus", contrat.getBonusMalus());

            sendEmail(
                    contrat.getEmail(),
                    "Votre contrat AUTO a été créé",
                    "creation",
                    context
            );

        } catch (Exception e) {
            log.error("Erreur envoi email création contrat auto : {}", e.getMessage());
        }
    }


    // -------------------------------
    // ENVOI EMAIL CREATION CONTRAT HABITATION
    // -------------------------------
    public void sendContractCreatedEmail(ContratHabitationResponse contrat) {
        try {
            Context context = new Context();

            mapBaseVariables(context, contrat);

            context.setVariable("adresse", contrat.getAdresse());
            context.setVariable("superficie", contrat.getSuperficie());
            context.setVariable("zoneRisque", contrat.getZoneRisque());

            sendEmail(
                    contrat.getEmail(),
                    "Votre contrat HABITATION a été créé",
                    "creation",
                    context
            );

        } catch (Exception e) {
            log.error("Erreur envoi email création contrat habitation : {}", e.getMessage());
        }
    }

    // -------------------------------
    // ENVOI EMAIL CREATION CONTRAT VIE
    // -------------------------------
    public void sendContractCreatedEmail(ContratVieResponse contrat) {
        try {
            Context context = new Context();

            mapBaseVariables(context, contrat);

            context.setVariable("ageAssure", contrat.getAgeAssure());
            context.setVariable("capitalGaranti", contrat.getCapitalGaranti());
            context.setVariable("beneficiaire", contrat.getBeneficiaire());

            sendEmail(
                    contrat.getEmail(),
                    "Votre contrat VIE a été créé",
                    "creation",
                    context
            );

        } catch (Exception e) {
            log.error("Erreur envoi email création contrat vie : {}", e.getMessage());
        }
    }

    // =======================================================================================
    //                            MODIFICATION DES CONTRATS
    // =======================================================================================

    public void sendContractUpdatedEmail(ContratAutoResponse contrat) {
        try {
            Context context = new Context();
            mapBaseVariables(context, contrat);

            context.setVariable("immatriculation", contrat.getImmatriculation());
            context.setVariable("puissanceFiscale", contrat.getPuissanceFiscale());
            context.setVariable("bonusMalus", contrat.getBonusMalus());

            sendEmail(
                    contrat.getEmail(),
                    "Votre contrat AUTO a été modifié",
                    "modification",
                    context
            );
        } catch (Exception e) {
            log.error("Erreur modification contrat auto : {}", e.getMessage());
        }
    }

    public void sendContractUpdatedEmail(ContratHabitationResponse contrat) {
        try {
            Context context = new Context();
            mapBaseVariables(context, contrat);

            context.setVariable("adresse", contrat.getAdresse());
            context.setVariable("superficie", contrat.getSuperficie());
            context.setVariable("zoneRisque", contrat.getZoneRisque());

            sendEmail(
                    contrat.getEmail(),
                    "Votre contrat HABITATION a été modifié",
                    "modification",
                    context
            );
        } catch (Exception e) {
            log.error("Erreur modification contrat habitation : {}", e.getMessage());
        }
    }

    public void sendContractUpdatedEmail(ContratVieResponse contrat) {
        try {
            Context context = new Context();
            mapBaseVariables(context, contrat);

            context.setVariable("ageAssure", contrat.getAgeAssure());
            context.setVariable("capitalGaranti", contrat.getCapitalGaranti());
            context.setVariable("beneficiaire", contrat.getBeneficiaire());

            sendEmail(
                    contrat.getEmail(),
                    "Votre contrat VIE a été modifié",
                    "modification",
                    context
            );
        } catch (Exception e) {
            log.error("Erreur modification contrat vie : {}", e.getMessage());
        }
    }


    // Méthode générique pour envoyer un email pour tous les types de contrats
    public void sendContractStatusChangedEmail(ContratResponse contrat) {
        try {
            Context context = new Context();
            context.setVariable("clientNom", contrat.getNom_complet());
            context.setVariable("contratId", contrat.getId());
            context.setVariable("status", contrat.getStatus());

            sendEmail(contrat.getEmail(),
                    "Changement de statut de votre contrat",
                    "status",  // Template Thymeleaf
                    context);

        } catch (Exception e) {
            log.error("Erreur envoi email changement statut contrat : {}", e.getMessage());
        }
    }

    // -------------------------------
    // ENVOI EMAIL CONFIRMATION DOCUMENT UPLOADED
    // -------------------------------
    public void sendDocumentUploadedEmail(DocumentResponse documentResponse) {
        try {
            // Récupérer le contrat à partir de l'ID du contrat
            Long contratId = Long.valueOf(documentResponse.getContratId());
            Contrat contrat = contratRepository.findById(String.valueOf(contratId))
                    .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));

            // Préparer les variables pour le template Thymeleaf
            Context context = new Context();
            context.setVariable("nomClient", contrat.getNom_complet());  // Nom du client
            context.setVariable("numeroContrat", contrat.getNumeroContrat());  // Numéro du contrat
            context.setVariable("fileName", documentResponse.getFileName());  // Nom du fichier
            context.setVariable("documentType", documentResponse.getDocumentType());  // Type du document
            context.setVariable("year", LocalDate.now().getYear());  // Année actuelle pour le footer

            // Appeler la méthode générique pour envoyer l'email
            sendEmail(
                    contrat.getEmail(),  // Destinataire (email du contrat)
                    "Document reçu - SecureLife", // Sujet
                    "document_uploaded",  // Nom du template Thymeleaf
                    context
            );

            log.info("Email de confirmation envoyé à {}", contrat.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation de document uploadé : {}", e.getMessage());
        }
    }



    //
    // Méthode générique pour envoyer un email
void sendEmail(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {} : {}", to, e.getMessage());
        }
    }



    private void mapBaseVariables(Context context, ContratResponse contrat) {
        context.setVariable("id", contrat.getId());
        context.setVariable("numeroContrat", contrat.getNumeroContrat());
        context.setVariable("nomComplet", contrat.getNom_complet());
        context.setVariable("email", contrat.getEmail());
        context.setVariable("primeBase", contrat.getPrimeBase());
        context.setVariable("primeAnnuelle", contrat.getPrimeAnnuelle());
        context.setVariable("status", contrat.getStatus());
        context.setVariable("createdAt", contrat.getCreatedAt());
        context.setVariable("updatedAt", contrat.getUpdatedAt());
        context.setVariable("typeAssurance", contrat.getTypeAssurance());
    }


}
