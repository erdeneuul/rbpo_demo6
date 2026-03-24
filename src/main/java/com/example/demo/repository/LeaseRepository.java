package com.example.demo.repository;

import com.example.demo.model.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {

    List<Lease> findByPropertyId(Long propertyId);

    List<Lease> findByTenantId(Long tenantId);

    // Check if an ACTIVE lease for the same property overlaps with given dates
    @Query("SELECT l FROM Lease l WHERE l.property.id = :propertyId " +
           "AND l.status = 'ACTIVE' " +
           "AND l.startDate <= :endDate " +
           "AND l.endDate >= :startDate")
    List<Lease> findOverlappingLeases(@Param("propertyId") Long propertyId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}
