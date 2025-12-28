package com.planning.service;

import com.planning.model.Activite;
import com.planning.model.ContrainteHoraire;
import com.planning.model.ContraintePersonnelle;
import com.planning.dao.ActiviteDAO;
import com.planning.dao.ContrainteDAO;
import com.planning.dao.impl.ActiviteDAOImpl;
import com.planning.dao.impl.ContrainteDAOImpl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

/**
 * Service d'optimisation de planning par Recuit Simulé (Simulated Annealing)
 * Objectif: Générer un planning optimal respectant les contraintes et minimisant:
 *   - Les chevauchements d'activités
 *   - Les temps morts (gaps)
 *   - Les violations de contraintes horaires
 *   - Le non-respect des priorités
 */
public class OptimisationService {
    
    private final ActiviteDAO activiteDAO;
    private final ContrainteDAO contrainteDAO;
    private final ConflitService conflitService;
    private final Random random;
    
    // Paramètres du recuit simulé
    private static final double TEMPERATURE_INITIALE = 100.0;
    private static final double TEMPERATURE_MINIMALE = 0.1;
    private static final double TAUX_REFROIDISSEMENT = 0.95;
    private static final int ITERATIONS_PAR_TEMPERATURE = 50;
    
    // Poids de la fonction objectif
    private static final double POIDS_CONFLITS = 100.0;
    private static final double POIDS_GAPS = 10.0;
    private static final double POIDS_CONTRAINTES = 80.0;
    private static final double POIDS_PRIORITES = 20.0;
    
    public OptimisationService() {
        this.activiteDAO = new ActiviteDAOImpl();
        this.contrainteDAO = new ContrainteDAOImpl();
        this.conflitService = new ConflitService();
        this.random = new Random();
    }
    
