package com.example.demo.controller;

import com.example.demo.model.Landlord;
import com.example.demo.repository.LandlordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/landlords")
public class LandlordController {

    private final LandlordRepository repo;

    public LandlordController(LandlordRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Landlord> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Landlord> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Landlord create(@RequestBody Landlord landlord) {
        return repo.save(landlord);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Landlord> update(@PathVariable Long id, @RequestBody Landlord updated) {
        return repo.findById(id).map(existing -> {
            existing.setFullName(updated.getFullName());
            existing.setEmail(updated.getEmail());
            existing.setPhone(updated.getPhone());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok("Арендодатель удалён");
    }
}
