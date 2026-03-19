package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Service.StudyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                studyGroupService.createGroup(req.get("name"), userDetails.getUsername())
        );
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> myGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyGroupService.getMyGroups(userDetails.getUsername()));
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> join(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                studyGroupService.joinGroup(req.get("inviteCode"), userDetails.getUsername())
        );
    }

    @PostMapping("/{groupId}/share/{noteId}")
    public ResponseEntity<Map<String, Object>> shareNote(
            @PathVariable Long groupId,
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                studyGroupService.shareNote(groupId, noteId, userDetails.getUsername())
        );
    }

    @GetMapping("/{groupId}/notes")
    public ResponseEntity<List<Map<String, Object>>> groupNotes(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                studyGroupService.getGroupNotes(groupId, userDetails.getUsername())
        );
    }
}

