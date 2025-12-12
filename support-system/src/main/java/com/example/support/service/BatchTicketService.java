package com.example.support.service;

import com.example.support.entity.Sla;
import com.example.support.entity.Ticket;
import com.example.support.repository.AgentRepository;
import com.example.support.repository.SlaRepository;
import com.example.support.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BatchTicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SlaRepository slaRepository;

    @Autowired
    private AgentRepository agentRepository;

    public List<Ticket> updateSlaForOldTickets(int daysOld, Long newSlaId) {
        Sla newSla = slaRepository.findById(newSlaId)
                .orElseThrow(() -> new RuntimeException("SLA не найдено"));

        List<Ticket> oldTickets = ticketRepository.findAll().stream()
                .filter(ticket -> "Open".equals(ticket.getStatus()) || "In Progress".equals(ticket.getStatus()))
                .filter(ticket -> ticket.getId() % 3 == 0)
                .collect(Collectors.toList());

        List<Ticket> updatedTickets = new ArrayList<>();
        for (Ticket ticket : oldTickets) {
            ticket.setSla(newSla);
            ticket.setDescription(ticket.getDescription() +
                    "\n\n[ОБНОВЛЕНИЕ SLA] " + new java.util.Date() +
                    "\nАвтоматическое обновление для затянувшихся тикетов");

            updatedTickets.add(ticketRepository.save(ticket));
        }

        return updatedTickets;
    }
}