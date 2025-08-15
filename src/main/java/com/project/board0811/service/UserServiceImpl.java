package com.project.board0811.service;

import com.project.board0811.common.exception.CustomException;
import com.project.board0811.common.exception.ErrorCode;
import com.project.board0811.common.paging.PageConverters;
import com.project.board0811.common.paging.PageRequestDto;
import com.project.board0811.common.paging.PageResponse;
import com.project.board0811.domain.User;
import com.project.board0811.domain.enums.UserRole;
import com.project.board0811.domain.enums.UserStatus;
import com.project.board0811.dto.request.UserCreateRequestDto;
import com.project.board0811.dto.request.UserUpdateRequestDto;
import com.project.board0811.dto.response.UserResponseDto;
import com.project.board0811.dto.response.UserSummaryResponseDto;
import com.project.board0811.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 등 빈 등록 필요

    // ===== Create =====
    @Override
    public UserResponseDto create(UserCreateRequestDto request) {
        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_VALUE);
        }

        // 비밀번호 해싱 후 엔티티 생성
        String encoded = passwordEncoder.encode(request.getPassword());
        User saved = userRepository.save(request.toEntity(encoded));
        return UserResponseDto.from(saved);
    }

    // ===== Read One =====
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto get(Long id) {
        User user = getUserOrThrow(id);
        return UserResponseDto.from(user);
    }

    // ===== List (filters optional) =====
    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserSummaryResponseDto> list(PageRequestDto pageReq,
                                                     UserStatus status,
                                                     UserRole role,
                                                     String keyword) {
        var pageable = pageReq.toPageable();

        Page<User> page;
        if (keyword != null && !keyword.isBlank()) {
            // 이름 부분검색 우선(간단 구현) — 필요 시 username/email 포함 검색으로 확장
            page = userRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else if (status != null) {
            page = userRepository.findByStatus(status, pageable);
        } else if (role != null) {
            page = userRepository.findByRole(role, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        return PageConverters.toResponse(page, UserSummaryResponseDto::from,
                pageReq.getSort(), pageReq.getDirection());
    }

    // ===== Update Profile =====
    @Override
    public UserResponseDto update(Long id, Long editorUserId, UserUpdateRequestDto request) {
        User target = getUserOrThrow(id);
        User editor = getUserOrThrow(editorUserId);

        checkSelfOrAdmin(target, editor); // 본인 또는 관리자만

        // 중복 이메일 방지 (이메일을 변경하는 경우)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // 동일 유저의 기존 이메일 변경이 아니라면 존재 체크
            userRepository.findByEmail(request.getEmail())
                    .filter(found -> !Objects.equals(found.getId(), target.getId()))
                    .ifPresent(found -> { throw new CustomException(ErrorCode.DUPLICATE_VALUE); });
        }

        // 권한/상태는 관리자 권한에서만 변경 허용
        if (request.getRole() != null || request.getStatus() != null) {
            checkAdmin(editor);
        }

        request.applyTo(target); // 이름/이메일/역할/상태 변경
        return UserResponseDto.from(target);
    }

    // ===== Change Password =====
    @Override
    public void changePassword(Long id, Long requesterUserId, String newPassword) {
        User target = getUserOrThrow(id);
        User requester = getUserOrThrow(requesterUserId);

        checkSelfOrAdmin(target, requester);

        String encoded = passwordEncoder.encode(newPassword);
        target.changePassword(encoded);
    }

    // ===== Change Role (Admin only) =====
    @Override
    public void changeRole(Long id, Long adminUserId, UserRole newRole) {
        User target = getUserOrThrow(id);
        User admin = getUserOrThrow(adminUserId);

        checkAdmin(admin);
        target.changeRole(newRole);
    }

    // ===== Change Status (Admin only) =====
    @Override
    public void changeStatus(Long id, Long adminUserId, UserStatus newStatus) {
        User target = getUserOrThrow(id);
        User admin = getUserOrThrow(adminUserId);

        checkAdmin(admin);
        target.changeStatus(newStatus);
    }

    // ===== Soft Delete (본인 또는 관리자) =====
    @Override
    public void delete(Long id, Long requesterUserId) {
        User target = getUserOrThrow(id);
        User requester = getUserOrThrow(requesterUserId);

        checkSelfOrAdmin(target, requester);
        target.changeStatus(UserStatus.DELETED);
    }

    // ===== Last Login Update =====
    @Override
    public void updateLastLoginAt(Long id) {
        // 존재 체크
        getUserOrThrow(id);
        userRepository.updateLastLoginAt(id, LocalDateTime.now());
    }

    // ===== Helpers =====
    @Transactional(readOnly = true)
    protected User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    protected void checkSelfOrAdmin(User target, User actor) {
        boolean isSelf = target.getId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == UserRole.ROLE_ADMIN;
        if (!isSelf && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    protected void checkAdmin(User actor) {
        if (actor.getRole() != UserRole.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
