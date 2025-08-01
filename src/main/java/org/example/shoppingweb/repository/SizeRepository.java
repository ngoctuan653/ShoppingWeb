package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Integer> {
    Optional<Size> findBySizeLabel(String sizeLabel);
    boolean existsBySizeLabel(String sizeLabel);

    Optional<Size> findBySizeLabelIgnoreCase(String sizeLabel);
}
