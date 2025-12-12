package com.example.support.controller;

import com.example.support.entity.Agent;
import com.example.support.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agents")
public class AgentController {

    @Autowired
    private AgentRepository agentRepository;

    @PostMapping
    public ResponseEntity<Agent> createAgent(@RequestBody Agent agent) {
        try {
            Agent savedAgent = agentRepository.save(agent);
            return ResponseEntity.ok(savedAgent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agent> getAgentById(@PathVariable Long id) {
        Optional<Agent> agent = agentRepository.findById(id);
        return agent.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAgent(@PathVariable Long id) {
        if (agentRepository.existsById(id)) {
            agentRepository.deleteById(id);
            return ResponseEntity.ok("Агент успешно удален");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agent> updateAgent(@PathVariable Long id, @RequestBody Agent agentDetails) {
        return agentRepository.findById(id)
                .map(existingAgent -> {
                    existingAgent.setName(agentDetails.getName());
                    Agent updatedAgent = agentRepository.save(existingAgent);
                    return ResponseEntity.ok(updatedAgent);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}