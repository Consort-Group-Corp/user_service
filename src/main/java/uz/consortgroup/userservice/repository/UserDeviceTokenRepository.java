package uz.consortgroup.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.userservice.entity.UserDeviceToken;

import java.util.List;
import java.util.UUID;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, UUID> {
    Page<UserDeviceToken> findByIsActiveTrue(Pageable pageable);
    Page<UserDeviceToken> findByUserIdAndIsActiveTrue(UUID userId, Pageable pageable);
    List<UserDeviceToken> findByFcmTokenAndIsActiveTrue(String fcmToken);
    List<UserDeviceToken> findAllByUserIdInAndIsActiveTrue(List<UUID> userIds);
}
