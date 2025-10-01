package uz.consortgroup.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPurchasedCourseRepository extends JpaRepository<UserPurchasedCourse, UUID> {
    Optional<UserPurchasedCourse> findByUserIdAndCourseId(UUID userId, UUID courseId);

    @Query("""
           select upc.userId
           from UserPurchasedCourse upc
           where upc.courseId = :courseId
             and upc.userId in :userIds
             and upc.accessUntil >= :now
           """)
    List<UUID> findEnrolledUserIds(@Param("courseId") UUID courseId,
                                   @Param("userIds") List<UUID> userIds,
                                   @Param("now") Instant now);


    @Query("""
           select distinct upc.userId
           from UserPurchasedCourse upc
           where upc.courseId = :courseId
             and upc.accessUntil >= :now
           """)
    Page<UUID> pageActiveEnrolledUserIds(@Param("courseId") UUID courseId,
                                         @Param("now") Instant now,
                                         Pageable pageable);
}
