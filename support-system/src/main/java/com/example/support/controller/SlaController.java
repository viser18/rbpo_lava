package com.example.support.controller;

import com.example.support.entity.Sla;
import com.example.support.repository.SlaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/slas")
public class SlaController {

    @Autowired
    private SlaRepository slaRepository;

    @PostMapping
    public ResponseEntity<Sla> createSla(@RequestBody Sla sla) {
        try {
            Sla savedSla = slaRepository.save(sla);
            return ResponseEntity.ok(savedSla);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<Sla> getAllSlas() {
        return slaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sla> getSlaById(@PathVariable Long id) {
        Optional<Sla> sla = slaRepository.findById(id);
        return sla.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSla(@PathVariable Long id) {
        if (slaRepository.existsById(id)) {
            slaRepository.deleteById(id);
            return ResponseEntity.ok("SLA успешно удален");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sla> updateSla(@PathVariable Long id, @RequestBody Sla slaDetails) {
        return slaRepository.findById(id)
                .map(existingSla -> {
                    existingSla.setReactionDeadlineHours(slaDetails.getReactionDeadlineHours());
                    existingSla.setResolutionDeadlineHours(slaDetails.getResolutionDeadlineHours());
                    Sla updatedSla = slaRepository.save(existingSla);
                    return ResponseEntity.ok(updatedSla);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}