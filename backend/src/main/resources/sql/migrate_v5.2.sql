-- ============================================================
-- V5.2 迁移脚本（在已有 V5.1 生产库上执行）
-- 执行方式：mysql -u root -p dashboard_db < migrate_v5.2.sql
-- 幂等：重复执行不会报错
-- ============================================================

USE dashboard_db;

-- 1. user 表：新增 role_id 列，并允许 role 为 NULL
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `role_id` BIGINT DEFAULT NULL COMMENT '自定义角色ID，内置角色为NULL';

ALTER TABLE `user`
    MODIFY COLUMN `role` VARCHAR(20) DEFAULT NULL COMMENT '角色：ADMIN/MANAGER/USER，自定义角色为NULL';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `code`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色编码',
    `built_in`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置 1=是',
    `remark`     VARCHAR(200) COMMENT '备注',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限点表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '权限名称',
    `code`       VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    `type`       VARCHAR(20)  NOT NULL COMMENT 'MENU/BUTTON',
    `parent_id`  BIGINT       DEFAULT NULL COMMENT '父权限ID',
    `sort_order` INT          DEFAULT 0,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';

-- 4. 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 内置角色种子数据
INSERT INTO `sys_role` (`id`, `name`, `code`, `built_in`, `remark`) VALUES
(1, '管理员',   'ADMIN',   1, '内置角色，拥有全部权限'),
(2, '经理',     'MANAGER', 1, '内置角色，可增改查，无删除'),
(3, '普通用户', 'USER',    1, '内置角色，只读权限')
ON DUPLICATE KEY UPDATE id=id;

-- 6. 权限点种子数据（含 V5.2 新增的 requirement:view）
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

SELECT 'V5.2 migration completed.' AS result;
