-- V6.1 迁移脚本：产品对接人维护功能

CREATE TABLE IF NOT EXISTS `sys_product_owner` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL UNIQUE,
    `sort_order` INT          DEFAULT 0,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO `sys_product_owner` (name, sort_order) VALUES
('刘秋诗', 1),
('赵轶群', 2),
('丁滢',   3),
('Hanson', 4),
('张明洋', 5),
('邵森伟', 6);
