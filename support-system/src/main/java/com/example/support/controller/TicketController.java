package com.example.support.controller;

import com.example.support.entity.Ticket;
import com.example.support.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        try {
            Ticket savedTicket = ticketRepository.save(ticket);
            return ResponseEntity.ok(savedTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        return ticket.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTicket(@PathVariable Long id) {
        if (ticketRepository.existsById(id)) {
            ticketRepository.deleteById(id);
            return ResponseEntity.ok("Тикет успешно удален");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody Ticket ticketDetails) {
        return ticketRepository.findById(id)
                .map(existingTicket -> {
                    // Обновляем только нужные поля
                    if (ticketDetails.getTitle() != null) {
                        existingTicket.setTitle(ticketDetails.getTitle());
                    }
                    if (ticketDetails.getDescription() != null) {
                        existingTicket.setDescription(ticketDetails.getDescription());
                    }
                    if (ticketDetails.getStatus() != null) {
                        existingTicket.setStatus(ticketDetails.getStatus());
                    }
                    if (ticketDetails.getSolution() != null) {
                        existingTicket.setSolution(ticketDetails.getSolution());
                    }
                    Ticket updatedTicket = ticketRepository.save(existingTicket);
                    return ResponseEntity.ok(updatedTicket);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}