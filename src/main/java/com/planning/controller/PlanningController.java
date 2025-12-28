package com.planning.controller;

import com.planning.dao.impl.ActiviteDAOImpl;
import com.planning.model.Activite;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "PlanningController", urlPatterns = {"/planning"})
public class PlanningController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        int userId = parseInt(req.getParameter("userId"), 2);
        LocalDate start = parseDate(req.getParameter("start"), LocalDate.now());
        LocalDate end = parseDate(req.getParameter("end"), start.plusDays(6));

        List<Activite> all = new ActiviteDAOImpl().findAllByUser(userId);
        LocalDateTime sdt = start.atStartOfDay();
        LocalDateTime edt = end.plusDays(1).atStartOfDay();
        List<Activite> inRange = all.stream()
                .filter(a -> a.getDebut() != null && a.getFin() != null)
                .filter(a -> !(a.getFin().isBefore(sdt) || a.getDebut().isAfter(edt)))
                .collect(Collectors.toList());

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < inRange.size(); i++) {
            Activite a = inRange.get(i);
            json.append("{\"id\":").append(a.getId())
                .append(",\"title\":\"").append(escape(a.getTitre())).append("\"")
                .append(",\"start\":\"").append(a.getDebut().toString()).append("\"")
                .append(",\"end\":\"").append(a.getFin().toString()).append("\"")
                .append(",\"category\":").append(a.getCategorieId() == null ? "null" : a.getCategorieId())
                .append(",\"priority\":\"").append(a.getPriorite()).append("\"")
                .append("}");
            if (i < inRange.size() - 1) json.append(',');
        }
        json.append(']');
        resp.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: Generate/update planning via service
        resp.getWriter().write("PlanningController POST");
    }

    private static int parseInt(String s, int d) { try { return Integer.parseInt(s); } catch (Exception e) { return d; } }
    private static LocalDate parseDate(String s, LocalDate d) { try { return LocalDate.parse(s); } catch (Exception e) { return d; } }
    private static String escape(String s) { return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\""); }
}