package com.example.support.entity;

import jakarta.persistence.*;

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
    private String status; // "Open", "In Progress", "Closed"

    @Column(columnDefinition = "TEXT")
    private String solution; // решение при закрытии

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "sla_id")
    private Sla sla;

    // Конструктор по умолчанию (как в похожем проекте)
    public Ticket() {
    }

    // Конструктор с основными параметрами (как в Ride.java похожего проекта)
    public Ticket(String title, String description, String status, User user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.user = user;
    }

    // Полный конструктор (опционально)
    public Ticket(String title, String description, String status, String solution,
                  User user, Agent agent, Sla sla) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.solution = solution;
        this.user = user;
        this.agent = agent;
        this.sla = sla;
    }

    // Геттеры и сеттеры (как в похожем проекте)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Sla getSla() {
        return sla;
    }

    public void setSla(Sla sla) {
        this.sla = sla;
    }
}