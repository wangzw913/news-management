package com.example.news.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.news.common.annotation.LogOperation;
import com.example.news.common.response.PageResult;
import com.example.news.common.response.Result;
import com.example.news.common.security.JwtTokenProvider;
import com.example.news.modules.user.dto.ChangePasswordDTO;
import com.example.news.modules.user.service.UserService;
import com.example.news.modules.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户 CRUD、密码修改")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "用户列表（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Page<UserVO> p = userService.listUsers(page, size, keyword, role);
        return Result.ok(PageResult.of(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @Operation(summary = "切换用户状态（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "切换用户状态")
    @PutMapping("/admin/users/{id}/toggle-status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @LogOperation(value = "修改密码")
    @PutMapping("/user/password")
    public Result<Void> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = (Long) auth.getCredentials();
        userService.changePassword(userId, dto);
        return Result.ok();
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/user/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody UserVO vo) {
        Long userId = (Long) auth.getCredentials();
        var user = userService.getById(userId);
        if (vo.getEmail() != null) user.setEmail(vo.getEmail());
        if (vo.getPhone() != null) user.setPhone(vo.getPhone());
        if (vo.getAvatar() != null) user.setAvatar(vo.getAvatar());
        userService.updateById(user);
        return Result.ok();
    }
}
