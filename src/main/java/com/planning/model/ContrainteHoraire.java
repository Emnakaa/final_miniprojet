package com.planning.model;

import java.time.LocalTime;

public class ContrainteHoraire {
    private int id;
    private int utilisateurId;
    private String jourSemaine; // LUNDI .. DIMANCHE
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String typeContrainte; // DISPONIBLE, INDISPONIBLE

    public ContrainteHoraire() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) { this.jourSemaine = jourSemaine; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getTypeContrainte() { return typeContrainte; }
    public void setTypeContrainte(String typeContrainte) { this.typeContrainte = typeContrainte; }
}