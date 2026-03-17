package com.dashboard.controller;

import com.dashboard.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.error(400, "文件大小不能超过 10MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String savedName = UUID.randomUUID().toString() + ext;

        Path uploadDir = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Files.copy(file.getInputStream(), uploadDir.resolve(savedName));

        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/" + savedName);
        data.put("fileName", originalName);
        return Result.success(data);
    }
}
