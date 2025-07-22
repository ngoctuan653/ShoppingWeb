package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Product;
import org.example.shoppingweb.entity.Productsize;
import org.example.shoppingweb.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductSizeRepository extends JpaRepository<Productsize,Integer> {
    List<Productsize> findByProductId(int productId);
    Optional<Productsize> findByProductAndSize(Product product, Size size);
    List<Productsize> findByProduct(Product product);
    void deleteByProduct(Product product);
}
