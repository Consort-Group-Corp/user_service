package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.Password;

import java.util.UUID;

@Repository
public interface PasswordRepository extends JpaRepository<Password, UUID> {
}
