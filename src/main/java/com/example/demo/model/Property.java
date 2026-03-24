package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    // APARTMENT, HOUSE, OFFICE
    @Column(nullable = false)
    private String type;

    private double areaSqM;

    private double pricePerMonth;

    // Many properties belong to one landlord
    @ManyToOne
    @JoinColumn(name = "landlord_id", nullable = false)
    private Landlord landlord;

    // One property can have many leases (over time)
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<Lease> leases;

    public Property() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAreaSqM() { return areaSqM; }
    public void setAreaSqM(double areaSqM) { this.areaSqM = areaSqM; }

    public double getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(double pricePerMonth) { this.pricePerMonth = pricePerMonth; }

    public Landlord getLandlord() { return landlord; }
    public void setLandlord(Landlord landlord) { this.landlord = landlord; }

    public List<Lease> getLeases() { return leases; }
    public void setLeases(List<Lease> leases) { this.leases = leases; }
}
