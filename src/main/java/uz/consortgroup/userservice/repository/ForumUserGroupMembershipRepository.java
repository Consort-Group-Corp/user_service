package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.ForumUserGroupMembership;

import java.util.UUID;

@Repository
public interface ForumUserGroupMembershipRepository extends JpaRepository<ForumUserGroupMembership, UUID> {
    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);
}
