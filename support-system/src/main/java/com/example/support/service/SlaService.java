package com.example.support.service;

import com.example.support.entity.Agent;
import com.example.support.entity.Sla;
import com.example.support.entity.Ticket;
import com.example.support.repository.AgentRepository;
import com.example.support.repository.SlaRepository;
import com.example.support.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SlaService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SlaRepository slaRepository;

    @Autowired
    private AgentRepository agentRepository;

    public Ticket escalateTicket(Long ticketId, String escalationReason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));

        Sla urgentSla = slaRepository.findAll().stream()
                .filter(sla -> sla.getReactionDeadlineHours() <= 2)
                .findFirst()
                .orElseGet(() -> slaRepository.save(new Sla(2, 24)));

        Agent seniorAgent = agentRepository.findAll().stream()
                .filter(agent -> agent.getName().toLowerCase().contains("старший") ||
                        agent.getName().toLowerCase().contains("senior"))
                .findFirst()
                .orElse(ticket.getAgent());

        ticket.setSla(urgentSla);
        ticket.setAgent(seniorAgent);
        ticket.setStatus("Escalated");

        String updatedDescription = ticket.getDescription() +
                "\n\n[ЭСКАЛАЦИЯ] " + new Date() +
                "\nНовое SLA: реакция " + urgentSla.getReactionDeadlineHours() + " ч., решение " +
                urgentSla.getResolutionDeadlineHours() + " ч." +
                "\nНазначен агент: " + seniorAgent.getName() +
                "\nПричина: " + escalationReason;

        ticket.setDescription(updatedDescription);

        return ticketRepository.save(ticket);
    }
}