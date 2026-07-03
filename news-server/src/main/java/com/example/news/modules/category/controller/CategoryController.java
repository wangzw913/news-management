package com.example.news.modules.category.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.news.common.annotation.LogOperation;
import com.example.news.common.exception.BusinessException;
import com.example.news.common.exception.ErrorCode;
import com.example.news.common.response.Result;
import com.example.news.modules.category.entity.Category;
import com.example.news.modules.category.mapper.CategoryMapper;
import com.example.news.modules.news.mapper.NewsMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "新闻分类 CRUD")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final NewsMapper newsMapper;

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public Result<List<Category>> list() {
        return Result.ok(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder)));
    }

    @Operation(summary = "新增分类")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "新增分类")
    @PostMapping("/admin/categories")
    public Result<Void> create(@RequestBody Category category) {
        categoryMapper.insert(category);
        return Result.ok();
    }

    @Operation(summary = "编辑分类")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "编辑分类")
    @PutMapping("/admin/categories/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setCategoryId(id);
        categoryMapper.updateById(category);
        return Result.ok();
    }

    @Operation(summary = "删除分类")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(value = "删除分类")
    @DeleteMapping("/admin/categories/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (newsMapper.selectCount(new LambdaQueryWrapper<com.example.news.modules.news.entity.News>()
                .eq(com.example.news.modules.news.entity.News::getCategoryId, id)) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_NEWS);
        }
        categoryMapper.deleteById(id);
        return Result.ok();
    }
}
