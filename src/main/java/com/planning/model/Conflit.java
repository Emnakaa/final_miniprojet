package com.planning.model;

import java.time.LocalDateTime;

public class Conflit {
    private int id;
    private int activite1Id;
    private int activite2Id;
    private String activite1Titre;
    private String activite2Titre;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String type; // CHEVAUCHEMENT, CONTRAINTE_HORAIRE, CONTRAINTE_PERSONNELLE, SOMMEIL
    private String severite; // CRITIQUE, MAJEURE, MINEURE
    private String description;

    public Conflit() {}

    public Conflit(String type, String severite, String description) {
        this.type = type;
        this.severite = severite;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getActivite1Id() { return activite1Id; }
    public void setActivite1Id(int activite1Id) { this.activite1Id = activite1Id; }

    public int getActivite2Id() { return activite2Id; }
    public void setActivite2Id(int activite2Id) { this.activite2Id = activite2Id; }

    public String getActivite1Titre() { return activite1Titre; }
    public void setActivite1Titre(String activite1Titre) { this.activite1Titre = activite1Titre; }

    public String getActivite2Titre() { return activite2Titre; }
    public void setActivite2Titre(String activite2Titre) { this.activite2Titre = activite2Titre; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeverite() { return severite; }
    public void setSeverite(String severite) { this.severite = severite; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
