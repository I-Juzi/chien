package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.BusinessException;
import com.juzi.chien.admin.common.ErrorCode;
import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.config.UploadConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "文件上传", description = "文件上传接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final UploadConfig uploadConfig;

    @Operation(summary = "上传单个文件")
    @PostMapping("/upload")
    @Log(title = "文件上传", businessType = BusinessType.INSERT)
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        Map<String, Object> result = saveFile(file);
        return Result.success(result);
    }

    @Operation(summary = "上传多个文件")
    @PostMapping("/upload/batch")
    @Log(title = "批量上传", businessType = BusinessType.INSERT)
    public Result<List<Map<String, Object>>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            results.add(saveFile(file));
        }
        return Result.success(results);
    }

    /**
     * 文件预览/下载（无需登录）
     * 路径示例：/admin/file/view/2026/05/28/abc123.jpg
     */
    @GetMapping("/view/**")
    public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 从完整 URI 中提取相对路径
        String uri = request.getRequestURI();
        String prefix = request.getContextPath() + "/file/view/";
        String relativePath = uri.substring(uri.indexOf(prefix) + prefix.length());

        // 防止路径遍历攻击
        if (relativePath.contains("..")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File file = new File(uploadConfig.getPath() + "/" + relativePath);
        if (!file.exists() || !file.isFile()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 根据文件后缀设置 Content-Type
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());

        // 图片等资源内联显示，其他文件下载
        String ext = getFileExtension(file.getName()).toLowerCase();
        Set<String> inlineTypes = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "pdf", "mp3", "mp4");
        if (!inlineTypes.contains(ext)) {
            String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
        }

        try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "上传文件不能为空");
        }

        // 校验文件大小
        long maxSizeBytes = (long) uploadConfig.getMaxSize() * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED,
                    "文件大小不能超过 " + uploadConfig.getMaxSize() + "MB");
        }

        // 校验文件后缀
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件名不能为空");
        }

        String extension = getFileExtension(originalName).toLowerCase();
        Set<String> allowed = Set.of(uploadConfig.getAllowedExtensions().split(","));
        if (!allowed.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "不支持的文件类型: " + extension + "，允许的类型: " + uploadConfig.getAllowedExtensions());
        }
    }

    /**
     * 保存文件到磁盘
     */
    private Map<String, Object> saveFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);

        // 按日期生成子目录，避免单目录文件过多
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        // 拼接存储路径
        String relativePath = dateDir + "/" + newFileName;
        String storagePath = uploadConfig.getPath() + "/" + relativePath;

        File dest = new File(storagePath);
        // 确保父目录存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件保存失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalName", originalName);
        result.put("fileName", newFileName);
        result.put("url", "/admin/file/view/" + relativePath);
        result.put("size", file.getSize());
        result.put("extension", extension);
        return result;
    }

    /**
     * 获取文件后缀名（不含点）
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
}
