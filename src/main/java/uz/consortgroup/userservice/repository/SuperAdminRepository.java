package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.SuperAdmin;
import uz.consortgroup.userservice.entity.enumeration.UserRole;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, UUID> {
    Optional<SuperAdmin> findUserByEmail(String email);

    @Query("SELECT a.id FROM SuperAdmin a WHERE a.userRole = :userRole")
    Stream<UUID> findIdsByRole(@Param("userRole") UserRole userRole);
}
