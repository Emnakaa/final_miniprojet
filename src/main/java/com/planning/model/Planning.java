package com.planning.model;

import java.util.ArrayList;
import java.util.List;

public class Planning {
    private int id;
    private int utilisateurId;
    private List<Activite> activites = new ArrayList<>();

    public Planning() {}

    public Planning(int id, int utilisateurId) {
        this.id = id;
        this.utilisateurId = utilisateurId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public List<Activite> getActivites() { return activites; }
    public void setActivites(List<Activite> activites) { this.activites = activites; }

    public void addActivite(Activite a) { this.activites.add(a); }
}