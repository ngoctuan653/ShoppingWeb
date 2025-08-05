package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Order;
import org.example.shoppingweb.entity.Orderdetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<Orderdetail, Integer> {
    List<Orderdetail> findByOrder(Order order);

 // Lấy 5 sản phẩm bán chạy nhất
    @Query("SELECT od.product, SUM(od.quantity) as totalSold FROM Orderdetail od GROUP BY od.product ORDER BY totalSold DESC")
    List<Object[]> findTop5ProductsByTotalSold();

    List<Orderdetail> findByOrder_IdAndOrder_User_Id(Integer orderId, Integer userId);
    boolean existsById(Integer id);

    Optional<Orderdetail> findById(Integer id);
//    List<Orderdetail> findAll();

}
