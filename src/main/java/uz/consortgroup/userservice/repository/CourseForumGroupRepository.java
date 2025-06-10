package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.userservice.entity.CourseForumGroup;

import java.util.Optional;
import java.util.UUID;

public interface CourseForumGroupRepository extends JpaRepository<CourseForumGroup, UUID> {
    Optional<CourseForumGroup> findByCourseId(UUID courseId);
}
