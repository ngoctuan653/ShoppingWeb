package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
