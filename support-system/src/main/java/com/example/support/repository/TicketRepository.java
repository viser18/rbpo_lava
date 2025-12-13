package com.example.support.repository;

import com.example.support.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(String status);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByAgentId(Long agentId);
}