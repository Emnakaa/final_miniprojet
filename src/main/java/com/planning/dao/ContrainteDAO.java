package com.planning.dao;

import com.planning.model.ContrainteHoraire;
import com.planning.model.ContraintePersonnelle;
import java.util.List;

public interface ContrainteDAO {
    List<ContrainteHoraire> findHoraireByUser(int userId);
    List<ContraintePersonnelle> findPersonnelleByUser(int userId);
    void saveHoraire(ContrainteHoraire c);
    void savePersonnelle(ContraintePersonnelle c);
    void delete(int id);
}