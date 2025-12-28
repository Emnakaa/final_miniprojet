package com.planning.model;

import java.time.LocalDateTime;

public class ContraintePersonnelle {
    private int id;
    private int utilisateurId;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String motif;
    private String typeContrainte; // CONGE, REUNION, AUTRE

    public ContraintePersonnelle() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getTypeContrainte() { return typeContrainte; }
    public void setTypeContrainte(String typeContrainte) { this.typeContrainte = typeContrainte; }
}