package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    long countByRoleId(Integer roleId);

    @EntityGraph(attributePaths = "role")
    Optional<User> findWithRoleByEmail(String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findById(Integer id);

}

