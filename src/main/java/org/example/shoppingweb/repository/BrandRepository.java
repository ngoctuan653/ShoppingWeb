package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Brand;
import org.example.shoppingweb.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand,Integer> {

}
