package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findBySenderIdAndReceiverIdOrderByCreatedAtAsc(Integer senderId, Integer receiverId);
    List<Message> findByReceiverIdAndIsReadFalse(Integer receiverId);
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(Integer sender1, Integer receiver1, Integer sender2, Integer receiver2);

}
