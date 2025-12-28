package com.planning.controller;

import com.planning.model.Conflit;
import com.planning.service.ConflitService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/conflits")
public class ConflitController extends HttpServlet {
    private final ConflitService conflitService = new ConflitService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String userIdStr = req.getParameter("userId");
            String startStr = req.getParameter("start");
            String endStr = req.getParameter("end");

            if (userIdStr == null) {
                out.print("{\"status\":\"error\",\"message\":\"userId requis\"}");
                return;
            }

            int userId = Integer.parseInt(userIdStr);
            
            // Par défaut: semaine courante
            LocalDateTime dateDebut = startStr != null && !startStr.isEmpty()
                ? LocalDateTime.parse(startStr + "T00:00")
                : LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            
            LocalDateTime dateFin = endStr != null && !endStr.isEmpty()
                ? LocalDateTime.parse(endStr + "T23:59")
                : dateDebut.plusDays(7);

            List<Conflit> conflits = conflitService.detecterConflits(userId, dateDebut, dateFin);

            // Construction du JSON
            StringBuilder json = new StringBuilder("{\"status\":\"ok\",\"conflits\":[");
            for (int i = 0; i < conflits.size(); i++) {
                Conflit c = conflits.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"type\":\"").append(escapeJson(c.getType())).append("\",");
                json.append("\"severite\":\"").append(escapeJson(c.getSeverite())).append("\",");
                json.append("\"description\":\"").append(escapeJson(c.getDescription())).append("\",");
                json.append("\"activite1Id\":").append(c.getActivite1Id()).append(",");
                json.append("\"activite1Titre\":\"").append(escapeJson(c.getActivite1Titre())).append("\"");
                
                if (c.getActivite2Id() > 0) {
                    json.append(",\"activite2Id\":").append(c.getActivite2Id());
                    json.append(",\"activite2Titre\":\"").append(escapeJson(c.getActivite2Titre())).append("\"");
                }
                
                if (c.getDateDebut() != null) {
                    json.append(",\"dateDebut\":\"").append(c.getDateDebut().format(FORMATTER)).append("\"");
                }
                if (c.getDateFin() != null) {
                    json.append(",\"dateFin\":\"").append(c.getDateFin().format(FORMATTER)).append("\"");
                }
                
                json.append("}");
            }
            json.append("],\"count\":").append(conflits.size()).append("}");

            out.print(json.toString());

        } catch (NumberFormatException e) {
            out.print("{\"status\":\"error\",\"message\":\"Format userId invalide\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
