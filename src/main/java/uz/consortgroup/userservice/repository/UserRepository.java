package uz.consortgroup.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id > :lastId ORDER BY u.id ASC")
    Page<User> findUsersByBatch(@Param("lastId") Long lastId, Pageable pageable);

    @Query(nativeQuery = true, value = """
                UPDATE user_schema.users
                SET first_name = COALESCE(:firstName, first_name),
                    middle_name = COALESCE(:middleName, middle_name),
                    last_name = COALESCE(:lastName, last_name),
                    work_place = COALESCE(:workPlace, work_place),
                    email = COALESCE(:email, email),
                    position = COALESCE(:position, position),
                    pinfl = COALESCE(:pinfl, pinfl),
                    updated_at = NOW()
                WHERE id = :id
                RETURNING *
            """)
    Optional<User> updateUserById(@Param("id") long id, @Param("firstName") String firstName,
                                  @Param("middleName") String middleName, @Param("lastName") String lastName,
                                  @Param("workPlace") String workPlace, @Param("email") String email,
                                  @Param("position") String position, @Param("pinfl") String pinfl);
}
