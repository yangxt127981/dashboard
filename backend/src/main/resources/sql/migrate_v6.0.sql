-- V6.0 迁移脚本：需求提报功能
-- 在 requirement 表添加提报相关字段

ALTER TABLE requirement
    ADD COLUMN submission_status VARCHAR(20) DEFAULT NULL COMMENT '提报状态：已创建/待评估/已驳回/进入需求池/已取消，NULL表示旧数据直接显示在看板',
    ADD COLUMN submitted_by VARCHAR(50) DEFAULT NULL COMMENT '提报人（IOA username）';

CREATE INDEX idx_requirement_submission_status ON requirement(submission_status, created_at);

-- V6.0.3 驳回意见
ALTER TABLE requirement ADD COLUMN reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回意见';
