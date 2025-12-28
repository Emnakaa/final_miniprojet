package com.planning.service;

import com.planning.dao.ActiviteDAO;
import com.planning.dao.ContrainteDAO;
import com.planning.dao.impl.ActiviteDAOImpl;
import com.planning.dao.impl.ContrainteDAOImpl;
import com.planning.model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConflitService {
    private final ActiviteDAO activiteDAO;
    private final ContrainteDAO contrainteDAO;

    public ConflitService() {
        this.activiteDAO = new ActiviteDAOImpl();
        this.contrainteDAO = new ContrainteDAOImpl();
    }

    /**
     * Détecte tous les conflits pour un utilisateur dans une plage de dates
     */
    public List<Conflit> detecterConflits(int utilisateurId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<Conflit> conflits = new ArrayList<>();
        
        // Charger toutes les activités de l'utilisateur dans la période
        List<Activite> activites = activiteDAO.findByUserAndDateRange(utilisateurId, dateDebut, dateFin);
        
        // Charger les contraintes
        List<ContrainteHoraire> contraintesHoraires = contrainteDAO.findHoraireByUser(utilisateurId);
        List<ContraintePersonnelle> contraintesPersonnelles = contrainteDAO.findPersonnelleByUser(utilisateurId);
        
        // 1. Détecter les chevauchements entre activités
        conflits.addAll(detecterChevauchements(activites));
        
        // 2. Vérifier les violations de contraintes horaires
        conflits.addAll(verifierContraintesHoraires(activites, contraintesHoraires));
        
        // 3. Vérifier les violations de contraintes personnelles
        conflits.addAll(verifierContraintesPersonnelles(activites, contraintesPersonnelles));
        
        return conflits;
    }

    /**
     * Détecte les chevauchements entre activités
     */
    private List<Conflit> detecterChevauchements(List<Activite> activites) {
        List<Conflit> conflits = new ArrayList<>();
        
        for (int i = 0; i < activites.size(); i++) {
            Activite a1 = activites.get(i);
            if (a1.getDebut() == null || a1.getFin() == null) continue;
            
            for (int j = i + 1; j < activites.size(); j++) {
                Activite a2 = activites.get(j);
                if (a2.getDebut() == null || a2.getFin() == null) continue;
                
                // Vérifier si les activités se chevauchent
                if (seChevauchent(a1.getDebut(), a1.getFin(), 
                                  a2.getDebut(), a2.getFin())) {
                    Conflit conflit = new Conflit();
                    conflit.setType("CHEVAUCHEMENT");
                    conflit.setSeverite("CRITIQUE");
                    conflit.setActivite1Id(a1.getId());
                    conflit.setActivite2Id(a2.getId());
                    conflit.setActivite1Titre(a1.getTitre());
                    conflit.setActivite2Titre(a2.getTitre());
                    conflit.setDateDebut(a1.getDebut());
                    conflit.setDateFin(a1.getFin());
                    conflit.setDescription(String.format(
                        "Les activités '%s' et '%s' se chevauchent entre %s et %s",
                        a1.getTitre(), a2.getTitre(),
                        formatDateTime(a1.getDebut()), formatDateTime(a1.getFin())
                    ));
                    conflits.add(conflit);
                }
            }
        }
        
        return conflits;
    }

    /**
     * Vérifie les violations de contraintes horaires récurrentes
     */
    private List<Conflit> verifierContraintesHoraires(List<Activite> activites, 
                                                       List<ContrainteHoraire> contraintes) {
        List<Conflit> conflits = new ArrayList<>();
        
        for (Activite activite : activites) {
            if (activite.getDebut() == null || activite.getFin() == null) continue;
            
            // Extraire le jour de la semaine et les heures
            DayOfWeek jourActivite = activite.getDebut().getDayOfWeek();
            String jourStr = jourActivite.getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase();
            LocalTime heureDebutActivite = activite.getDebut().toLocalTime();
            LocalTime heureFinActivite = activite.getFin().toLocalTime();
            
            // Vérifier chaque contrainte horaire
            for (ContrainteHoraire contrainte : contraintes) {
                if (!contrainte.getJourSemaine().equals(jourStr)) continue;
                if (contrainte.getHeureDebut() == null || contrainte.getHeureFin() == null) continue;
                
                // Si c'est une contrainte INDISPONIBLE et l'activité tombe dedans
                if ("INDISPONIBLE".equals(contrainte.getTypeContrainte())) {
                    if (seChevauchenTime(heureDebutActivite, heureFinActivite,
                                         contrainte.getHeureDebut(), contrainte.getHeureFin())) {
                        Conflit conflit = new Conflit();
                        conflit.setType("CONTRAINTE_HORAIRE");
                        conflit.setSeverite("MAJEURE");
                        conflit.setActivite1Id(activite.getId());
                        conflit.setActivite1Titre(activite.getTitre());
                        conflit.setDateDebut(activite.getDebut());
                        conflit.setDateFin(activite.getFin());
                        conflit.setDescription(String.format(
                            "L'activité '%s' viole une contrainte d'indisponibilité le %s entre %s et %s",
                            activite.getTitre(), jourStr,
                            contrainte.getHeureDebut(), contrainte.getHeureFin()
                        ));
                        conflits.add(conflit);
                    }
                }
            }
        }
        
        return conflits;
    }

    /**
     * Vérifie les violations de contraintes personnelles (périodes bloquées)
     */
    private List<Conflit> verifierContraintesPersonnelles(List<Activite> activites,
                                                           List<ContraintePersonnelle> contraintes) {
        List<Conflit> conflits = new ArrayList<>();
        
        for (Activite activite : activites) {
            if (activite.getDebut() == null || activite.getFin() == null) continue;
            
            for (ContraintePersonnelle contrainte : contraintes) {
                if (contrainte.getDateDebut() == null || contrainte.getDateFin() == null) continue;
                
                // Vérifier si l'activité chevauche la période bloquée
                if (seChevauchent(activite.getDebut(), activite.getFin(),
                                  contrainte.getDateDebut(), contrainte.getDateFin())) {
                    Conflit conflit = new Conflit();
                    conflit.setType("CONTRAINTE_PERSONNELLE");
                    conflit.setSeverite("MAJEURE");
                    conflit.setActivite1Id(activite.getId());
                    conflit.setActivite1Titre(activite.getTitre());
                    conflit.setDateDebut(activite.getDebut());
                    conflit.setDateFin(activite.getFin());
                    conflit.setDescription(String.format(
                        "L'activité '%s' tombe pendant une période bloquée: %s (%s - %s)",
                        activite.getTitre(),
                        contrainte.getMotif() != null ? contrainte.getMotif() : "Non spécifié",
                        formatDateTime(contrainte.getDateDebut()),
                        formatDateTime(contrainte.getDateFin())
                    ));
                    conflits.add(conflit);
                }
            }
        }
        
        return conflits;
    }

    /**
     * Vérifie si deux périodes se chevauchent
     */
    private boolean seChevauchent(LocalDateTime debut1, LocalDateTime fin1,
                                   LocalDateTime debut2, LocalDateTime fin2) {
        return debut1.isBefore(fin2) && debut2.isBefore(fin1);
    }

    /**
     * Vérifie si deux plages horaires (LocalTime) se chevauchent
     */
    private boolean seChevauchenTime(LocalTime debut1, LocalTime fin1,
                                      LocalTime debut2, LocalTime fin2) {
        return debut1.isBefore(fin2) && debut2.isBefore(fin1);
    }

    /**
     * Valide qu'une activité respecte les contraintes avant sa création/modification
     */
    public List<String> validerActivite(Activite activite, int utilisateurId) {
        List<String> erreurs = new ArrayList<>();
        
        if (activite.getDebut() == null || activite.getFin() == null) {
            erreurs.add("Les dates de début et fin sont obligatoires");
            return erreurs;
        }
        
        if (activite.getDebut().isAfter(activite.getFin())) {
            erreurs.add("La date de début doit être antérieure à la date de fin");
        }
        
        // Charger toutes les activités existantes (sauf celle en cours d'édition)
        List<Activite> activitesExistantes = activiteDAO.findAllByUser(utilisateurId);
        activitesExistantes.removeIf(a -> a.getId() == activite.getId());
        
        // Vérifier les chevauchements
        for (Activite existante : activitesExistantes) {
            if (existante.getDebut() != null && existante.getFin() != null) {
                if (seChevauchent(activite.getDebut(), activite.getFin(),
                                  existante.getDebut(), existante.getFin())) {
                    erreurs.add(String.format("Conflit avec l'activité existante '%s'", existante.getTitre()));
                }
            }
        }
        
        // Vérifier les contraintes horaires
        List<ContrainteHoraire> contraintesHoraires = contrainteDAO.findHoraireByUser(utilisateurId);
        DayOfWeek jour = activite.getDebut().getDayOfWeek();
        String jourStr = jour.getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase();
        LocalTime heureDebut = activite.getDebut().toLocalTime();
        LocalTime heureFin = activite.getFin().toLocalTime();
        
        for (ContrainteHoraire contrainte : contraintesHoraires) {
            if (contrainte.getJourSemaine().equals(jourStr) && 
                "INDISPONIBLE".equals(contrainte.getTypeContrainte()) &&
                contrainte.getHeureDebut() != null && contrainte.getHeureFin() != null) {
                if (seChevauchenTime(heureDebut, heureFin, 
                                     contrainte.getHeureDebut(), contrainte.getHeureFin())) {
                    erreurs.add(String.format("Vous êtes indisponible le %s entre %s et %s",
                        jourStr, contrainte.getHeureDebut(), contrainte.getHeureFin()));
                }
            }
        }
        
        // Vérifier les contraintes personnelles
        List<ContraintePersonnelle> contraintesPersonnelles = contrainteDAO.findPersonnelleByUser(utilisateurId);
        for (ContraintePersonnelle contrainte : contraintesPersonnelles) {
            if (contrainte.getDateDebut() != null && contrainte.getDateFin() != null) {
                if (seChevauchent(activite.getDebut(), activite.getFin(),
                                  contrainte.getDateDebut(), contrainte.getDateFin())) {
                    erreurs.add(String.format("Période bloquée: %s",
                        contrainte.getMotif() != null ? contrainte.getMotif() : "Non spécifié"));
                }
            }
        }
        
        return erreurs;
    }

    /**
     * Formate une date-heure pour l'affichage
     */
    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return String.format("%02d/%02d/%d %02d:%02d",
            dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
            dt.getHour(), dt.getMinute());
    }

    public boolean hasConflit(Planning planning) {
        // TODO: check for time overlaps and other conflicts
        return false;
    }
}
