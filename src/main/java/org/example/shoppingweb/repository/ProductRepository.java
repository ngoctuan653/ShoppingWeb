package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByStatusAndProductNameContainingIgnoreCase(String status, String keyword);

    List<Product> findByStatus(String status);
    
 // Đếm số sản phẩm có status = 'Active'
    long countByStatus(String status);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productSizes ps LEFT JOIN FETCH ps.size")
    Page<Product> findAllWithSizes(Pageable pageable);

}
