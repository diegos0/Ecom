package com.ecom.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 500)
    private String title;

    @Column(length = 500)
    private String description;

    private Double price;

    private int stock;

    private String image;


    private int discount;

    private Double discountPrice;

    @ManyToOne
    @JoinColumn(name = "category_id") // FK en la tabla Product
    private Category category; // ✅ Relación correcta

    @Column(columnDefinition = "BIT", nullable = false)
    private boolean active;

}
