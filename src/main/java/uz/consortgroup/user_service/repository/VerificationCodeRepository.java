package uz.consortgroup.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.user_service.entity.VerificationCode;
import uz.consortgroup.user_service.entity.enumeration.VerificationCodeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    @Query("SELECT v FROM VerificationCode v WHERE v.id > :lastId ORDER BY v.id ASC")
    List<VerificationCode> findCodesBatch(@Param("lastId") Long lastId, @Param("size") int size);

    @Query("SELECT v FROM VerificationCode v WHERE v.user.id = :userId ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationCode> findLastActiveCodeByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE VerificationCode v SET v.attempts = v.attempts + 1, v.updatedAt = :now WHERE v.id = :codeId")
    void incrementAttempts(@Param("codeId") Long codeId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VerificationCode v SET " +
            "v.status = :status, " +
            "v.updatedAt = :now, " +
            "v.usedAt = CASE WHEN :status = uz.consortgroup.user_service.entity.enumeration.VerificationCodeStatus.USED THEN :now ELSE v.usedAt END " +
            "WHERE v.id = :codeId")
    void updateStatus(@Param("codeId") Long codeId,
                      @Param("status") VerificationCodeStatus status,
                      @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VerificationCode v SET " +
            "v.status = :status, " +
            "v.updatedAt = :now " +
            "WHERE v.status = 'ACTIVE' AND v.expiresAt <= :now")
    int updateExpiredCodes(@Param("status") VerificationCodeStatus status,
                           @Param("now") LocalDateTime now);
}
