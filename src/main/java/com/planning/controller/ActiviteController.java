package com.planning.controller;

import com.planning.dao.impl.ActiviteDAOImpl;
import com.planning.model.Activite;
import com.planning.service.ConflitService;
import com.planning.service.OptimisationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet(name = "ActiviteController", urlPatterns = {"/activites"})
public class ActiviteController extends HttpServlet {
    private final ConflitService conflitService = new ConflitService();
    private final OptimisationService optimisationService = new OptimisationService();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int userId = parseInt(req.getParameter("userId"), 2);
        List<Activite> activites = new ActiviteDAOImpl().findAllByUser(userId);
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < activites.size(); i++) {
            Activite a = activites.get(i);
            json.append("{\"id\":").append(a.getId())
                .append(",\"titre\":\"").append(escape(a.getTitre())).append("\"")
                .append(",\"description\":\"").append(escape(nullToEmpty(a.getDescription()))).append("\"")
                .append(",\"debut\":\"").append(a.getDebut() != null ? a.getDebut().toString() : "").append("\"")
                .append(",\"fin\":\"").append(a.getFin() != null ? a.getFin().toString() : "").append("\"")
                .append(",\"categorieId\":").append(a.getCategorieId() == null ? "null" : a.getCategorieId())
                .append(",\"priorite\":\"").append(escape(nullToEmpty(a.getPriorite()))).append("\"")
                .append(",\"statut\":\"").append(escape(nullToEmpty(a.getStatut()))).append("\"")
                .append("}");
            if (i < activites.size() - 1) json.append(',');
        }
        json.append(']');
        resp.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String action = req.getParameter("action");
        ActiviteDAOImpl dao = new ActiviteDAOImpl();
        try {
            if ("create".equalsIgnoreCase(action)) {
                Activite a = fromRequest(req);
                
                // Validation avec détection de conflits
                List<String> erreurs = conflitService.validerActivite(a, a.getUtilisateurId());
                if (!erreurs.isEmpty()) {
                    // Tentative de résolution automatique par optimisation
                    String resultat = optimiserEtSauvegarder(a, dao);
                    resp.getWriter().write(resultat);
                    return;
                }
                
                dao.save(a);
                resp.getWriter().write("{\"status\":\"ok\",\"id\":" + a.getId() + "}");
            } else if ("update".equalsIgnoreCase(action)) {
                Activite a = fromRequest(req);
                a.setId(parseInt(req.getParameter("id"), 0));
                
                // Validation avec détection de conflits
                List<String> erreurs = conflitService.validerActivite(a, a.getUtilisateurId());
                if (!erreurs.isEmpty()) {
                    // Tentative de résolution automatique par optimisation
                    String resultat = optimiserEtSauvegarder(a, dao);
                    resp.getWriter().write(resultat);
                    return;
                }
                
                dao.update(a);
                resp.getWriter().write("{\"status\":\"ok\"}");
            } else if ("delete".equalsIgnoreCase(action)) {
                int id = parseInt(req.getParameter("id"), 0);
                dao.delete(id);
                resp.getWriter().write("{\"status\":\"ok\"}");
            } else {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Unknown action\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private Activite fromRequest(HttpServletRequest req) {
        Activite a = new Activite();
        a.setUtilisateurId(parseInt(req.getParameter("userId"), 2));
        a.setTitre(nvl(req.getParameter("titre"), "(Sans titre)"));
        a.setDescription(nvl(req.getParameter("description"), ""));
        String debut = req.getParameter("debut");
        String fin = req.getParameter("fin");
        a.setDebut(debut != null && !debut.isEmpty() ? LocalDateTime.parse(debut) : null);
        a.setFin(fin != null && !fin.isEmpty() ? LocalDateTime.parse(fin) : null);
        String cat = req.getParameter("categorieId");
        a.setCategorieId((cat == null || cat.isEmpty()) ? null : Integer.parseInt(cat));
        a.setPriorite(nvl(req.getParameter("priorite"), "NORMALE"));
        a.setStatut(nvl(req.getParameter("statut"), "PLANIFIE"));
        return a;
    }

    private static int parseInt(String s, int dflt) {
        try { return Integer.parseInt(s); } catch (Exception e) { return dflt; }
    }
    private static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String nvl(String s, String d) { return s == null || s.isEmpty() ? d : s; }

    /**
     * Si des conflits sont détectés, on tente une optimisation automatique
     * en replanifiant toutes les activités du créneau concerné (y compris celle reçue).
     */
    private String optimiserEtSauvegarder(Activite nouvelle, ActiviteDAOImpl dao) throws Exception {
        if (nouvelle.getDebut() == null || nouvelle.getFin() == null) {
            return "{\"status\":\"error\",\"error\":\"Dates manquantes pour optimiser\"}";
        }

        // Fenêtre d'optimisation : 1 jour avant/après l'activité
        java.time.LocalDateTime fenetreDebut = nouvelle.getDebut().minusDays(1);
        java.time.LocalDateTime fenetreFin = nouvelle.getFin().plusDays(1);

        // Charger les activités existantes dans la fenêtre
        java.util.List<Activite> existantes = dao.findByUserAndDateRange(nouvelle.getUtilisateurId(), fenetreDebut, fenetreFin);

        // En cas de mise à jour, retirer l'ancienne version de la liste
        if (nouvelle.getId() > 0) {
            existantes.removeIf(act -> act.getId() == nouvelle.getId());
        }

        // Ajouter la nouvelle/activite à replanifier
        existantes.add(nouvelle);

        // Générer un planning optimisé
        java.util.List<Activite> optimises = optimisationService.genererPlanningOptimise(
                existantes,
                nouvelle.getUtilisateurId(),
                fenetreDebut,
                fenetreFin
        );

        // Persister le nouveau planning (update si id>0, save sinon)
        for (Activite act : optimises) {
            if (act.getId() > 0) {
                dao.update(act);
            } else {
                dao.save(act);
            }
        }

        // Réponse JSON avec le planning optimisé
        StringBuilder json = new StringBuilder("{\"status\":\"ok\",\"optimised\":true,\"activites\":[");
        for (int i = 0; i < optimises.size(); i++) {
            Activite a = optimises.get(i);
            if (i > 0) json.append(',');
            json.append('{')
                .append("\"id\":").append(a.getId())
                .append(",\"titre\":\"").append(escape(nvl(a.getTitre(), ""))).append("\"")
                .append(",\"debut\":\"").append(a.getDebut() != null ? a.getDebut().toString() : "").append("\"")
                .append(",\"fin\":\"").append(a.getFin() != null ? a.getFin().toString() : "").append("\"")
                .append(",\"priorite\":\"").append(escape(nvl(a.getPriorite(), ""))).append("\"")
                .append('}');
        }
        json.append("],\"count\":").append(optimises.size()).append('}');
        return json.toString();
    }
}