package com.planning.dao;

import com.planning.model.Planning;
import java.util.List;

public interface PlanningDAO {
    Planning findById(int id);
    List<Planning> findAllByUser(int userId);
    void save(Planning planning);
    void update(Planning planning);
    void delete(int id);
}