package com.project.board0811.domain.user.dto.request;

import com.project.board0811.domain.user.entity.User;
import com.project.board0811.domain.user.enums.UserRole;
import com.project.board0811.domain.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDto {
    @Size(max = 20)
    private String name;

    @Email
    @Size(max = 50)
    private String email;

    private UserRole role; // 관리자만 수정 가능하게 할 수도 있음
    private UserStatus status;

    /** 엔티티에 변경 적용 (일반 필드만) */
    public void applyProfile(User user) {
        user.updateProfile(name, email); // 내부에서 null/blank 체크함
    }

    /** 관리자 전용 변경 (권한/상태) */
    public void applyAdminOnly(User user) {
        if (role != null) user.changeRole(role);
        if (status != null) user.changeStatus(status);
    }
}
