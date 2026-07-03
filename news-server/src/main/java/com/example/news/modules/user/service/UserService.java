package com.example.news.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.news.common.exception.BusinessException;
import com.example.news.common.exception.ErrorCode;
import com.example.news.modules.user.dto.ChangePasswordDTO;
import com.example.news.modules.user.dto.LoginDTO;
import com.example.news.modules.user.dto.RegisterDTO;
import com.example.news.modules.user.entity.User;
import com.example.news.modules.user.mapper.UserMapper;
import com.example.news.modules.user.vo.LoginResultVO;
import com.example.news.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;

    /**
     * 登录 — 验证用户名+密码+角色
     */
    public User login(LoginDTO dto) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        return user;
    }

    /**
     * 注册
     */
    @Transactional
    public void register(RegisterDTO dto) {
        if (this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        // 邮箱唯一性检查（如果提供了邮箱）
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail())) > 0) {
                throw new BusinessException(1007, "该邮箱已被注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("user"); // 注册只能是普通用户
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        this.save(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
    }

    /**
     * 用户列表（分页）
     */
    public Page<UserVO> listUsers(int page, int size, String keyword, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = this.page(new Page<>(page, size), wrapper);
        return (Page<UserVO>) userPage.convert(this::toVO);
    }

    /**
     * 切换用户启用/停用状态
     */
    @Transactional
    public void toggleStatus(Long userId) {
        User user = this.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        this.updateById(user);
    }

    // ===== DTO 转换 =====
    public UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setRoleName(getRoleName(user.getRole()));
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    public LoginResultVO toLoginResult(User user, String accessToken, String refreshToken) {
        return LoginResultVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .roleName(getRoleName(user.getRole()))
                .build();
    }

    private String getRoleName(String role) {
        return switch (role) {
            case "admin" -> "管理员";
            case "auditor" -> "审核员";
            case "editor" -> "编辑";
            case "user" -> "普通用户";
            default -> role;
        };
    }
}
