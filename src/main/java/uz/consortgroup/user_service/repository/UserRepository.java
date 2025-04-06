package uz.consortgroup.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.entity.enumeration.UserRole;
import uz.consortgroup.user_service.entity.enumeration.UserStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id > :lastId ORDER BY u.id ASC")
    List<User> findUsersByBatch(@Param("lastId") Long lastId, @Param("size") int size);

    @Query(nativeQuery = true, value = """
                UPDATE user_schema.users
                SET last_name = COALESCE(:lastName, last_name),
                    first_name = COALESCE(:firstName, first_name),
                    middle_name = COALESCE(:middleName, middle_name),
                    born_date = COALESCE(:bornDate, born_date), 
                    phone_number = COALESCE(:phoneNumber, phone_number),
                    work_place = COALESCE(:workPlace, work_place),
                    email = COALESCE(:email, email),
                    position = COALESCE(:position, position),
                    pinfl = COALESCE(:pinfl, pinfl),
                    role = COALESCE(:role, role),
                    updated_at = NOW()
                WHERE id = :id
                RETURNING *
            """)
    Optional<User> updateUserById(@Param("id") long id, @Param("lastName") String lastName,
                                  @Param("firstName") String firstName, @Param("middleName") String middleName,
                                  @Param("bornDate") LocalDate bornDate, @Param("phoneNumber") String phoneNumber,
                                  @Param("workPlace") String workPlace, @Param("email") String email,
                                  @Param("position") String position, @Param("pinfl") String pinfl, @Param("role") String role);

    @Modifying
    @Query("UPDATE User u SET u.isVerified = :verificationStatus, u.status = :status WHERE u.id = :userId")
    void updateVerificationStatus(@Param("userId") Long userId,
                                  @Param("verificationStatus") boolean verificationStatus,
                                  @Param("status") UserStatus status);

    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateUserRole(@Param("userId") Long userId, @Param("role") UserRole role);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
    void changePassword(@Param("userId") Long userId, @Param("password") String password);
}
