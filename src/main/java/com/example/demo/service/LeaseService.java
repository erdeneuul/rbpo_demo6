package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Business operations for the rental system.
 * All multi-entity operations are wrapped in @Transactional.
 */
@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;
    private final PaymentRepository paymentRepository;

    public LeaseService(LeaseRepository leaseRepository,
                        PropertyRepository propertyRepository,
                        TenantRepository tenantRepository,
                        PaymentRepository paymentRepository) {
        this.leaseRepository = leaseRepository;
        this.propertyRepository = propertyRepository;
        this.tenantRepository = tenantRepository;
        this.paymentRepository = paymentRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // BUSINESS OPERATION 1: Create lease (with overlap check)
    // Touches: Property, Tenant, Lease — wrapped in one transaction
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public Lease createLease(Long propertyId, Long tenantId,
                             LocalDate startDate, LocalDate endDate) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Объект не найден: " + propertyId));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Арендатор не найден: " + tenantId));

        // Check for overlapping active leases for this property
        List<Lease> overlapping = leaseRepository.findOverlappingLeases(propertyId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException(
                "Объект уже арендуется в указанный период. Пересечения активных договоров запрещены."
            );
        }

        Lease lease = new Lease();
        lease.setProperty(property);
        lease.setTenant(tenant);
        lease.setStartDate(startDate);
        lease.setEndDate(endDate);
        lease.setStatus("ACTIVE");

        return leaseRepository.save(lease);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSINESS OPERATION 2: Cancel lease + mark payments as OVERDUE
    // Touches: Lease, Payment — wrapped in one transaction
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public Lease cancelLease(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Договор не найден: " + leaseId));

        if (!"ACTIVE".equals(lease.getStatus())) {
            throw new RuntimeException("Можно отменить только активный договор.");
        }

        // Mark all pending payments as OVERDUE
        List<Payment> payments = paymentRepository.findByLeaseId(leaseId);
        for (Payment p : payments) {
            if ("PENDING".equals(p.getStatus())) {
                p.setStatus("OVERDUE");
                paymentRepository.save(p);
            }
        }

        lease.setStatus("CANCELLED");
        return leaseRepository.save(lease);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSINESS OPERATION 3: Complete lease (mark as COMPLETED)
    // Touches: Lease, Payment — wrapped in one transaction
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public Lease completeLease(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Договор не найден: " + leaseId));

        if (!"ACTIVE".equals(lease.getStatus())) {
            throw new RuntimeException("Можно завершить только активный договор.");
        }

        // Mark all pending payments as OVERDUE on completion
        List<Payment> payments = paymentRepository.findByLeaseId(leaseId);
        for (Payment p : payments) {
            if ("PENDING".equals(p.getStatus())) {
                p.setStatus("OVERDUE");
                paymentRepository.save(p);
            }
        }

        lease.setStatus("COMPLETED");
        return leaseRepository.save(lease);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSINESS OPERATION 4: Pay all pending payments for a lease
    // Touches: Lease, Payment — wrapped in one transaction
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public String payAllPending(Long leaseId) {
        leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Договор не найден: " + leaseId));

        List<Payment> payments = paymentRepository.findByLeaseId(leaseId);
        long count = 0;
        for (Payment p : payments) {
            if ("PENDING".equals(p.getStatus()) || "OVERDUE".equals(p.getStatus())) {
                p.setStatus("PAID");
                paymentRepository.save(p);
                count++;
            }
        }
        return "Оплачено платежей: " + count;
    }

    // ─────────────────────────────────────────────────────────────
    // BUSINESS OPERATION 5: Get full info about a lease
    // Touches: Lease, Property, Tenant, Payment — multi-entity query
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public String getLeaseInfo(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Договор не найден: " + leaseId));

        List<Payment> payments = paymentRepository.findByLeaseId(leaseId);
        long paidCount = payments.stream().filter(p -> "PAID".equals(p.getStatus())).count();
        long pendingCount = payments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        double totalPaid = payments.stream()
                .filter(p -> "PAID".equals(p.getStatus()))
                .mapToDouble(Payment::getAmount).sum();

        return String.format(
            "Договор #%d | Объект: %s | Арендатор: %s | Период: %s — %s | Статус: %s | " +
            "Платежей оплачено: %d | Ожидают оплаты: %d | Итого оплачено: %.2f руб.",
            lease.getId(),
            lease.getProperty().getAddress(),
            lease.getTenant().getFullName(),
            lease.getStartDate(), lease.getEndDate(),
            lease.getStatus(),
            paidCount, pendingCount, totalPaid
        );
    }
}
