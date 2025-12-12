package com.example.support.repository;

import com.example.support.entity.Sla;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaRepository extends JpaRepository<Sla, Long> {
}