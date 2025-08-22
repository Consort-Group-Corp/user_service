package uz.consortgroup.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.ForumUserGroupMembership;

import java.util.List;
import java.util.UUID;

@Repository
public interface ForumUserGroupMembershipRepository extends JpaRepository<ForumUserGroupMembership, UUID> {
    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);

    @Query("SELECT DISTINCT m.groupId FROM ForumUserGroupMembership m WHERE m.userId = :userId")
    List<UUID> findGroupIdsByUserId(@Param("userId") UUID userId);
}
