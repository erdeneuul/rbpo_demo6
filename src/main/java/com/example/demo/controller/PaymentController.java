package com.example.demo.controller;

import com.example.demo.model.Lease;
import com.example.demo.model.Payment;
import com.example.demo.repository.LeaseRepository;
import com.example.demo.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository repo;
    private final LeaseRepository leaseRepo;

    public PaymentController(PaymentRepository repo, LeaseRepository leaseRepo) {
        this.repo = repo;
        this.leaseRepo = leaseRepo;
    }

    @GetMapping
    public List<Payment> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-lease/{leaseId}")
    public List<Payment> getByLease(@PathVariable Long leaseId) {
        return repo.findByLeaseId(leaseId);
    }

    @GetMapping("/by-status/{status}")
    public List<Payment> getByStatus(@PathVariable String status) {
        return repo.findByStatus(status);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody PaymentRequest request) {
        Lease lease = leaseRepo.findById(request.leaseId)
                .orElseThrow(() -> new RuntimeException("Договор не найден: " + request.leaseId));

        Payment payment = new Payment();
        payment.setLease(lease);
        payment.setAmount(request.amount);
        payment.setPaymentDate(request.paymentDate);
        payment.setStatus(request.status != null ? request.status : "PENDING");

        return ResponseEntity.ok(repo.save(payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable Long id, @RequestBody PaymentRequest request) {
        return repo.findById(id).map(existing -> {
            existing.setAmount(request.amount);
            existing.setPaymentDate(request.paymentDate);
            existing.setStatus(request.status);
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok("Платёж удалён");
    }

    // Simple DTO
    public static class PaymentRequest {
        public Long leaseId;
        public double amount;
        public java.time.LocalDate paymentDate;
        public String status;
    }
}
