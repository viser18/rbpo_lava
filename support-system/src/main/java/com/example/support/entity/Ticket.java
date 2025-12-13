package com.example.support.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status = "Open";

    @Column(columnDefinition = "TEXT")
    private String solution;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Agent agent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sla_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sla sla;

    // КОНСТРУКТОРЫ
    public Ticket() {
        // Пустой конструктор для JPA
    }

    // Конструктор для TicketService
    public Ticket(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.status = "Open";
    }

    // ВАЖНО: Полный конструктор с 6 параметрами (для обратной совместимости)
    public Ticket(String title, String description, String status, String solution,
                  User user, Agent agent, Sla sla) {
        this.title = title;
        this.description = description;
        this.status = status != null ? status : "Open";
        this.solution = solution;
        this.user = user;
        this.agent = agent;
        this.sla = sla;
    }

    // --- ГЕТТЕРЫ (ОБЯЗАТЕЛЬНО!) ---
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getSolution() {
        return solution;
    }

    public User getUser() {
        return user;
    }

    public Agent getAgent() {
        return agent;
    }

    public Sla getSla() {
        return sla;
    }

    // --- СЕТТЕРЫ (ОБЯЗАТЕЛЬНО!) ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void setSla(Sla sla) {
        this.sla = sla;
    }
}