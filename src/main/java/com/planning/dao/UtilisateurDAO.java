package com.planning.dao;

import com.planning.model.Utilisateur;
import java.util.List;

public interface UtilisateurDAO {
    Utilisateur findById(int id);
    Utilisateur findByEmail(String email);
    List<Utilisateur> findAll();
    void save(Utilisateur user);
    void update(Utilisateur user);
    void delete(int id);
}