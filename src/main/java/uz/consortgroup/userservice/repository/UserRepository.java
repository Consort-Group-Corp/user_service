package uz.consortgroup.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.userservice.entity.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@Param("email") String email);


    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id > :lastId ORDER BY u.id ASC")
    List<User> findUsersByBatch(@Param("lastId") UUID lastId, @Param("size") int size);

    @Query(nativeQuery = true, value = """
     UPDATE user_schema.users
     SET last_name = COALESCE(:lastName, last_name),
         first_name = COALESCE(:firstName, first_name),
         middle_name = COALESCE(:middleName, middle_name),
         born_date = COALESCE(:bornDate, born_date), 
         phone_number = COALESCE(:phoneNumber, phone_number),
         work_place = COALESCE(:workPlace, work_place),
         position = COALESCE(:position, position),
         pinfl = COALESCE(:pinfl, pinfl),
         updated_at = NOW()
     WHERE id = :id
     RETURNING *
""")
    Optional<User> updateUserProfileById(@Param("id") UUID id, @Param("lastName") String lastName,
                                         @Param("firstName") String firstName, @Param("middleName") String middleName,
                                         @Param("bornDate") LocalDate bornDate, @Param("phoneNumber") String phoneNumber,
                                         @Param("workPlace") String workPlace, @Param("position") String position,
                                         @Param("pinfl") String pinfl);


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
    Optional<User> updateUserById(@Param("id") UUID id, @Param("lastName") String lastName,
                                  @Param("firstName") String firstName, @Param("middleName") String middleName,
                                  @Param("bornDate") LocalDate bornDate, @Param("phoneNumber") String phoneNumber,
                                  @Param("workPlace") String workPlace, @Param("email") String email,
                                  @Param("position") String position, @Param("pinfl") String pinfl, @Param("role") String role);

    @Modifying
    @Query("UPDATE User u SET u.isVerified = :verificationStatus, u.status = :status WHERE u.id = :userId")
    void updateVerificationStatus(@Param("userId") UUID userId,
                                  @Param("verificationStatus") boolean verificationStatus,
                                  @Param("status") UserStatus status);


    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    int updateUserRole(@Param("userId") UUID userId, @Param("role") UserRole role);


    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
    void changePassword(@Param("userId") UUID userId, @Param("password") String password);

    Optional<User> findByOneIdUserId(String oneIdUserId);

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    UUID findUserIdByEmail(@Param("email") String email);

    Page<User> findByEmailIgnoreCaseOrPinfl(String email, String pinfl, Pageable pageable);

    Optional<User> findUserByPinfl(String pinfl);

    List<User> findByIdIn(List<UUID> ids);


    List<User> findByEmailIn(Collection<String> emails);

    List<User> findByPinflIn(Collection<String> pinfls);

    boolean existsByIdAndRole(UUID id, UserRole role);

    @Query("SELECT CASE WHEN EXISTS (" +
            "    SELECT 1 FROM User u WHERE u.id = :userId AND u.status = 'BLOCKED'" +
            ") THEN true ELSE false END")
    boolean isUserBlocked(UUID userId);
}
