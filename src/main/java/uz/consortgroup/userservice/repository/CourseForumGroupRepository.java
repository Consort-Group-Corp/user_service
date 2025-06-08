package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.userservice.entity.CourseForumGroup;

import java.util.UUID;

public interface CourseForumGroupRepository extends JpaRepository<CourseForumGroup, UUID> {
}
