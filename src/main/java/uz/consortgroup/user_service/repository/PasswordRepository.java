package uz.consortgroup.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.user_service.entity.Password;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {
}
