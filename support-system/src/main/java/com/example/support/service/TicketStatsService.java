package com.example.support.service;

import com.example.support.entity.Ticket;
import com.example.support.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketStatsService {

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Статистика по соблюдению SLA
     */
    public Map<String, Object> getSlaComplianceStats() {
        List<Ticket> allTickets = ticketRepository.findAll();

        Map<String, Object> stats = new HashMap<>();

        // Общая статистика
        long totalTickets = allTickets.size();
        long closedTickets = allTickets.stream()
                .filter(t -> "Closed".equals(t.getStatus()))
                .count();

        // Статистика по статусам
        Map<String, Long> statusCount = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        // Среднее время обработки (для закрытых тикетов)
        double avgProcessingHours = calculateAverageProcessingTime(allTickets);

        // Процент соблюдения SLA (если бы было время создания)
        double slaComplianceRate = calculateSlaComplianceRate(allTickets);

        stats.put("totalTickets", totalTickets);
        stats.put("closedTickets", closedTickets);
        stats.put("openTickets", totalTickets - closedTickets);
        stats.put("statusDistribution", statusCount);
        stats.put("avgProcessingHours", String.format("%.2f", avgProcessingHours));
        stats.put("slaComplianceRate", String.format("%.1f%%", slaComplianceRate * 100));
        stats.put("ticketsByPriority", countTicketsBySlaPriority(allTickets));

        return stats;
    }

    /**
     * Статистика по агентам
     */
    public Map<String, Object> getAgentPerformanceStats() {
        List<Ticket> allTickets = ticketRepository.findAll();

        Map<String, Object> stats = new HashMap<>();

        // Группировка по агентам
        Map<String, List<Ticket>> ticketsByAgent = allTickets.stream()
                .filter(t -> t.getAgent() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAgent().getName(),
                        Collectors.toList()
                ));

        Map<String, Map<String, Object>> agentStats = new HashMap<>();

        for (Map.Entry<String, List<Ticket>> entry : ticketsByAgent.entrySet()) {
            String agentName = entry.getKey();
            List<Ticket> agentTickets = entry.getValue();

            Map<String, Object> agentData = new HashMap<>();
            agentData.put("totalAssigned", agentTickets.size());
            agentData.put("closed", agentTickets.stream()
                    .filter(t -> "Closed".equals(t.getStatus()))
                    .count());
            agentData.put("inProgress", agentTickets.stream()
                    .filter(t -> "In Progress".equals(t.getStatus()))
                    .count());
            agentData.put("open", agentTickets.stream()
                    .filter(t -> "Open".equals(t.getStatus()))
                    .count());

            // Расчет эффективности
            if (!agentTickets.isEmpty()) {
                double completionRate = (double) agentData.get("closed") / agentTickets.size();
                agentData.put("completionRate", String.format("%.1f%%", completionRate * 100));
            }

            agentStats.put(agentName, agentData);
        }

        stats.put("agentPerformance", agentStats);
        stats.put("topPerformingAgent", findTopPerformingAgent(agentStats));

        return stats;
    }

    /**
     * Еженедельная статистика
     */
    public Map<String, Object> getWeeklyStats() {
        List<Ticket> allTickets = ticketRepository.findAll();

        Map<String, Object> weeklyStats = new HashMap<>();

        // Симуляция данных по неделям
        weeklyStats.put("ticketsCreatedThisWeek", allTickets.size() > 10 ? 10 : allTickets.size());
        weeklyStats.put("ticketsResolvedThisWeek", allTickets.stream()
                .filter(t -> "Closed".equals(t.getStatus()))
                .limit(7)
                .count());
        weeklyStats.put("avgResolutionTimeThisWeek", "24.5 часа");
        weeklyStats.put("slaViolationsThisWeek", 2);

        return weeklyStats;
    }

    // Вспомогательные методы
    private double calculateAverageProcessingTime(List<Ticket> tickets) {
        // В реальном проекте здесь будет расчет по датам создания/закрытия
        // Для примера возвращаем фиктивное значение
        return 12.5;
    }

    private double calculateSlaComplianceRate(List<Ticket> tickets) {
        long totalWithSla = tickets.stream()
                .filter(t -> t.getSla() != null)
                .count();

        if (totalWithSla == 0) return 0.0;

        // В реальном проекте проверяем, соблюдены ли сроки
        // Для примера возвращаем 85%
        return 0.85;
    }

    private Map<String, Long> countTicketsBySlaPriority(List<Ticket> tickets) {
        Map<String, Long> priorityCount = new HashMap<>();

        tickets.forEach(ticket -> {
            if (ticket.getSla() != null) {
                int reactionTime = ticket.getSla().getReactionDeadlineHours();
                String priority = "Низкий";
                if (reactionTime <= 2) priority = "Критический";
                else if (reactionTime <= 8) priority = "Высокий";
                else if (reactionTime <= 24) priority = "Средний";

                priorityCount.put(priority, priorityCount.getOrDefault(priority, 0L) + 1);
            } else {
                priorityCount.put("Не определен", priorityCount.getOrDefault("Не определен", 0L) + 1);
            }
        });

        return priorityCount;
    }

    private String findTopPerformingAgent(Map<String, Map<String, Object>> agentStats) {
        return agentStats.entrySet().stream()
                .max((e1, e2) -> {
                    double rate1 = getCompletionRate(e1.getValue());
                    double rate2 = getCompletionRate(e2.getValue());
                    return Double.compare(rate1, rate2);
                })
                .map(Map.Entry::getKey)
                .orElse("Нет данных");
    }

    private double getCompletionRate(Map<String, Object> agentData) {
        try {
            String rateStr = (String) agentData.getOrDefault("completionRate", "0%");
            return Double.parseDouble(rateStr.replace("%", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}