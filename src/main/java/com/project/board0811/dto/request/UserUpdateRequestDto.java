package com.project.board0811.dto.request;

import com.project.board0811.domain.User;
import com.project.board0811.domain.enums.UserRole;
import com.project.board0811.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequestDto {
    @Size(max = 20)
    private String name;

    @Email
    @Size(max = 50)
    private String email;

    private UserRole role; // 관리자만 수정 가능하게 할 수도 있음
    private UserStatus status;

    /** 엔티티에 변경 적용 */
    public void applyTo(User user) {
        if (name != null && !name.isBlank()) user.updateProfile(name, email != null ? email : user.getEmail());
        if (role != null) user.changeRole(role);
        if (status != null) user.changeStatus(status);
    }
}
