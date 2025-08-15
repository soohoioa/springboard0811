package com.project.board0811.domain.user.entity;

import com.project.board0811.domain.user.enums.UserRole;
import com.project.board0811.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(nullable = false, length = 50)
    private String username;

    // 이메일
    @Column(nullable = false, length = 100)
    private String email;

    // 비밀번호
    @Column(nullable = false)
    private String password;

    // 이름
    @Column(nullable = false, length = 50)
    private String name;

    // 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.ROLE_USER;

    // 계정 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    // 마지막 로그인 시간
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String username, String email, String password, String name, UserRole role, UserStatus status) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        if (role != null) this.role = role;
        if (status != null) this.status = status;
    }

    // 로그인 시 마지막 로그인 시각 업데이트
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // 회원정보 수정
    public void updateProfile(String name, String email) {
        if (name != null && !name.isBlank()) this.name = name;
        if (email != null && !email.isBlank()) this.email = email;
    }

    // 비밀번호 변경 (호출 전 해싱된 값으로 세팅 권장)
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // 권한/상태 변경
    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
    }
}