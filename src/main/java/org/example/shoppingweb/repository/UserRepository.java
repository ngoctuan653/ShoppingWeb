package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
<<<<<<< HEAD
=======
    User findByUsername(String username);
    boolean existsByEmail(String email);

>>>>>>> PhamVietHoang
}
