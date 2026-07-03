package com.example.news.modules.slide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.common.annotation.LogOperation;
import com.example.news.common.response.Result;
import com.example.news.modules.slide.entity.Slide;
import com.example.news.modules.slide.mapper.SlideMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轮播管理", description = "首页轮播图 CRUD")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SlideController {

    private final SlideMapper slideMapper;

    @Operation(summary = "启用的轮播列表")
    @GetMapping("/slides")
    public Result<List<Slide>> list() {
        return Result.ok(slideMapper.selectList(
                new LambdaQueryWrapper<Slide>().eq(Slide::getStatus, 1).orderByAsc(Slide::getSortOrder)));
    }

    @Operation(summary = "全部轮播（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/slides")
    public Result<List<Slide>> adminList() {
        return Result.ok(slideMapper.selectList(
                new LambdaQueryWrapper<Slide>().orderByAsc(Slide::getSortOrder)));
    }

    @Operation(summary = "新增轮播")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "新增轮播")
    @PostMapping("/admin/slides")
    public Result<Void> create(@RequestBody Slide slide) {
        slideMapper.insert(slide);
        return Result.ok();
    }

    @Operation(summary = "编辑轮播")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/slides/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Slide slide) {
        slide.setSlideId(id);
        slideMapper.updateById(slide);
        return Result.ok();
    }

    @Operation(summary = "删除轮播")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/slides/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        slideMapper.deleteById(id);
        return Result.ok();
    }
}
