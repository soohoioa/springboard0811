package com.project.board0811.repository;

import com.project.board0811.domain.User;
import com.project.board0811.domain.enums.UserRole;
import com.project.board0811.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ===== 단건 조회 / 로그인 =====
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email); // username 또는 email 로 로그인 식별

    Optional<User> findByIdAndStatus(Long id, UserStatus status); // 상태까지 포함한 단건 조회

    // ===== 중복 체크 =====
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ===== 목록/검색 =====
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByRole(UserRole role, Pageable pageable);
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ===== 부가 기능 =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.lastLoginAt = :time where u.id = :id")
    int updateLastLoginAt(@Param("id") Long id, @Param("time") LocalDateTime time); // 마지막 로그인 시각 업데이트 (쓰기전용 쿼리)
}