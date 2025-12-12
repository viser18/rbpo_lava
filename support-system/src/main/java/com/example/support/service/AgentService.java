package com.example.support.service;

import com.example.support.entity.Agent;
import com.example.support.entity.Ticket;
import com.example.support.repository.AgentRepository;
import com.example.support.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class AgentService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AgentRepository agentRepository;

    public Ticket reassignTicket(Long ticketId, Long newAgentId, String reassignReason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));

        Agent newAgent = agentRepository.findById(newAgentId)
                .orElseThrow(() -> new RuntimeException("Агент не найден"));

        Agent oldAgent = ticket.getAgent();
        ticket.setAgent(newAgent);
        ticket.setStatus("Reassigned");

        String newDescription = ticket.getDescription() +
                "\n\n[ПЕРЕДАЧА] " + new Date() +
                "\nОт агента: " + (oldAgent != null ? oldAgent.getName() : "Не назначен") +
                "\nАгенту: " + newAgent.getName() +
                "\nПричина: " + reassignReason;

        ticket.setDescription(newDescription);

        return ticketRepository.save(ticket);
    }
}