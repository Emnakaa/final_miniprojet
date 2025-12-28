package com.planning.controller;

import com.planning.dao.impl.ContrainteDAOImpl;
import com.planning.model.ContrainteHoraire;
import com.planning.model.ContraintePersonnelle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@WebServlet(name = "ContrainteController", urlPatterns = {"/contraintes"})
public class ContrainteController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int userId = parseInt(req.getParameter("userId"), 2);
        ContrainteDAOImpl dao = new ContrainteDAOImpl();
        List<ContrainteHoraire> horaires = dao.findHoraireByUser(userId);
        List<ContraintePersonnelle> persos = dao.findPersonnelleByUser(userId);
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"horaires\":[");
        for (int i = 0; i < horaires.size(); i++) {
            ContrainteHoraire c = horaires.get(i);
            sb.append("{\"id\":").append(c.getId())
              .append(",\"jour\":\"").append(c.getJourSemaine()).append("\"")
              .append(",\"debut\":\"").append(c.getHeureDebut() != null ? c.getHeureDebut().toString() : "").append("\"")
              .append(",\"fin\":\"").append(c.getHeureFin() != null ? c.getHeureFin().toString() : "").append("\"")
              .append(",\"type\":\"").append(c.getTypeContrainte()).append("\"}");
            if (i < horaires.size() - 1) sb.append(',');
        }
        sb.append(']');
        sb.append(",\"personnelles\":[");
        for (int i = 0; i < persos.size(); i++) {
            ContraintePersonnelle c = persos.get(i);
            sb.append("{\"id\":").append(c.getId())
              .append(",\"debut\":\"").append(c.getDateDebut() != null ? c.getDateDebut().toString() : "").append("\"")
              .append(",\"fin\":\"").append(c.getDateFin() != null ? c.getDateFin().toString() : "").append("\"")
              .append(",\"motif\":\"").append(c.getMotif() == null ? "" : c.getMotif().replace("\"","\\\"")).append("\"")
              .append(",\"type\":\"").append(c.getTypeContrainte()).append("\"}");
            if (i < persos.size() - 1) sb.append(',');
        }
        sb.append("]}");
        resp.getWriter().write(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String scope = req.getParameter("scope"); // horaire | personnelle
        String action = req.getParameter("action"); // create | delete
        ContrainteDAOImpl dao = new ContrainteDAOImpl();
        try {
            if ("horaire".equalsIgnoreCase(scope) && "create".equalsIgnoreCase(action)) {
                ContrainteHoraire c = new ContrainteHoraire();
                c.setUtilisateurId(parseInt(req.getParameter("userId"), 2));
                c.setJourSemaine(req.getParameter("jour"));
                String h1 = req.getParameter("debut");
                String h2 = req.getParameter("fin");
                c.setHeureDebut(h1 != null && !h1.isEmpty() ? LocalTime.parse(h1) : null);
                c.setHeureFin(h2 != null && !h2.isEmpty() ? LocalTime.parse(h2) : null);
                c.setTypeContrainte(req.getParameter("type"));
                dao.saveHoraire(c);
                resp.getWriter().write("{\"status\":\"ok\",\"id\":" + c.getId() + "}");
            } else if ("personnelle".equalsIgnoreCase(scope) && "create".equalsIgnoreCase(action)) {
                ContraintePersonnelle c = new ContraintePersonnelle();
                c.setUtilisateurId(parseInt(req.getParameter("userId"), 2));
                String d1 = req.getParameter("debut");
                String d2 = req.getParameter("fin");
                c.setDateDebut(d1 != null && !d1.isEmpty() ? LocalDateTime.parse(d1) : null);
                c.setDateFin(d2 != null && !d2.isEmpty() ? LocalDateTime.parse(d2) : null);
                c.setMotif(req.getParameter("motif"));
                c.setTypeContrainte(req.getParameter("type"));
                dao.savePersonnelle(c);
                resp.getWriter().write("{\"status\":\"ok\",\"id\":" + c.getId() + "}");
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
            resp.getWriter().write("{\"error\":\"" + e.getMessage().replace("\"","\\\"") + "\"}");
        }
    }

    private static int parseInt(String s, int dflt) {
        try { return Integer.parseInt(s); } catch (Exception e) { return dflt; }
    }
}