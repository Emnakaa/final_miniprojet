package com.planning.controller;

import com.planning.model.Activite;
import com.planning.service.OptimisationService;
import com.planning.dao.impl.ActiviteDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "OptimisationController", urlPatterns = {"/optimisation/generer", "/optimisation/appliquer"})
public class OptimisationController extends HttpServlet {
    private final OptimisationService optimisationService = new OptimisationService();
    private final ActiviteDAOImpl activiteDAO = new ActiviteDAOImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String servletPath = req.getServletPath();
        int userId = parseInt(req.getParameter("userId"), 2);

        String debutStr = req.getParameter("dateDebut");
        String finStr = req.getParameter("dateFin");
        if (debutStr == null || finStr == null || debutStr.isEmpty() || finStr.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\":\"error\",\"error\":\"Paramètres dateDebut/dateFin requis\"}");
            return;
        }

        LocalDateTime dateDebut;
        LocalDateTime dateFin;
        try {
            dateDebut = LocalDateTime.parse(debutStr);
            dateFin = LocalDateTime.parse(finStr);
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\":\"error\",\"error\":\"Format de date invalide\"}");
            return;
        }

        // Construire la liste des activités à optimiser (optionnel via JSON, sinon modèle par défaut)
        List<Activite> aOptimiser = buildInputActivities(req, userId, dateDebut);

        // Générer le planning optimisé
        List<Activite> resultat = optimisationService.genererPlanningOptimise(aOptimiser, userId, dateDebut, dateFin);

        boolean appliquer = servletPath.endsWith("/appliquer");
        if (appliquer) {
            try {
                for (Activite act : resultat) {
                    // Persister: update si id>0, sinon création
                    if (act.getId() > 0) {
                        activiteDAO.update(act);
                    } else {
                        activiteDAO.save(act);
                    }
                }
            } catch (Exception e) {
                resp.setStatus(500);
                resp.getWriter().write("{\"status\":\"error\",\"error\":\"" + escape(e.getMessage()) + "\"}");
                return;
            }
        }

        // Réponse JSON
        StringBuilder json = new StringBuilder("{");
        json.append("\"status\":\"ok\"");
        if (appliquer) json.append(",\"applied\":true");
        json.append(",\"activites\":[");
        for (int i = 0; i < resultat.size(); i++) {
            Activite a = resultat.get(i);
            if (i > 0) json.append(',');
            json.append('{')
                .append("\"id\":").append(a.getId())
                .append(",\"titre\":\"").append(escape(nvl(a.getTitre(), ""))).append("\"")
                .append(",\"debut\":\"").append(a.getDebut() != null ? a.getDebut().toString() : "").append("\"")
                .append(",\"fin\":\"").append(a.getFin() != null ? a.getFin().toString() : "").append("\"")
                .append(",\"priorite\":\"").append(escape(nvl(a.getPriorite(), "NORMALE"))).append("\"")
                .append('}');
        }
        json.append(']').append('}');
        resp.getWriter().write(json.toString());
    }

    private List<Activite> buildInputActivities(HttpServletRequest req, int userId, LocalDateTime dateDebut) {
        String activitesJson = req.getParameter("activites");
        List<Activite> list = new ArrayList<>();
        if (activitesJson == null || activitesJson.trim().isEmpty()) {
            // Modèle par défaut: 3 activités de 2h avec différentes priorités
            list.add(makeActivity(userId, "Tâche A", "", dateDebut, dateDebut.plusHours(2), "HAUTE"));
            list.add(makeActivity(userId, "Tâche B", "", dateDebut, dateDebut.plusHours(2), "NORMALE"));
            list.add(makeActivity(userId, "Tâche C", "", dateDebut, dateDebut.plusHours(2), "BASSE"));
            return list;
        }

        // Parsing léger du JSON attendu: [{ "titre": "...", "dureeHeures": 2, "priorite": "HAUTE" }, ...]
        // Sans dépendance externe: extraire champs simples par motifs
        try {
            String s = activitesJson;
            // Séparer les objets naïvement par '}'
            String[] objs = s.split("\\}");
            int offset = 0;
            for (String raw : objs) {
                String chunk = raw.trim();
                if (chunk.isEmpty()) continue;
                // Normaliser en supprimant les espaces/blancs pour des recherches simples
                String norm = chunk.replaceAll("\\s+", "");
                // Récupérer titre
                String titre = extractString(norm, "\"titre\":\"");
                if (titre == null) titre = "Sans titre";
                // Récupérer durée en heures
                Integer dureeH = extractInt(norm, "\"dureeHeures\":");
                if (dureeH == null || dureeH <= 0) dureeH = 2;
                // Récupérer priorité
                String priorite = extractString(norm, "\"priorite\":\"");
                if (priorite == null) priorite = "NORMALE";

                LocalDateTime debut = dateDebut.plusHours(offset);
                LocalDateTime fin = debut.plusHours(dureeH);
                list.add(makeActivity(userId, titre, "", debut, fin, priorite));
                offset += 1; // étaler légèrement le point de départ pour éviter les mêmes heures
            }
        } catch (Exception e) {
            // En cas d'échec de parsing, revenir au modèle par défaut
            list.clear();
            list.add(makeActivity(userId, "Tâche A", "", dateDebut, dateDebut.plusHours(2), "HAUTE"));
            list.add(makeActivity(userId, "Tâche B", "", dateDebut, dateDebut.plusHours(2), "NORMALE"));
            list.add(makeActivity(userId, "Tâche C", "", dateDebut, dateDebut.plusHours(2), "BASSE"));
        }
        return list;
    }

    private Activite makeActivity(int userId, String titre, String description, LocalDateTime debut, LocalDateTime fin, String priorite) {
        Activite a = new Activite();
        a.setUtilisateurId(userId);
        a.setTitre(titre);
        a.setDescription(description);
        a.setDebut(debut);
        a.setFin(fin);
        a.setPriorite(priorite);
        a.setStatut("PLANIFIE");
        return a;
    }

    private static int parseInt(String s, int dflt) {
        try { return Integer.parseInt(s); } catch (Exception e) { return dflt; }
    }
    private static String escape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String nvl(String s, String d) { return s == null || s.isEmpty() ? d : s; }

    // Extraction utilitaires (très naïfs) pour éviter d'ajouter une lib JSON
    private String extractString(String src, String keyPattern) {
        try {
            int idx = src.indexOf(keyPattern);
            if (idx < 0) return null;
            int start = idx + keyPattern.length();
            int end = src.indexOf('"', start);
            if (end < 0) return null;
            return src.substring(start, end);
        } catch (Exception e) { return null; }
    }
    private Integer extractInt(String src, String keyPattern) {
        try {
            int idx = src.indexOf(keyPattern);
            if (idx < 0) return null;
            int start = idx + keyPattern.length();
            StringBuilder num = new StringBuilder();
            while (start < src.length()) {
                char c = src.charAt(start);
                if (Character.isDigit(c)) { num.append(c); start++; }
                else break;
            }
            if (num.length() == 0) return null;
            return Integer.parseInt(num.toString());
        } catch (Exception e) { return null; }
    }
}
