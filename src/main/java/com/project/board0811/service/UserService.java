package com.project.board0811.service;

import com.project.board0811.common.paging.PageRequestDto;
import com.project.board0811.common.paging.PageResponse;
import com.project.board0811.domain.enums.UserRole;
import com.project.board0811.domain.enums.UserStatus;
import com.project.board0811.dto.request.UserCreateRequestDto;
import com.project.board0811.dto.request.UserUpdateRequestDto;
import com.project.board0811.dto.response.UserResponseDto;
import com.project.board0811.dto.response.UserSummaryResponseDto;

public interface UserService {

    UserResponseDto create(UserCreateRequestDto request); // 회원가입

    UserResponseDto get(Long id); // 단건 조회

    PageResponse<UserSummaryResponseDto> list(PageRequestDto pageReq, UserStatus status,
                                              UserRole role, String keyword); // 목록 조회 (옵션 필터: status/role/keyword[이름 부분일치])

    UserResponseDto update(Long id, Long editorUserId, UserUpdateRequestDto request);

    void changePassword(Long id, Long requesterUserId, String newPassword);

    void changeRole(Long id, Long adminUserId, UserRole newRole); // 역할 변경 (관리자만)

    void changeStatus(Long id, Long adminUserId, UserStatus newStatus); // 상태 변경 (관리자만)

    void delete(Long id, Long requesterUserId); //  탈퇴 처리(소프트 삭제: status=DELETED)

    void updateLastLoginAt(Long id); // 마지막 로그인 시각 업데이트
}
