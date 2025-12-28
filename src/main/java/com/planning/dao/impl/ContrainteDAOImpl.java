package com.planning.dao.impl;

import com.planning.dao.ContrainteDAO;
import com.planning.dao.DBConnection;
import com.planning.model.ContrainteHoraire;
import com.planning.model.ContraintePersonnelle;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ContrainteDAOImpl implements ContrainteDAO {
    @Override
    public List<ContrainteHoraire> findHoraireByUser(int userId) {
        List<ContrainteHoraire> out = new ArrayList<>();
        String sql = "SELECT * FROM contraintes_horaires WHERE utilisateur_id=? ORDER BY FIELD(jour_semaine,'LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI','DIMANCHE'), heure_debut";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContrainteHoraire ch = new ContrainteHoraire();
                    ch.setId(rs.getInt("id"));
                    ch.setUtilisateurId(rs.getInt("utilisateur_id"));
                    ch.setJourSemaine(rs.getString("jour_semaine"));
                    Time t1 = rs.getTime("heure_debut");
                    Time t2 = rs.getTime("heure_fin");
                    ch.setHeureDebut(t1 != null ? t1.toLocalTime() : null);
                    ch.setHeureFin(t2 != null ? t2.toLocalTime() : null);
                    ch.setTypeContrainte(rs.getString("type_contrainte"));
                    out.add(ch);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    @Override
    public List<ContraintePersonnelle> findPersonnelleByUser(int userId) {
        List<ContraintePersonnelle> out = new ArrayList<>();
        String sql = "SELECT * FROM contraintes_personnelles WHERE utilisateur_id=? ORDER BY date_debut";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContraintePersonnelle cp = new ContraintePersonnelle();
                    cp.setId(rs.getInt("id"));
                    cp.setUtilisateurId(rs.getInt("utilisateur_id"));
                    Timestamp d1 = rs.getTimestamp("date_debut");
                    Timestamp d2 = rs.getTimestamp("date_fin");
                    cp.setDateDebut(d1 != null ? d1.toLocalDateTime() : null);
                    cp.setDateFin(d2 != null ? d2.toLocalDateTime() : null);
                    cp.setMotif(rs.getString("motif"));
                    cp.setTypeContrainte(rs.getString("type_contrainte"));
                    out.add(cp);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    @Override
    public void saveHoraire(ContrainteHoraire cst) {
        String sql = "INSERT INTO contraintes_horaires(utilisateur_id, jour_semaine, heure_debut, heure_fin, type_contrainte) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cst.getUtilisateurId());
            ps.setString(2, cst.getJourSemaine());
            ps.setTime(3, cst.getHeureDebut() != null ? Time.valueOf(cst.getHeureDebut()) : null);
            ps.setTime(4, cst.getHeureFin() != null ? Time.valueOf(cst.getHeureFin()) : null);
            ps.setString(5, cst.getTypeContrainte());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cst.setId(keys.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void savePersonnelle(ContraintePersonnelle cst) {
        String sql = "INSERT INTO contraintes_personnelles(utilisateur_id, date_debut, date_fin, motif, type_contrainte) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cst.getUtilisateurId());
            ps.setTimestamp(2, cst.getDateDebut() != null ? Timestamp.valueOf(cst.getDateDebut()) : null);
            ps.setTimestamp(3, cst.getDateFin() != null ? Timestamp.valueOf(cst.getDateFin()) : null);
            ps.setString(4, cst.getMotif());
            ps.setString(5, cst.getTypeContrainte());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cst.setId(keys.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(int id) {
        String[] tables = {"contraintes_personnelles", "contraintes_horaires"};
        for (String tbl : tables) {
            String sql = "DELETE FROM " + tbl + " WHERE id=?";
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                int ct = ps.executeUpdate();
                if (ct > 0) return;
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
