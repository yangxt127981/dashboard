CREATE DATABASE IF NOT EXISTS dashboard_db DEFAULT CHARACTER SET utf8mb4;

USE dashboard_db;

CREATE TABLE IF NOT EXISTS `user` (
    `id`       BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `role`     VARCHAR(20)  NOT NULL COMMENT '角色：ADMIN/USER'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `requirement` (
    `id`                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    `function_name`       VARCHAR(100) NOT NULL COMMENT '需求名称',
    `module_name`         VARCHAR(100) COMMENT '模块名称',
    `request_department`  VARCHAR(100) COMMENT '需求方部门',
    `request_owner`       VARCHAR(50)  COMMENT '需求对接人',
    `product_owner`       VARCHAR(50)  COMMENT '产品对接人',
    `priority`            VARCHAR(20)  COMMENT '优先级：紧急/高/中/低',
    `planned_start_time`  DATE         COMMENT '计划开始时间',
    `planned_end_time`    DATE         COMMENT '计划完成时间',
    `actual_start_time`   DATE         COMMENT '实际开始时间',
    `actual_end_time`     DATE         COMMENT '实际完成时间',
    `status`              VARCHAR(20)  NOT NULL DEFAULT '未开始' COMMENT '状态：未开始/设计中/开发中/测试中/已上线',
    `description`         TEXT         COMMENT '需求描述',
    `created_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

CREATE TABLE IF NOT EXISTS `attachment` (
    `id`               BIGINT PRIMARY KEY AUTO_INCREMENT,
    `requirement_id`   BIGINT       NOT NULL COMMENT '关联需求ID',
    `file_name`        VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url`         VARCHAR(500) NOT NULL COMMENT '文件访问路径',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

CREATE TABLE IF NOT EXISTS `sys_department` (
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '部门名称',
    `sort_order` INT          DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求方部门字典';

CREATE TABLE IF NOT EXISTS `sys_module` (
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '模块名称',
    `sort_order` INT          DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求模块字典';

-- 部门初始数据
INSERT INTO `sys_department` (`name`, `sort_order`) VALUES
('商品选品部', 1), ('商品合规部', 2), ('美妆支持中心', 3), ('财务部', 4),
('时尚事业部', 5), ('信息安全部', 6), ('法律合规部', 7), ('公共传播部', 8),
('直播现场运营部', 9), ('业务增长部', 10), ('所有女生直播间', 11),
('商品计划部', 12), ('招商部', 13), ('美妆国货部', 14)
ON DUPLICATE KEY UPDATE id=id;

-- 初始账号
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', 'admin123', 'ADMIN'),
('user', 'user123', 'USER')
ON DUPLICATE KEY UPDATE id=id;

-- 示例数据
INSERT INTO `requirement` (`function_name`, `module_name`, `request_department`, `request_owner`, `product_owner`, `priority`, `planned_start_time`, `planned_end_time`, `status`, `description`) VALUES
('用户登录功能', '权限模块', '信息部', '张三', '李四', '高', '2026-03-01', '2026-03-15', '已上线', '支持用户名密码登录'),
('报表导出功能', '报表模块', '财务部', '王五', '赵六', '中', '2026-03-10', '2026-04-01', '开发中', '支持Excel格式导出'),
('消息通知推送', '通知模块', '运营部', '陈七', '李四', '高', '2026-03-15', '2026-04-10', '设计中', '支持站内消息和邮件通知'),
('数据看板优化', '首页模块', '管理层', '刘八', '赵六', '紧急', '2026-03-20', '2026-03-31', '未开始', '优化首页数据展示，增加图表'),
('移动端适配', '全局', '市场部', '孙九', '李四', '低', '2026-04-01', '2026-05-01', '未开始', '响应式布局适配移动设备');
