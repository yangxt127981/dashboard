package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.entity.Attachment;
import com.dashboard.mapper.AttachmentMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    private final AttachmentMapper attachmentMapper;

    public AttachmentController(AttachmentMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;
    }

    @GetMapping("/requirements/{requirementId}/attachments")
    public Result<?> list(@PathVariable Long requirementId) {
        List<Attachment> list = attachmentMapper.findByRequirementId(requirementId);
        return Result.success(list);
    }

    @PostMapping("/requirements/{requirementId}/attachments")
    public Result<?> add(@PathVariable Long requirementId,
                         @RequestBody Attachment attachment,
                         HttpServletRequest request) {
        attachment.setRequirementId(requirementId);
        attachmentMapper.insert(attachment);
        return Result.success(attachment);
    }

    @DeleteMapping("/attachments/{id}")
    public Result<?> delete(@PathVariable Long id) {
        attachmentMapper.deleteById(id);
        return Result.success();
    }
}
