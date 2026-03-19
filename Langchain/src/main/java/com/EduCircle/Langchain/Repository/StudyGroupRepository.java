package com.EduCircle.Langchain.Repository;

import com.EduCircle.Langchain.Entity.StudyGroup;
import com.EduCircle.Langchain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup,Long> {
    Optional<StudyGroup> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<StudyGroup> findByOwnerId(Long ownerId);

    List<StudyGroup> findByMembers_Id(Long userId);
}
