package com.example.support.controller;

import com.example.support.entity.Ticket;
import com.example.support.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketOperationsController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private SlaService slaService;

    @Autowired
    private TicketResolutionService resolutionService;

    @Autowired
    private BatchTicketService batchTicketService;

    @PostMapping("/create-with-agent/{userId}")
    public ResponseEntity<Ticket> createTicketWithAgent(
            @PathVariable Long userId,
            @RequestParam String title,
            @RequestParam String description) {
        Ticket ticket = ticketService.createTicketWithAgentAssignment(userId, title, description);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}/reassign/{newAgentId}")
    public ResponseEntity<Ticket> reassignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long newAgentId,
            @RequestParam String reason) {
        Ticket ticket = agentService.reassignTicket(ticketId, newAgentId, reason);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}/escalate")
    public ResponseEntity<Ticket> escalateTicket(
            @PathVariable Long ticketId,
            @RequestParam String reason) {
        Ticket ticket = slaService.escalateTicket(ticketId, reason);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}/close")
    public ResponseEntity<Ticket> closeTicket(
            @PathVariable Long ticketId,
            @RequestParam String solution,
            @RequestParam(required = false) Long agentId) {
        Ticket ticket = resolutionService.closeTicketWithSolution(ticketId, solution, agentId);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/batch-update-sla")
    public ResponseEntity<List<Ticket>> updateOldTicketsSla(
            @RequestParam int daysOld,
            @RequestParam Long slaId) {
        List<Ticket> tickets = batchTicketService.updateSlaForOldTickets(daysOld, slaId);
        return ResponseEntity.ok(tickets);
    }
}