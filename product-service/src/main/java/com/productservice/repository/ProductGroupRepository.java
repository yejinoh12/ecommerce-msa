package com.productservice.repository;

import com.productservice.domain.ProductGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {

    @EntityGraph(attributePaths = {"products"})
    List<ProductGroup> findAll();
}

