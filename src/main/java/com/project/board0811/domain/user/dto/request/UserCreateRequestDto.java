package com.project.board0811.domain.user.dto.request;

import com.project.board0811.domain.user.entity.User;
import com.project.board0811.domain.user.enums.UserRole;
import com.project.board0811.domain.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequestDto {
    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Email
    @Size(max = 50)
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자리 이상이어야 합니다.")
    private String password;

    @NotBlank
    @Size(max = 20)
    private String name;

    private UserRole role = UserRole.ROLE_USER;

    private UserStatus status = UserStatus.ACTIVE;

    public User toEntity(String encodedPassword) {
        return User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword) // 반드시 BCrypt 등으로 해싱한 값
                .name(name)
                .role(role != null ? role : UserRole.ROLE_USER)
                .status(status != null ? status : UserStatus.ACTIVE)
                .build();
    }
}
