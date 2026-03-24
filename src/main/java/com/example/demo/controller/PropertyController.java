package com.example.demo.controller;

import com.example.demo.model.Landlord;
import com.example.demo.model.Property;
import com.example.demo.repository.LandlordRepository;
import com.example.demo.repository.PropertyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyRepository repo;
    private final LandlordRepository landlordRepo;

    public PropertyController(PropertyRepository repo, LandlordRepository landlordRepo) {
        this.repo = repo;
        this.landlordRepo = landlordRepo;
    }

    @GetMapping
    public List<Property> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-landlord/{landlordId}")
    public List<Property> getByLandlord(@PathVariable Long landlordId) {
        return repo.findByLandlordId(landlordId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody PropertyRequest request) {
        Landlord landlord = landlordRepo.findById(request.landlordId)
                .orElseThrow(() -> new RuntimeException("Арендодатель не найден: " + request.landlordId));

        Property property = new Property();
        property.setAddress(request.address);
        property.setType(request.type);
        property.setAreaSqM(request.areaSqM);
        property.setPricePerMonth(request.pricePerMonth);
        property.setLandlord(landlord);

        return ResponseEntity.ok(repo.save(property));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> update(@PathVariable Long id, @RequestBody PropertyRequest request) {
        return repo.findById(id).map(existing -> {
            Landlord landlord = landlordRepo.findById(request.landlordId)
                    .orElseThrow(() -> new RuntimeException("Арендодатель не найден"));
            existing.setAddress(request.address);
            existing.setType(request.type);
            existing.setAreaSqM(request.areaSqM);
            existing.setPricePerMonth(request.pricePerMonth);
            existing.setLandlord(landlord);
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok("Объект недвижимости удалён");
    }

    // Simple DTO for receiving property data with landlordId
    public static class PropertyRequest {
        public String address;
        public String type;
        public double areaSqM;
        public double pricePerMonth;
        public Long landlordId;
    }
}
