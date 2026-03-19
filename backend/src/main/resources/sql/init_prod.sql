-- ============================================================
-- 项目计划看板系统 - 生产环境数据库初始化脚本
-- 适用版本：V4
-- 执行方式：mysql -u root -p < init_prod.sql
-- 注意：首次部署执行，已有数据库请勿重复执行
-- ============================================================

CREATE DATABASE IF NOT EXISTS dashboard_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dashboard_db;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名 / IOA 工号',
    `password` VARCHAR(100) NOT NULL        COMMENT '密码（IOA 登录用户固定为 IOA_SSO）',
    `role`     VARCHAR(20)  DEFAULT NULL    COMMENT '角色：ADMIN/MANAGER/USER，自定义角色为NULL'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 需求表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `requirement` (
    `id`                  BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `function_name`       VARCHAR(100) NOT NULL        COMMENT '需求名称',
    `module_name`         VARCHAR(100)                 COMMENT '模块名称',
    `request_department`  VARCHAR(100)                 COMMENT '需求方部门',
    `request_owner`       VARCHAR(50)                  COMMENT '需求对接人',
    `product_owner`       VARCHAR(50)                  COMMENT '产品对接人',
    `priority`            VARCHAR(20)                  COMMENT '优先级：紧急/高/中/低',
    `planned_start_time`  DATE                         COMMENT '计划开始时间',
    `planned_end_time`    DATE                         COMMENT '计划完成时间',
    `actual_start_time`   DATE                         COMMENT '实际开始时间',
    `actual_end_time`     DATE                         COMMENT '实际完成时间',
    `status`              VARCHAR(20)  NOT NULL DEFAULT '未开始' COMMENT '状态：未开始/设计中/开发中/测试中/已上线/已取消',
    `description`         TEXT                         COMMENT '需求描述',
    `created_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- ------------------------------------------------------------
-- 附件表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `attachment` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `requirement_id`   BIGINT       NOT NULL COMMENT '关联需求ID',
    `file_name`        VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url`         VARCHAR(500) NOT NULL COMMENT '文件访问路径',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

-- ------------------------------------------------------------
-- 需求操作日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `requirement_log` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `requirement_id`   BIGINT       NOT NULL COMMENT '关联需求ID',
    `operator`         VARCHAR(50)  NOT NULL COMMENT '操作人',
    `operation_type`   VARCHAR(20)  NOT NULL COMMENT '操作类型：创建/编辑/删除',
    `before_content`   TEXT                  COMMENT '变更前内容（JSON）',
    `after_content`    TEXT                  COMMENT '变更后内容（JSON）',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求操作日志表';

-- ------------------------------------------------------------
-- 用户登录日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `login_log` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`         VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `login_type`       VARCHAR(20)  NOT NULL COMMENT '登录方式：账号密码/IOA',
    `login_ip`         VARCHAR(50)           COMMENT '登录IP',
    `user_agent`       VARCHAR(255)          COMMENT '浏览器信息',
    `login_time`       DATETIME     NOT NULL COMMENT '登录时间',
    `logout_time`      DATETIME              COMMENT '退出时间',
    `duration_minutes` INT                   COMMENT '停留时长（分钟）',
    `status`           VARCHAR(10)  NOT NULL DEFAULT '在线' COMMENT '在线/已退出'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志';

-- ------------------------------------------------------------
-- 需求方部门字典表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '部门名称',
    `sort_order` INT          DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求方部门字典';

-- ------------------------------------------------------------
-- 需求模块字典表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_module` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '模块名称',
    `sort_order` INT          DEFAULT 0 COMMENT '排序',
    `bg_color`   VARCHAR(20)  DEFAULT NULL COMMENT '背景色',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求模块字典';

-- ------------------------------------------------------------
-- 角色表（V5.2 新增）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `code`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色编码',
    `built_in`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置 1=是',
    `remark`     VARCHAR(200) COMMENT '备注',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ------------------------------------------------------------
-- 权限点表（V5.2 新增）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '权限名称',
    `code`       VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    `type`       VARCHAR(20)  NOT NULL COMMENT 'MENU/BUTTON',
    `parent_id`  BIGINT       DEFAULT NULL COMMENT '父权限ID',
    `sort_order` INT          DEFAULT 0,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限点表';

-- ------------------------------------------------------------
-- 角色权限关联表（V5.2 新增）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- user 表加 role_id 列（V5.2，自定义角色用）
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `role_id` BIGINT DEFAULT NULL COMMENT '自定义角色ID，内置角色为NULL';

-- 内置角色数据
INSERT INTO `sys_role` (`id`, `name`, `code`, `built_in`, `remark`) VALUES
(1, '管理员',  'ADMIN',   1, '内置角色，拥有全部权限'),
(2, '经理',    'MANAGER', 1, '内置角色，可增改查，无删除'),
(3, '普通用户','USER',    1, '内置角色，只读权限')
ON DUPLICATE KEY UPDATE id=id;

-- 权限点数据
INSERT INTO `sys_permission` (`id`, `name`, `code`, `type`, `parent_id`, `sort_order`) VALUES
(1,  '看板',       'board:view',          'MENU',   NULL, 1),
(6,  '查看需求',   'requirement:view',    'BUTTON', 1,    0),
(2,  '新增需求',   'requirement:create',  'BUTTON', 1,    1),
(3,  '编辑需求',   'requirement:edit',    'BUTTON', 1,    2),
(4,  '取消需求',   'requirement:cancel',  'BUTTON', 1,    3),
(5,  '删除需求',   'requirement:delete',  'BUTTON', 1,    4),
(10, '需求方部门', 'system:dept',         'MENU',   NULL, 10),
(14, '查看部门',   'dept:view',           'BUTTON', 10,   0),
(11, '新增部门',   'dept:create',         'BUTTON', 10,   1),
(12, '编辑部门',   'dept:edit',           'BUTTON', 10,   2),
(13, '删除部门',   'dept:delete',         'BUTTON', 10,   3),
(20, '需求模块',   'system:module',       'MENU',   NULL, 20),
(24, '查看模块',   'module:view',         'BUTTON', 20,   0),
(21, '新增模块',   'module:create',       'BUTTON', 20,   1),
(22, '编辑模块',   'module:edit',         'BUTTON', 20,   2),
(23, '删除模块',   'module:delete',       'BUTTON', 20,   3),
(30, '用户管理',   'system:user',         'MENU',   NULL, 30),
(34, '查看用户',   'user:view',           'BUTTON', 30,   0),
(31, '新增用户',   'user:create',         'BUTTON', 30,   1),
(32, '编辑用户',   'user:edit',           'BUTTON', 30,   2),
(33, '删除用户',   'user:delete',         'BUTTON', 30,   3),
(40, '登录日志',   'system:login-log',    'MENU',   NULL, 40),
(50, '角色管理',   'system:role',         'MENU',   NULL, 50),
(51, '新增角色',   'role:create',         'BUTTON', 50,   1),
(52, '编辑角色',   'role:edit',           'BUTTON', 50,   2),
(53, '删除角色',   'role:delete',         'BUTTON', 50,   3)
ON DUPLICATE KEY UPDATE id=id;

-- 部门初始数据
INSERT INTO `sys_department` (`name`, `sort_order`) VALUES
('商品选品部', 1), ('商品合规部', 2), ('美妆支持中心', 3), ('财务部', 4),
('时尚事业部', 5), ('信息安全部', 6), ('法律合规部', 7), ('公共传播部', 8),
('直播现场运营部', 9), ('业务增长部', 10), ('所有女生直播间', 11),
('商品计划部', 12), ('招商部', 13), ('美妆国货部', 14)
ON DUPLICATE KEY UPDATE id=id;

-- ------------------------------------------------------------
-- 初始账号（生产环境请登录后立即修改 admin 密码）
-- ------------------------------------------------------------
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', 'admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE id=id;
