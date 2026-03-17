package com.dashboard.mapper;

import com.dashboard.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttachmentMapper {
    List<Attachment> findByRequirementId(Long requirementId);
    int insert(Attachment attachment);
    int deleteById(Long id);
}
