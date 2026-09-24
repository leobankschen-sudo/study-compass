package com.zhixu.studycompass.mapper;

import com.zhixu.studycompass.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface UserMapper {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email, @Param("excludeId") Long excludeId);
    int insert(User user);
    int insertWithPassword(User user);
    int updatePasswordHash(@Param("id") Long id, @Param("passwordHash") String passwordHash);
    int update(User user);
    int deleteById(Long id);
}
