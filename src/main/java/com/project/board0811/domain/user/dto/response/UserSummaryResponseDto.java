package com.project.board0811.domain.user.dto.response;

import com.project.board0811.domain.user.entity.User;
import com.project.board0811.domain.user.enums.UserRole;
import com.project.board0811.domain.user.enums.UserStatus;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponseDto {
    private Long id;
    private String username;
    private String name;
    private UserRole role;
    private UserStatus status;

    public static UserSummaryResponseDto from(User user) {
        return UserSummaryResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
