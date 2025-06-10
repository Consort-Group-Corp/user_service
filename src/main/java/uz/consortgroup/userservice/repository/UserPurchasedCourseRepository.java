package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;

import java.util.UUID;

@Repository
public interface UserPurchasedCourseRepository extends JpaRepository<UserPurchasedCourse, UUID> {
}
