package com.example.support.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sla")
public class Sla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reaction_deadline_hours", nullable = false)
    private int reactionDeadlineHours;

    @Column(name = "resolution_deadline_hours", nullable = false)
    private int resolutionDeadlineHours;

    // Конструктор по умолчанию
    public Sla() {
    }

    // Конструктор с параметрами
    public Sla(int reactionDeadlineHours, int resolutionDeadlineHours) {
        this.reactionDeadlineHours = reactionDeadlineHours;
        this.resolutionDeadlineHours = resolutionDeadlineHours;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getReactionDeadlineHours() { return reactionDeadlineHours; }
    public void setReactionDeadlineHours(int reactionDeadlineHours) {
        this.reactionDeadlineHours = reactionDeadlineHours;
    }
    public int getResolutionDeadlineHours() { return resolutionDeadlineHours; }
    public void setResolutionDeadlineHours(int resolutionDeadlineHours) {
        this.resolutionDeadlineHours = resolutionDeadlineHours;
    }
}