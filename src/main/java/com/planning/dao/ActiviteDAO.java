package com.planning.dao;

import com.planning.model.Activite;
import java.time.LocalDateTime;
import java.util.List;

public interface ActiviteDAO {
    Activite findById(int id);
    List<Activite> findAllByUser(int userId);
    List<Activite> findByUserAndDateRange(int userId, LocalDateTime debut, LocalDateTime fin);
    void save(Activite activite);
    void update(Activite activite);
    void delete(int id);
}