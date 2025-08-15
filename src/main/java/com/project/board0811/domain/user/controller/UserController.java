package com.project.board0811.domain.user.controller;

import com.project.board0811.common.paging.PageRequestDto;
import com.project.board0811.common.paging.PageResponse;
import com.project.board0811.common.response.CommonApiResponse;
import com.project.board0811.domain.user.dto.request.UserCreateRequestDto;
import com.project.board0811.domain.user.dto.request.UserPasswordChangeRequestDto;
import com.project.board0811.domain.user.dto.request.UserUpdateRequestDto;
import com.project.board0811.domain.user.dto.response.UserResponseDto;
import com.project.board0811.domain.user.dto.response.UserSummaryResponseDto;
import com.project.board0811.domain.user.enums.UserRole;
import com.project.board0811.domain.user.enums.UserStatus;
import com.project.board0811.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // 회원가입 (201 Created)
    @PostMapping
    public ResponseEntity<CommonApiResponse<UserResponseDto>> create(
            @Valid @RequestBody UserCreateRequestDto req
    ) {
        UserResponseDto data = userService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.ok(data));
    }

    // 단건 조회 (200 OK)
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<UserResponseDto>> get(@PathVariable Long id) {
        UserResponseDto data = userService.get(id);
        return ResponseEntity.ok(CommonApiResponse.ok(data));
    }

    // 목록 조회 (200 OK)
    @GetMapping
    public ResponseEntity<CommonApiResponse<PageResponse<UserSummaryResponseDto>>> list(
            @Valid PageRequestDto pageReq,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<UserSummaryResponseDto> data = userService.list(pageReq, status, role, keyword);
        return ResponseEntity.ok(CommonApiResponse.ok(data));
    }

    // 회원정보 수정 (본인 또는 관리자) (200 OK)
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<UserResponseDto>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long principalUserId,
            @Valid @RequestBody UserUpdateRequestDto req
    ) {
        UserResponseDto data = userService.update(id, principalUserId, req);
        return ResponseEntity.ok(CommonApiResponse.ok(data));
    }

    // 비밀번호 변경 (본인 또는 관리자) (200 OK)
    @PostMapping("/{id}/password")
    public ResponseEntity<CommonApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long principalUserId,
            @Valid @RequestBody UserPasswordChangeRequestDto req
    ) {
        userService.changePassword(id, principalUserId, req.getNewPassword());
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    // 역할 변경 (관리자 전용) (200 OK)
    @PostMapping("/{id}/role")
    public ResponseEntity<CommonApiResponse<Void>> changeRole(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long adminUserId,
            @RequestParam UserRole role
    ) {
        userService.changeRole(id, adminUserId, role);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    // 상태 변경 (관리자 전용) (200 OK)
    @PostMapping("/{id}/status")
    public ResponseEntity<CommonApiResponse<Void>> changeStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long adminUserId,
            @RequestParam UserStatus status
    ) {
        userService.changeStatus(id, adminUserId, status);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    // 탈퇴 처리 (본인 또는 관리자) - 소프트 삭제: status=DELETED (204 No Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long principalUserId
    ) {
        userService.delete(id, principalUserId);
        return ResponseEntity.noContent().build();
    }
}
