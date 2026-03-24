package com.example.demo.controller;

import com.example.demo.model.Lease;
import com.example.demo.repository.LeaseRepository;
import com.example.demo.service.LeaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/leases")
public class LeaseController {

    private final LeaseRepository repo;
    private final LeaseService leaseService;

    public LeaseController(LeaseRepository repo, LeaseService leaseService) {
        this.repo = repo;
        this.leaseService = leaseService;
    }

    @GetMapping
    public List<Lease> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lease> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-property/{propertyId}")
    public List<Lease> getByProperty(@PathVariable Long propertyId) {
        return repo.findByPropertyId(propertyId);
    }

    @GetMapping("/by-tenant/{tenantId}")
    public List<Lease> getByTenant(@PathVariable Long tenantId) {
        return repo.findByTenantId(tenantId);
    }

    // Business Operation 1: Create lease with overlap validation
    // POST /leases/create?propertyId=1&tenantId=1&startDate=2025-07-01&endDate=2025-12-31
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestParam Long propertyId,
                                         @RequestParam Long tenantId,
                                         @RequestParam LocalDate startDate,
                                         @RequestParam LocalDate endDate) {
        try {
            return ResponseEntity.ok(leaseService.createLease(propertyId, tenantId, startDate, endDate));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Business Operation 2: Cancel lease
    // POST /leases/1/cancel
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Object> cancel(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(leaseService.cancelLease(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Business Operation 3: Complete lease
    // POST /leases/1/complete
    @PostMapping("/{id}/complete")
    public ResponseEntity<Object> complete(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(leaseService.completeLease(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Business Operation 4: Pay all pending payments for a lease
    // POST /leases/1/pay-all
    @PostMapping("/{id}/pay-all")
    public ResponseEntity<String> payAll(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(leaseService.payAllPending(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Business Operation 5: Get full lease info (multi-entity summary)
    // GET /leases/1/info
    @GetMapping("/{id}/info")
    public ResponseEntity<String> getInfo(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(leaseService.getLeaseInfo(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok("Договор аренды удалён");
    }
}
