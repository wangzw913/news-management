package com.example.news.modules.upload.controller;

import com.example.news.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Tag(name = "文件上传", description = "图片上传")
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final List<String> ALLOWED_TYPES = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    private final Path uploadDir;

    public UploadController() {
        // 上传目录：项目根目录下的 6_uploads/images/
        String userDir = System.getProperty("user.dir");
        this.uploadDir = Paths.get(userDir, "..", "6_uploads", "images").toAbsolutePath().normalize();
        try { Files.createDirectories(uploadDir); } catch (IOException ignored) {}
    }

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("请选择文件");

        // 检查类型
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_TYPES.contains(ext)) {
            return Result.fail("不支持的文件类型: " + ext + "，仅允许 " + String.join(",", ALLOWED_TYPES));
        }

        // 检查大小
        if (file.getSize() > MAX_SIZE) {
            return Result.fail("文件不能超过 5MB");
        }

        // 生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;

        try {
            Path dest = uploadDir.resolve(filename);
            file.transferTo(dest.toFile());

            // 返回相对路径
            String relativePath = "6_uploads/images/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", relativePath);
            result.put("filename", filename);
            log.info("文件上传成功: {}", relativePath);
            return Result.ok(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }
}
