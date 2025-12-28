package com.planning.controller;

import com.planning.service.FatigueService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@WebServlet(name = "StatsController", urlPatterns = {"/stats", "/fatigue"})
public class StatsController extends HttpServlet {
    private final FatigueService fatigueService = new FatigueService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getServletPath();
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
            // Parse flexible date formats: "2025-12-28T00:00:00" or "2025-12-28T00:00"
            dateDebut = parseDateTime(debutStr);
            dateFin = parseDateTime(finStr);
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"status\":\"error\",\"error\":\"Format de date invalide: " + e.getMessage() + "\"}");
            return;
        }

        if (path.endsWith("/fatigue")) {
            Map<LocalDate, Double> daily = fatigueService.computeDailyFatigue(userId, dateDebut, dateFin);
            StringBuilder json = new StringBuilder("{\"status\":\"ok\",\"daily\":[");
            int i = 0;
            for (Map.Entry<LocalDate, Double> e : daily.entrySet()) {
                if (i++ > 0) json.append(',');
                json.append('{')
                    .append("\"date\":\"").append(e.getKey().toString()).append("\"")
                    .append(",\"fatigueIndex\":").append(String.format("%.0f", e.getValue()))
                    .append('}');
            }
            json.append(']').append('}');
            resp.getWriter().write(json.toString());
            return;
        }

        // /stats summary
        FatigueService.Summary s = fatigueService.computeSummary(userId, dateDebut, dateFin);
        StringBuilder json = new StringBuilder("{\"status\":\"ok\",\"summary\":{");
        // Force US locale for numeric formatting to ensure JSON uses '.'
        json.append("\"totalHours\":").append(String.format(java.util.Locale.US, "%.2f", s.totalHours))
            .append(",\"days\":").append(s.days)
            .append(",\"avgHoursPerDay\":").append(String.format(java.util.Locale.US, "%.2f", s.avgHoursPerDay))
            .append(",\"avgFatigue\":").append(String.format(java.util.Locale.US, "%.1f", s.avgFatigue))
            .append(",\"minFatigue\":").append(String.format(java.util.Locale.US, "%.0f", s.minFatigue))
            .append(",\"maxFatigue\":").append(String.format(java.util.Locale.US, "%.0f", s.maxFatigue))
            .append("}}");
        resp.getWriter().write(json.toString());
    }

    private static int parseInt(String s, int dflt) {
        try { return Integer.parseInt(s); } catch (Exception e) { return dflt; }
    }

    private static LocalDateTime parseDateTime(String dateStr) {
        // Try full format first: 2025-12-28T00:00:00
        try {
            return LocalDateTime.parse(dateStr);
        } catch (Exception e1) {
            // Try short format: 2025-12-28T00:00
            try {
                return LocalDateTime.parse(dateStr + ":00");
            } catch (Exception e2) {
                throw new RuntimeException("Invalid date format: " + dateStr);
            }
        }
    }
}
