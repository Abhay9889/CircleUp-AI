package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.Entity.Note;
import com.EduCircle.Langchain.Entity.StudyGroup;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Noterepository;
import com.EduCircle.Langchain.Repository.StudyGroupRepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final Noterepository noterepository;
    private final UserRepository userRepository;
    @Transactional
    public Map<String,Object>createGroup(String name,String userEmail){
        User owner=getUser(userEmail);
        StudyGroup group=StudyGroup.builder().name(name).owner(owner).inviteCode(generateInviteCode()).build();
        group.getMembers().add(owner);
        StudyGroup saved=groupRepository.save(group);
        return groupToMap(saved);
    }

    @Transactional
    public Map<String,Object>joinGroup(String invitecode,String userEmail){
        User user=getUser(userEmail);
        StudyGroup group=groupRepository.findByInviteCode(invitecode).orElseThrow(()->new ResourceNotFoundException("Group not found!!"));
        group.getMembers().add(user);
        groupRepository.save(group);
        return groupToMap(group);


    }

    public List<Map<String,Object>>getMyGroups(String userEmail){
        User user=getUser(userEmail);
        Set<StudyGroup>groups=new HashSet<>();
        groups.addAll(groupRepository.findByOwnerId(user.getId()));
        groups.addAll(groupRepository.findByMembers_Id(user.getId()));
        return groups.stream().map(this::groupToMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> shareNote(Long groupId, Long noteId, String userEmail) {
        User user   = getUser(userEmail);
        StudyGroup  group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        Note note   = noterepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        group.getSharedNotes().add(note);
        groupRepository.save(group);
        return Map.of("shared", true, "noteId", noteId, "groupId", groupId);
    }

    public List<Map<String, Object>> getGroupNotes(Long groupId, String userEmail) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return group.getSharedNotes().stream()
                .map(n -> Map.<String, Object>of(
                        "id",    n.getId(),
                        "title", n.getTitle(),
                        "type",  n.getFileType() != null ? n.getFileType() : ""
                ))
                .collect(Collectors.toList());
    }













    private String generateInviteCode() {
        String chars  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private Map<String, Object> groupToMap(StudyGroup g) {
        return Map.of(
                "id",          g.getId(),
                "name",        g.getName(),
                "inviteCode",  g.getInviteCode(),
                "memberCount", g.getMembers().size(),
                "noteCount",   g.getSharedNotes().size()
        );
    }


    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