    /**
     * Génère un planning optimisé pour une liste d'activités non planifiées
     * @param activitesNonPlanifiees Liste d'activités sans dates définitives
     * @param utilisateurId ID de l'utilisateur
     * @param dateDebut Date de début de la période de planification
     * @param dateFin Date de fin de la période de planification
     * @return Liste des activités optimalement planifiées
     */
    public List<Activite> genererPlanningOptimise(
            List<Activite> activitesNonPlanifiees,
            int utilisateurId,
            LocalDateTime dateDebut,
            LocalDateTime dateFin) {
        
        if (activitesNonPlanifiees == null || activitesNonPlanifiees.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Charger les contraintes de l'utilisateur
        List<ContrainteHoraire> contraintesHoraires = contrainteDAO.findHoraireByUser(utilisateurId);
        List<ContraintePersonnelle> contraintesPersonnelles = contrainteDAO.findPersonnelleByUser(utilisateurId);
        
        // Charger les activités déjà planifiées (fixes)
        List<Activite> activitesFixes = activiteDAO.findByUserAndDateRange(utilisateurId, dateDebut, dateFin);
        
        // Solution initiale: placement aléatoire
        List<Activite> solutionCourante = genererSolutionInitiale(
            activitesNonPlanifiees, dateDebut, dateFin, activitesFixes, contraintesHoraires, contraintesPersonnelles
        );
        
        double coutCourant = calculerCout(solutionCourante, activitesFixes, contraintesHoraires, contraintesPersonnelles);
        
        // Meilleure solution trouvée
        List<Activite> meilleureSolution = copierSolution(solutionCourante);
        double meilleurCout = coutCourant;
        
        // Recuit simulé
        double temperature = TEMPERATURE_INITIALE;
        
        while (temperature > TEMPERATURE_MINIMALE) {
            for (int iter = 0; iter < ITERATIONS_PAR_TEMPERATURE; iter++) {
                // Générer une solution voisine
                List<Activite> solutionVoisine = genererVoisin(solutionCourante, dateDebut, dateFin);
                double coutVoisin = calculerCout(solutionVoisine, activitesFixes, contraintesHoraires, contraintesPersonnelles);
                
                // Critère d'acceptation
                double delta = coutVoisin - coutCourant;
                
                if (delta < 0 || Math.exp(-delta / temperature) > random.nextDouble()) {
                    // Accepter la solution voisine
                    solutionCourante = solutionVoisine;
                    coutCourant = coutVoisin;
                    
                    // Mettre à jour la meilleure solution
                    if (coutCourant < meilleurCout) {
                        meilleureSolution = copierSolution(solutionCourante);
                        meilleurCout = coutCourant;
                    }
                }
            }
            
            // Refroidissement
            temperature *= TAUX_REFROIDISSEMENT;
        }
        
        return meilleureSolution;
    }
    
    /**
     * Génère une solution initiale en plaçant les activités dans des créneaux valides
     */
    private List<Activite> genererSolutionInitiale(
            List<Activite> activites,
            LocalDateTime debut,
            LocalDateTime fin,
            List<Activite> activitesFixes,
            List<ContrainteHoraire> contraintesHoraires,
            List<ContraintePersonnelle> contraintesPersonnelles) {
        
        List<Activite> solution = new ArrayList<>();
        LocalDateTime curseur = debut;
        
        // Trier par priorité (URGENTE > HAUTE > NORMALE > BASSE)
        List<Activite> activitesTri = new ArrayList<>(activites);
        activitesTri.sort((a1, a2) -> comparerPriorites(a2.getPriorite(), a1.getPriorite()));
        
        for (Activite act : activitesTri) {
            // Calculer la durée de l'activité (estimation: 2h par défaut si non définie)
            Duration duree = (act.getDebut() != null && act.getFin() != null) 
                ? Duration.between(act.getDebut(), act.getFin())
                : Duration.ofHours(2);
            
            // Chercher un créneau valide
            LocalDateTime creneauDebut = trouverCreneauValide(
                curseur, fin, duree, activitesFixes, solution, contraintesHoraires, contraintesPersonnelles
            );
            
            if (creneauDebut != null) {
                Activite nouvAct = copierActivite(act);
                nouvAct.setDebut(creneauDebut);
                nouvAct.setFin(creneauDebut.plus(duree));
                solution.add(nouvAct);
                curseur = nouvAct.getFin();
            }
        }
        
        return solution;
    }
    
    /**
     * Trouve un créneau valide pour placer une activité
     */
    private LocalDateTime trouverCreneauValide(
            LocalDateTime debut,
            LocalDateTime fin,
            Duration duree,
            List<Activite> activitesFixes,
            List<Activite> solution,
            List<ContrainteHoraire> contraintesHoraires,
            List<ContraintePersonnelle> contraintesPersonnelles) {
        
        LocalDateTime curseur = debut;
        
        while (curseur.plus(duree).isBefore(fin) || curseur.plus(duree).isEqual(fin)) {
            LocalDateTime finProposee = curseur.plus(duree);
            
            // Vérifier absence de chevauchement avec activités fixes
            boolean chevauche = false;
            for (Activite fixe : activitesFixes) {
                if (seChevauchent(curseur, finProposee, fixe.getDebut(), fixe.getFin())) {
                    chevauche = true;
                    curseur = fixe.getFin(); // Sauter après cette activité
                    break;
                }
            }
            if (chevauche) continue;
            
            // Vérifier absence de chevauchement avec solution partielle
            for (Activite planifie : solution) {
                if (seChevauchent(curseur, finProposee, planifie.getDebut(), planifie.getFin())) {
                    chevauche = true;
                    curseur = planifie.getFin();
                    break;
                }
            }
            if (chevauche) continue;
            
            // Vérifier contraintes horaires
            if (!respecteContraintesHoraires(curseur, finProposee, contraintesHoraires)) {
                curseur = curseur.plusHours(1);
                continue;
            }
            
            // Vérifier contraintes personnelles
            if (!respecteContraintesPersonnelles(curseur, finProposee, contraintesPersonnelles)) {
                curseur = curseur.plusHours(1);
                continue;
            }
            
            // Créneau valide trouvé
            return curseur;
        }
        
        // Aucun créneau trouvé
        return null;
    }
    
    /**
     * Génère une solution voisine en perturbant légèrement la solution courante
     */
    private List<Activite> genererVoisin(List<Activite> solution, LocalDateTime debut, LocalDateTime fin) {
        if (solution.isEmpty()) return solution;
        
        List<Activite> voisin = copierSolution(solution);
        
        // Choisir aléatoirement un type de perturbation
        int typePerturb = random.nextInt(3);
        
        switch (typePerturb) {
            case 0: // Décaler une activité
                int idx = random.nextInt(voisin.size());
                Activite act = voisin.get(idx);
                int decalageHeures = random.nextInt(5) - 2; // -2 à +2 heures
                LocalDateTime nouveauDebut = act.getDebut().plusHours(decalageHeures);
                if (nouveauDebut.isAfter(debut) && nouveauDebut.isBefore(fin)) {
                    Duration duree = Duration.between(act.getDebut(), act.getFin());
                    act.setDebut(nouveauDebut);
                    act.setFin(nouveauDebut.plus(duree));
                }
                break;
                
            case 1: // Échanger deux activités
                if (voisin.size() >= 2) {
                    int idx1 = random.nextInt(voisin.size());
                    int idx2 = random.nextInt(voisin.size());
                    if (idx1 != idx2) {
                        Activite a1 = voisin.get(idx1);
                        Activite a2 = voisin.get(idx2);
                        
                        LocalDateTime debut1 = a1.getDebut();
                        LocalDateTime fin1 = a1.getFin();
                        
                        Duration duree1 = Duration.between(a1.getDebut(), a1.getFin());
                        Duration duree2 = Duration.between(a2.getDebut(), a2.getFin());
                        
                        a1.setDebut(a2.getDebut());
                        a1.setFin(a2.getDebut().plus(duree1));
                        
                        a2.setDebut(debut1);
                        a2.setFin(debut1.plus(duree2));
                    }
                }
                break;
                
            case 2: // Changer la durée d'une activité
                int idx3 = random.nextInt(voisin.size());
                Activite act3 = voisin.get(idx3);
                int changementMinutes = (random.nextInt(7) - 3) * 15; // -45 à +45 min par paliers de 15
                LocalDateTime nouvelleFin = act3.getFin().plusMinutes(changementMinutes);
                if (nouvelleFin.isAfter(act3.getDebut()) && nouvelleFin.isBefore(fin)) {
                    act3.setFin(nouvelleFin);
                }
                break;
        }
        
        return voisin;
    }
    
    /**
     * Calcule le coût d'une solution (fonction objectif multi-critères)
     * Plus le coût est bas, meilleure est la solution
     */
    private double calculerCout(
            List<Activite> solution,
            List<Activite> activitesFixes,
            List<ContrainteHoraire> contraintesHoraires,
            List<ContraintePersonnelle> contraintesPersonnelles) {
        
        double cout = 0.0;
        
        // 1. Pénalité pour les chevauchements
        int nbChevauchements = compterChevauchements(solution, activitesFixes);
        cout += nbChevauchements * POIDS_CONFLITS;
        
        // 2. Pénalité pour les temps morts (gaps)
        double tempsMorts = calculerTempsMorts(solution);
        cout += tempsMorts * POIDS_GAPS;
        
        // 3. Pénalité pour violations de contraintes
        int nbViolationsContraintes = compterViolationsContraintes(solution, contraintesHoraires, contraintesPersonnelles);
        cout += nbViolationsContraintes * POIDS_CONTRAINTES;
        
        // 4. Pénalité pour mauvais placement des priorités (hautes priorités doivent être tôt)
        double penalitePriorites = calculerPenalitePriorites(solution);
        cout += penalitePriorites * POIDS_PRIORITES;
        
        return cout;
    }
    
    private int compterChevauchements(List<Activite> solution, List<Activite> activitesFixes) {
        int count = 0;
        
        // Chevauchements internes
        for (int i = 0; i < solution.size(); i++) {
            for (int j = i + 1; j < solution.size(); j++) {
                if (seChevauchent(solution.get(i).getDebut(), solution.get(i).getFin(),
                                  solution.get(j).getDebut(), solution.get(j).getFin())) {
                    count++;
                }
            }
        }
        
        // Chevauchements avec activités fixes
        for (Activite act : solution) {
            for (Activite fixe : activitesFixes) {
                if (seChevauchent(act.getDebut(), act.getFin(), fixe.getDebut(), fixe.getFin())) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private double calculerTempsMorts(List<Activite> solution) {
        if (solution.size() < 2) return 0.0;
        
        // Trier par date de début
        List<Activite> triees = new ArrayList<>(solution);
        triees.sort(Comparator.comparing(Activite::getDebut));
        
        double tempsMortsTotal = 0.0;
        for (int i = 0; i < triees.size() - 1; i++) {
            LocalDateTime finCourante = triees.get(i).getFin();
            LocalDateTime debutSuivante = triees.get(i + 1).getDebut();
            
            if (debutSuivante.isAfter(finCourante)) {
                long minutes = Duration.between(finCourante, debutSuivante).toMinutes();
                // Gaps < 30 min sont acceptables (pause), au-delà c'est pénalisé
                if (minutes > 30) {
                    tempsMortsTotal += (minutes - 30) / 60.0; // en heures
                }
            }
        }
        
        return tempsMortsTotal;
    }
    
    private int compterViolationsContraintes(
            List<Activite> solution,
            List<ContrainteHoraire> contraintesHoraires,
            List<ContraintePersonnelle> contraintesPersonnelles) {
        
        int violations = 0;
        
        for (Activite act : solution) {
            if (!respecteContraintesHoraires(act.getDebut(), act.getFin(), contraintesHoraires)) {
                violations++;
            }
            if (!respecteContraintesPersonnelles(act.getDebut(), act.getFin(), contraintesPersonnelles)) {
                violations++;
            }
        }
        
        return violations;
    }
    
    private double calculerPenalitePriorites(List<Activite> solution) {
        double penalite = 0.0;
        
        // Trier par date
        List<Activite> triees = new ArrayList<>(solution);
        triees.sort(Comparator.comparing(Activite::getDebut));
        
        // Les activités URGENTES/HAUTES devraient être en début de planning
        for (int i = 0; i < triees.size(); i++) {
            Activite act = triees.get(i);
            int poidsPriorite = getPoidsPriorite(act.getPriorite());
            
            // Plus l'activité importante est loin dans le planning, plus c'est pénalisé
            penalite += (i / (double) triees.size()) * poidsPriorite;
        }
        
        return penalite;
    }
    
    private boolean seChevauchent(LocalDateTime debut1, LocalDateTime fin1, LocalDateTime debut2, LocalDateTime fin2) {
        return debut1.isBefore(fin2) && debut2.isBefore(fin1);
    }
    
    private boolean respecteContraintesHoraires(
            LocalDateTime debut,
            LocalDateTime fin,
            List<ContrainteHoraire> contraintes) {
        // Utiliser le même format de jour que ConflitService: libellé FR en majuscules
        java.time.DayOfWeek dow = debut.getDayOfWeek();
        String jourFr = dow.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH).toUpperCase();

        for (ContrainteHoraire contrainte : contraintes) {
            if ("INDISPONIBLE".equals(contrainte.getTypeContrainte())) {
                if (jourFr.equals(contrainte.getJourSemaine())) {
                    LocalTime heureDebut = debut.toLocalTime();
                    LocalTime heureFin = fin.toLocalTime();
                    if (seChevauchenTime(heureDebut, heureFin,
                            contrainte.getHeureDebut(), contrainte.getHeureFin())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean respecteContraintesPersonnelles(
            LocalDateTime debut,
            LocalDateTime fin,
            List<ContraintePersonnelle> contraintes) {
        
        for (ContraintePersonnelle contrainte : contraintes) {
            if (seChevauchent(debut, fin, contrainte.getDateDebut(), contrainte.getDateFin())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean seChevauchenTime(LocalTime debut1, LocalTime fin1, LocalTime debut2, LocalTime fin2) {
        return debut1.isBefore(fin2) && debut2.isBefore(fin1);
    }
    
    private int comparerPriorites(String p1, String p2) {
        return getPoidsPriorite(p1) - getPoidsPriorite(p2);
    }
    
    private int getPoidsPriorite(String priorite) {
        if (priorite == null) return 1;
        switch (priorite.toUpperCase()) {
            case "URGENTE": return 4;
            case "HAUTE": return 3;
            case "NORMALE": return 2;
            case "BASSE": return 1;
            default: return 1;
        }
    }
    
    private List<Activite> copierSolution(List<Activite> solution) {
        List<Activite> copie = new ArrayList<>();
        for (Activite act : solution) {
            copie.add(copierActivite(act));
        }
        return copie;
    }
    
    private Activite copierActivite(Activite source) {
        Activite copie = new Activite();
        copie.setId(source.getId());
        copie.setUtilisateurId(source.getUtilisateurId());
        copie.setTitre(source.getTitre());
        copie.setDescription(source.getDescription());
        copie.setDebut(source.getDebut());
        copie.setFin(source.getFin());
        copie.setCategorieId(source.getCategorieId());
        copie.setPriorite(source.getPriorite());
        copie.setStatut(source.getStatut());
        return copie;
    }
}