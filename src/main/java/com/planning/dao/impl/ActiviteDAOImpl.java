package com.planning.dao.impl;

import com.planning.dao.ActiviteDAO;
import com.planning.dao.DBConnection;
import com.planning.model.Activite;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActiviteDAOImpl implements ActiviteDAO {

    private Activite map(ResultSet rs) throws SQLException {
        Activite a = new Activite();
        a.setId(rs.getInt("id"));
        a.setTitre(rs.getString("titre"));
        a.setDescription(rs.getString("description"));
        Timestamp d1 = rs.getTimestamp("date_debut");
        Timestamp d2 = rs.getTimestamp("date_fin");
        a.setDebut(d1 != null ? d1.toLocalDateTime() : null);
        a.setFin(d2 != null ? d2.toLocalDateTime() : null);
        int catId = rs.getInt("categorie_id");
        a.setCategorieId(rs.wasNull() ? null : catId);
        a.setUtilisateurId(rs.getInt("utilisateur_id"));
        a.setPriorite(rs.getString("priorite"));
        a.setStatut(rs.getString("statut"));
        return a;
    }

    @Override
    public Activite findById(int id) {
        String sql = "SELECT * FROM activites WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Activite> findAllByUser(int userId) {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM activites WHERE utilisateur_id = ? ORDER BY date_debut";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(Activite a) {
        String sql = "INSERT INTO activites(titre, description, date_debut, date_fin, priorite, statut, categorie_id, utilisateur_id) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setTimestamp(3, a.getDebut() != null ? Timestamp.valueOf(a.getDebut()) : null);
            ps.setTimestamp(4, a.getFin() != null ? Timestamp.valueOf(a.getFin()) : null);
            ps.setString(5, a.getPriorite() != null ? a.getPriorite() : "NORMALE");
            ps.setString(6, a.getStatut() != null ? a.getStatut() : "PLANIFIE");
            if (a.getCategorieId() != null) ps.setInt(7, a.getCategorieId()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, a.getUtilisateurId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setId(keys.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void update(Activite a) {
        String sql = "UPDATE activites SET titre=?, description=?, date_debut=?, date_fin=?, priorite=?, statut=?, categorie_id=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setTimestamp(3, a.getDebut() != null ? Timestamp.valueOf(a.getDebut()) : null);
            ps.setTimestamp(4, a.getFin() != null ? Timestamp.valueOf(a.getFin()) : null);
            ps.setString(5, a.getPriorite());
            ps.setString(6, a.getStatut());
            if (a.getCategorieId() != null) ps.setInt(7, a.getCategorieId()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM activites WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Activite> findByUserAndDateRange(int userId, LocalDateTime debut, LocalDateTime fin) {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM activites WHERE utilisateur_id = ? AND date_debut >= ? AND date_fin <= ? ORDER BY date_debut";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(debut));
            ps.setTimestamp(3, Timestamp.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
