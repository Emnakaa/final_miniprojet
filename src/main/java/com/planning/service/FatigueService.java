package com.planning.service;

import com.planning.dao.ActiviteDAO;
import com.planning.dao.impl.ActiviteDAOImpl;
import com.planning.model.Activite;

import java.time.*;
import java.util.*;

/**
 * Service de calcul d'indice de fatigue et métriques de base
 */
public class FatigueService {
    private final ActiviteDAO activiteDAO = new ActiviteDAOImpl();

    public FatigueService() {}

    /**
     * Calcule l'indice de fatigue par jour pour un utilisateur entre deux dates
     * Retourne une map LocalDate -> FatigueIndex (0-100)
     */
    public Map<LocalDate, Double> computeDailyFatigue(int userId, LocalDateTime debut, LocalDateTime fin) {
        List<Activite> activites = activiteDAO.findByUserAndDateRange(userId, debut, fin);
        Map<LocalDate, List<Interval>> byDay = splitByDay(activites);
        Map<LocalDate, Double> result = new LinkedHashMap<>();

        LocalDate d0 = debut.toLocalDate();
        LocalDate d1 = fin.toLocalDate();
        for (LocalDate d = d0; !d.isAfter(d1); d = d.plusDays(1)) {
            List<Interval> intervals = byDay.getOrDefault(d, Collections.emptyList());
            Metrics m = computeMetricsForDay(intervals, d);
            double fatigue = computeFatigueIndex(m);
            result.put(d, fatigue);
        }
        return result;
    }

    /**
     * Calcule un résumé sur la période: heures totales, moyenne/jour, min/max fatigue
     */
    public Summary computeSummary(int userId, LocalDateTime debut, LocalDateTime fin) {
        Map<LocalDate, Double> fatigueMap = computeDailyFatigue(userId, debut, fin);
        List<Activite> activites = activiteDAO.findByUserAndDateRange(userId, debut, fin);
        long totalMinutes = 0;
        for (Activite a : activites) {
            if (a.getDebut() != null && a.getFin() != null) {
                totalMinutes += Duration.between(a.getDebut(), a.getFin()).toMinutes();
            }
        }
        double min = 100, max = 0, sum = 0;
        int days = 0;
        for (Double v : fatigueMap.values()) {
            min = Math.min(min, v);
            max = Math.max(max, v);
            sum += v;
            days++;
        }
        Summary s = new Summary();
        s.totalHours = totalMinutes / 60.0;
        s.days = days;
        s.avgHoursPerDay = days > 0 ? s.totalHours / days : 0.0;
        s.avgFatigue = days > 0 ? sum / days : 0.0;
        s.minFatigue = days > 0 ? min : 0.0;
        s.maxFatigue = days > 0 ? max : 0.0;
        return s;
    }

    // ===== Internals =====
    private Map<LocalDate, List<Interval>> splitByDay(List<Activite> activites) {
        Map<LocalDate, List<Interval>> map = new LinkedHashMap<>();
        for (Activite a : activites) {
            LocalDateTime start = a.getDebut();
            LocalDateTime end = a.getFin();
            if (start == null || end == null) continue;
            LocalDate d = start.toLocalDate();
            LocalDate e = end.toLocalDate();
            LocalDate cur = d;
            while (!cur.isAfter(e)) {
                LocalDateTime dayStart = LocalDateTime.of(cur, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(cur, LocalTime.MAX);
                LocalDateTime segStart = start.isAfter(dayStart) ? start : dayStart;
                LocalDateTime segEnd = end.isBefore(dayEnd) ? end : dayEnd;
                if (segStart.isBefore(segEnd)) {
                    map.computeIfAbsent(cur, k -> new ArrayList<>())
                       .add(new Interval(segStart, segEnd));
                }
                cur = cur.plusDays(1);
            }
        }
        return map;
    }

    private Metrics computeMetricsForDay(List<Interval> intervals, LocalDate day) {
        Metrics m = new Metrics();
        if (intervals.isEmpty()) return m;

        // Sort by start
        intervals.sort(Comparator.comparing(i -> i.start));

        // Totals
        for (Interval it : intervals) {
            m.totalMinutes += Duration.between(it.start, it.end).toMinutes();
            // Night exposure (22:00-06:00)
            m.nightMinutes += overlapMinutes(it, day.atTime(22,0), day.plusDays(1).atTime(6,0));
            // Early start before 07:00
            if (it.start.toLocalTime().isBefore(LocalTime.of(7,0))) m.earlyStarts++;
        }

        // Overlaps count
        for (int i=0;i<intervals.size();i++) {
            for (int j=i+1;j<intervals.size();j++) {
                if (intervals.get(i).start.isBefore(intervals.get(j).end) &&
                    intervals.get(j).start.isBefore(intervals.get(i).end)) {
                    m.overlaps++;
                }
            }
        }

        // Continuous work (gaps < 15min are merged)
        List<Interval> merged = new ArrayList<>();
        Interval cur = intervals.get(0);
        for (int i=1;i<intervals.size();i++) {
            Interval next = intervals.get(i);
            long gap = Duration.between(cur.end, next.start).toMinutes();
            if (gap <= 15) {
                cur = new Interval(cur.start, next.end.isAfter(cur.end) ? next.end : cur.end);
            } else {
                merged.add(cur);
                cur = next;
            }
        }
        merged.add(cur);

        for (Interval block : merged) {
            long mins = Duration.between(block.start, block.end).toMinutes();
            if (mins > 120) m.continuousOverMinutes += (mins - 120);
        }
        return m;
    }

    private long overlapMinutes(Interval it, LocalDateTime s, LocalDateTime e) {
        LocalDateTime a = it.start.isAfter(s) ? it.start : s;
        LocalDateTime b = it.end.isBefore(e) ? it.end : e;
        if (a.isBefore(b)) return Duration.between(a, b).toMinutes();
        return 0;
    }

    private double computeFatigueIndex(Metrics m) {
        double risk = 0.0;
        risk += m.overlaps * 10.0;
        risk += (m.nightMinutes / 15.0) * 2.0;
        risk += m.earlyStarts * 5.0;
        risk += m.continuousOverMinutes * 0.5;
        double over8h = Math.max(0, (m.totalMinutes - 480));
        risk += over8h * 0.5;
        double index = 20.0 + risk; // base fatigue
        if (index > 100.0) index = 100.0;
        if (index < 0.0) index = 0.0;
        return Math.round(index);
    }

    // ===== Data Types =====
    private static class Interval {
        final LocalDateTime start;
        final LocalDateTime end;
        Interval(LocalDateTime s, LocalDateTime e) { this.start = s; this.end = e; }
    }
    private static class Metrics {
        long totalMinutes = 0;
        long nightMinutes = 0;
        int earlyStarts = 0;
        int overlaps = 0;
        long continuousOverMinutes = 0;
    }

    public static class Summary {
        public double totalHours;
        public int days;
        public double avgHoursPerDay;
        public double avgFatigue;
        public double minFatigue;
        public double maxFatigue;
    }
}
