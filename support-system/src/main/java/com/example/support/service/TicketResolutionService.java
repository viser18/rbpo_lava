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
public class TicketResolutionService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AgentRepository agentRepository;

    public Ticket closeTicketWithSolution(Long ticketId, String solution, Long closingAgentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));

        Agent closingAgent = ticket.getAgent();
        if (closingAgentId != null) {
            closingAgent = agentRepository.findById(closingAgentId)
                    .orElse(ticket.getAgent());
        }

        ticket.setStatus("Closed");
        ticket.setSolution(solution);
        ticket.setAgent(closingAgent);

        String updatedDescription = ticket.getDescription() +
                "\n\n[ЗАКРЫТИЕ] " + new Date() +
                "\nЗакрыл: " + closingAgent.getName() +
                "\nРешение: " + solution;

        ticket.setDescription(updatedDescription);

        return ticketRepository.save(ticket);
    }
}