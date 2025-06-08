package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.userservice.entity.ForumUserGroup;

import java.util.UUID;

public interface ForumUserGroupRepository extends JpaRepository<ForumUserGroup, UUID> {
}
