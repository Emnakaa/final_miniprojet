package com.planning.model;

import java.time.LocalDateTime;

public class Activite {
    private int id;
    private int utilisateurId;
    private String titre;
    private String description;
    private LocalDateTime debut;
    private LocalDateTime fin;
    private Integer categorieId; // nullable
    private String priorite; // BASSE, NORMALE, HAUTE, URGENTE
    private String statut;   // PLANIFIE, EN_COURS, TERMINE, ANNULE

    public Activite() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime debut) { this.debut = debut; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    public Integer getCategorieId() { return categorieId; }
    public void setCategorieId(Integer categorieId) { this.categorieId = categorieId; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}