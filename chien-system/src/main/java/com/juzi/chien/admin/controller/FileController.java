package com.juzi.chien.admin.controller;

import com.juzi.chien.admin.common.BusinessException;
import com.juzi.chien.admin.common.ErrorCode;
import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Result;
import com.juzi.chien.admin.config.UploadConfig;
import com.juzi.chien.admin.domain.entity.SysFileRecord;
import com.juzi.chien.admin.security.LoginUser;
import com.juzi.chien.admin.service.SysFileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "文件上传", description = "文件上传接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final UploadConfig uploadConfig;
    private final SysFileRecordService fileRecordService;

    @Operation(summary = "上传单个文件")
    @PostMapping("/upload")
    @Log(title = "文件上传", businessType = BusinessType.INSERT)
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        Map<String, Object> result = saveFileSafely(file);
        return Result.success(result);
    }

    @Operation(summary = "上传多个文件")
    @PostMapping("/upload/batch")
    @Log(title = "批量上传", businessType = BusinessType.INSERT)
    public Result<List<Map<String, Object>>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        // 先校验所有文件，任何一个不通过则全部不上传
        for (MultipartFile file : files) {
            validateFile(file);
        }
        // 校验通过后逐个保存
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> savedPaths = new ArrayList<>();
        List<Long> savedRecordIds = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                Map<String, Object> result = saveFileSafely(file);
                results.add(result);
                savedPaths.add((String) result.get("relativePath"));
                savedRecordIds.add((Long) result.get("recordId"));
            }

            return Result.success(results);
        } catch (Exception e) {
            // 清理已保存的磁盘文件
            for (String path : savedPaths) {
                File f = new File(uploadConfig.getPath() + "/" + path);
                if (f.exists()) f.delete();
            }
            // 清理已插入的数据库记录
            for (Long id : savedRecordIds) {
                try { fileRecordService.deleteById(id); } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    @Operation(summary = "文件列表")
    @GetMapping("/list")
    public Result<List<SysFileRecord>> list() {
        return Result.success(fileRecordService.selectAll());
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @Log(title = "文件管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        SysFileRecord record = fileRecordService.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件记录不存在");
        }
        // 先删数据库记录
        fileRecordService.deleteById(id);
        // 再删磁盘文件（即使失败也不影响返回，记录已删除）
        File file = new File(uploadConfig.getPath() + "/" + record.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        return Result.success();
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
     * 安全保存文件：先存磁盘，再存数据库，失败时自动清理
     */
    private Map<String, Object> saveFileSafely(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = dateDir + "/" + newFileName;
        String storagePath = uploadConfig.getPath() + "/" + relativePath;

        File dest = new File(storagePath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        // 保存到磁盘
        try {
            Files.copy(file.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件保存失败: " + e.getMessage());
        }

        // 校验文件完整性
        if (!dest.exists() || dest.length() <= 0) {
            if (dest.exists()) dest.delete();
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件写入异常，保存失败");
        }

        // 保存数据库记录
        SysFileRecord record = new SysFileRecord();
        try {
            String url = "/admin/file/view/" + relativePath;
            String uploadUser = "";
            try {
                LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                uploadUser = loginUser.getUsername();
            } catch (Exception ignored) {}

            record.setOriginalName(originalName);
            record.setFileName(newFileName);
            record.setFilePath(relativePath);
            record.setUrl(url);
            record.setExtension(extension);
            record.setFileSize(file.getSize());
            record.setUploadUser(uploadUser);
            fileRecordService.insert(record);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("recordId", record.getId());
            result.put("originalName", originalName);
            result.put("fileName", newFileName);
            result.put("relativePath", relativePath);
            result.put("url", url);
            result.put("size", file.getSize());
            result.put("extension", extension);
            return result;
        } catch (BusinessException e) {
            // BusinessException 直接清理后抛出
            cleanup(dest, record);
            throw e;
        } catch (Exception e) {
            // 其他异常清理磁盘文件和数据库记录
            cleanup(dest, record);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 清理：删除磁盘文件和数据库记录
     */
    private void cleanup(File dest, SysFileRecord record) {
        if (dest.exists()) {
            dest.delete();
        }
        if (record != null && record.getId() != null) {
            try { fileRecordService.deleteById(record.getId()); } catch (Exception ignored) {}
        }
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
