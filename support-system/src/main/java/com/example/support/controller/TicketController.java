package com.example.support.controller;

import com.example.support.entity.*;
import com.example.support.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private SlaRepository slaRepository;

    // УПРОЩЕННЫЙ метод создания тикета - точно работает
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== СОЗДАНИЕ ТИКЕТА ===");
            System.out.println("Получен запрос: " + request);

            // 1. Получаем данные из запроса
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            Object statusObj = request.get("status");
            String status = statusObj != null ? statusObj.toString() : "Open";

            // 2. Получаем userId (может быть Integer или Long)
            Object userIdObj = request.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body("Ошибка: userId не указан");
            }

            Long userId;
            if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                userId = (Long) userIdObj;
            } else {
                return ResponseEntity.badRequest().body("Ошибка: userId должен быть числом");
            }

            // 3. Ищем пользователя
            System.out.println("Ищем пользователя с ID: " + userId);
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Ошибка: пользователь с ID=" + userId + " не найден");
            }
            User user = userOptional.get();
            System.out.println("Найден пользователь: " + user.getName());

            // 4. Создаем тикет
            Ticket ticket = new Ticket();
            ticket.setTitle(title);
            ticket.setDescription(description);
            ticket.setStatus(status);
            ticket.setUser(user);

            // 5. Опциональные поля
            if (request.containsKey("agentId")) {
                Object agentIdObj = request.get("agentId");
                Long agentId = agentIdObj instanceof Integer ? ((Integer) agentIdObj).longValue() : (Long) agentIdObj;
                agentRepository.findById(agentId).ifPresent(ticket::setAgent);
            }

            if (request.containsKey("slaId")) {
                Object slaIdObj = request.get("slaId");
                Long slaId = slaIdObj instanceof Integer ? ((Integer) slaIdObj).longValue() : (Long) slaIdObj;
                slaRepository.findById(slaId).ifPresent(ticket::setSla);
            }

            // 6. Сохраняем
            Ticket savedTicket = ticketRepository.save(ticket);
            System.out.println("Тикет сохранен с ID: " + savedTicket.getId());

            return ResponseEntity.ok(savedTicket);

        } catch (Exception e) {
            System.err.println("ОШИБКА при создании тикета:");
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    // Тестовый метод для проверки
    @PostMapping("/test")
    public ResponseEntity<String> testCreate() {
        try {
            // Проверяем, есть ли пользователи
            long userCount = userRepository.count();
            if (userCount == 0) {
                return ResponseEntity.ok("Тест пройден, но нет пользователей. Создайте админа.");
            }

            // Создаем тестовый тикет
            User user = userRepository.findAll().get(0);
            Ticket ticket = new Ticket();
            ticket.setTitle("Тестовый тикет");
            ticket.setDescription("Создан автоматически для теста");
            ticket.setStatus("Open");
            ticket.setUser(user);

            Ticket saved = ticketRepository.save(ticket);
            return ResponseEntity.ok("Тест пройден! Создан тикет ID: " + saved.getId());

        } catch (Exception e) {
            return ResponseEntity.ok("Тест не пройден: " + e.getMessage());
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