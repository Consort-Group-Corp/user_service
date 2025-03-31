package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.VerificationCode;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    @Query(value = "SELECT * FROM verification_code WHERE id > :lastId ORDER BY id ASC LIMIT :size", nativeQuery = true)
    List<VerificationCode> findCodesBatch(@Param("lastId") Long lastId,
                                          @Param("size") int size);

    @Query("SELECT v FROM VerificationCode v WHERE v.user.id = :userId ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationCode> findLastActiveCodeByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE VerificationCode v SET v.status = 'INACTIVE' WHERE v.user.id = :userId AND v.status = 'ACTIVE'")
    int deactivateUserCodes(@Param("userId") Long userId);
}
