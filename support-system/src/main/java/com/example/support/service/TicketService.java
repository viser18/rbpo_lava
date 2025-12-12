package com.example.support.service;

import com.example.support.entity.*;
import com.example.support.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private SlaRepository slaRepository;

    public Ticket createTicketWithAgentAssignment(Long userId, String title, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // ИСПРАВЛЕННЫЙ КОД
        List<Agent> agents = agentRepository.findAll();
        if (agents.isEmpty()) {
            throw new RuntimeException("Нет доступных агентов");
        }
        Agent agent = agents.get(0);

        List<Sla> slas = slaRepository.findAll();
        Sla sla;
        if (slas.isEmpty()) {
            sla = slaRepository.save(new Sla(24, 72));
        } else {
            sla = slas.get(0);
        }

        Ticket ticket = new Ticket(title, description, "Open", user);
        ticket.setAgent(agent);
        ticket.setSla(sla);

        return ticketRepository.save(ticket);
    }
}