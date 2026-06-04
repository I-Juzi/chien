-- =============================================================
-- Chien Admin 数据库初始化脚本
-- 数据库: chien (utf8mb4)
-- 执行方式: mysql -u root -p < sql/chien.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `chien` DEFAULT CHARACTER SET utf8mb4;
USE `chien`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================
-- 1. 用户表
-- =============================================================
DROP TABLE IF EXISTS `chien-user`;
CREATE TABLE `chien-user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(200) NOT NULL COMMENT '密码',
  `nickname` varchar(64) DEFAULT '' COMMENT '昵称',
  `email` varchar(128) DEFAULT '' COMMENT '邮箱',
  `phone` varchar(20) DEFAULT '' COMMENT '手机号',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像地址',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0=禁用 1=正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0=未删除 1=已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================================
-- 2. 角色表
-- =============================================================
DROP TABLE IF EXISTS `chien-role`;
CREATE TABLE `chien-role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色标识',
  `description` varchar(255) DEFAULT '' COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0=禁用 1=正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================================
-- 3. 菜单权限表
-- =============================================================
DROP TABLE IF EXISTS `chien-menu`;
CREATE TABLE `chien-menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID（0=顶级）',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `path` varchar(255) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT '' COMMENT '组件路径',
  `icon` varchar(64) DEFAULT '' COMMENT '菜单图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `is_visible` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示（0=隐藏 1=显示）',
  `menu_type` char(1) NOT NULL COMMENT '菜单类型（M=目录 C=菜单 F=按钮）',
  `perms` varchar(200) DEFAULT '' COMMENT '权限标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- =============================================================
-- 4. 用户-角色关联表
-- =============================================================
DROP TABLE IF EXISTS `chien-user_role`;
CREATE TABLE `chien-user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- =============================================================
-- 5. 角色-菜单关联表
-- =============================================================
DROP TABLE IF EXISTS `chien-role_menu`;
CREATE TABLE `chien-role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- =============================================================
-- 6. 操作日志表
-- =============================================================
DROP TABLE IF EXISTS `chien-oper_log`;
CREATE TABLE `chien-oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0=其他 1=新增 2=修改 3=删除 4=导出 5=导入）',
  `method` varchar(200) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `oper_url` varchar(500) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(50) DEFAULT '' COMMENT '操作IP',
  `oper_param` text COMMENT '请求参数',
  `json_result` text COMMENT '返回结果',
  `status` int DEFAULT '1' COMMENT '操作状态（1=成功 0=失败）',
  `error_msg` text COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '耗时（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- =============================================================
-- 7. 登录日志表
-- =============================================================
DROP TABLE IF EXISTS `chien-login_log`;
CREATE TABLE `chien-login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT '' COMMENT '用户名',
  `ip` varchar(50) DEFAULT '' COMMENT '登录IP',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` int DEFAULT '1' COMMENT '状态（1=成功 0=失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '消息',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- =============================================================
-- 8. 字典类型表
-- =============================================================
DROP TABLE IF EXISTS `chien-dict_type`;
CREATE TABLE `chien-dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型（唯一标识）',
  `status` int DEFAULT '1' COMMENT '状态（1=正常 0=停用）',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- =============================================================
-- 9. 字典数据表
-- =============================================================
DROP TABLE IF EXISTS `chien-dict_data`;
CREATE TABLE `chien-dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
  `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(100) NOT NULL COMMENT '字典键值',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` int DEFAULT '1' COMMENT '状态（1=正常 0=停用）',
  `css_class` varchar(100) DEFAULT '' COMMENT '样式类名',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- =============================================================
-- 10. 通知公告表
-- =============================================================
DROP TABLE IF EXISTS `chien-notice`;
CREATE TABLE `chien-notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `notice_type` tinyint DEFAULT '1' COMMENT '类型（1=通知 2=公告）',
  `status` tinyint DEFAULT '0' COMMENT '状态（0=草稿 1=已发布）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- =============================================================
-- 11. 文件记录表
-- =============================================================
DROP TABLE IF EXISTS `chien-file_record`;
CREATE TABLE `chien-file_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件相对路径',
  `url` varchar(500) NOT NULL COMMENT '访问URL',
  `extension` varchar(20) DEFAULT NULL COMMENT '文件后缀',
  `file_size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `upload_user` varchar(64) DEFAULT NULL COMMENT '上传用户',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================
-- 初始数据
-- =============================================================

-- -----------------------------------------------------------
-- 管理员账号（默认密码: admin123）
-- -----------------------------------------------------------
INSERT INTO `chien-user` (`id`, `username`, `password`, `nickname`, `status`) VALUES
(1, 'admin', '$2a$10$/z/g2UIXLia7wfQOLKQYjO7xUtrxtgKves3T6l34tMbERpuFQKBYe', '超级管理员', 1);

-- -----------------------------------------------------------
-- 角色
-- -----------------------------------------------------------
INSERT INTO `chien-role` (`id`, `role_name`, `role_key`, `description`, `status`) VALUES
(1, '超级管理员', 'admin', '拥有所有权限', 1);

-- -----------------------------------------------------------
-- 用户-角色关联
-- -----------------------------------------------------------
INSERT INTO `chien-user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- -----------------------------------------------------------
-- 菜单权限
-- -----------------------------------------------------------
INSERT INTO `chien-menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `icon`, `sort_order`, `is_visible`, `menu_type`, `perms`) VALUES
-- 系统管理
(1,  0,  '系统管理', '/system',  '',                         'setting',          1, 1, 'M', ''),
(2,  1,  '用户管理',  'user',    'system/user/index',        'user',             1, 1, 'C', ''),
(3,  2,  '用户查询',  '',        '',                         '',                 1, 1, 'F', 'system:user:query'),
(4,  2,  '用户新增',  '',        '',                         '',                 2, 1, 'F', 'system:user:add'),
(5,  2,  '用户编辑',  '',        '',                         '',                 3, 1, 'F', 'system:user:edit'),
(6,  2,  '用户删除',  '',        '',                         '',                 4, 1, 'F', 'system:user:remove'),
(7,  1,  '角色管理',  'role',    'system/role/index',        'peoples',          2, 1, 'C', ''),
(8,  7,  '角色查询',  '',        '',                         '',                 1, 1, 'F', 'system:role:query'),
(9,  7,  '角色新增',  '',        '',                         '',                 2, 1, 'F', 'system:role:add'),
(10, 7,  '角色编辑',  '',        '',                         '',                 3, 1, 'F', 'system:role:edit'),
(11, 7,  '角色删除',  '',        '',                         '',                 4, 1, 'F', 'system:role:remove'),
(12, 1,  '菜单管理',  'menu',    'system/menu/index',        'tree-table',       3, 1, 'C', ''),
(13, 12, '菜单查询',  '',        '',                         '',                 1, 1, 'F', 'system:menu:query'),
(14, 12, '菜单新增',  '',        '',                         '',                 2, 1, 'F', 'system:menu:add'),
(15, 12, '菜单编辑',  '',        '',                         '',                 3, 1, 'F', 'system:menu:edit'),
(16, 12, '菜单删除',  '',        '',                         '',                 4, 1, 'F', 'system:menu:remove'),
(17, 2,  '用户列表',  '',        '',                         '',                 0, 1, 'F', 'system:user:list'),
(18, 7,  '角色列表',  '',        '',                         '',                 0, 1, 'F', 'system:role:list'),
(19, 12, '菜单列表',  '',        '',                         '',                 0, 1, 'F', 'system:menu:list'),
-- 系统监控
(20, 0,  '系统监控', '/monitor', '',                         'monitor',          2, 1, 'M', ''),
(21, 20, '在线用户',  'online',  'monitor/online/index',     'user-filled',      1, 1, 'C', ''),
(22, 21, '在线用户查询', '',     '',                         '',                 1, 1, 'F', 'monitor:online:query'),
(23, 21, '强退用户',  '',        '',                         '',                 2, 1, 'F', 'monitor:online:kick'),
(24, 20, '操作日志',  'operlog', 'monitor/operlog/index',    'document',         2, 1, 'C', ''),
(25, 24, '日志查询',  '',        '',                         '',                 1, 1, 'F', 'monitor:operlog:query'),
(26, 24, '日志删除',  '',        '',                         '',                 2, 1, 'F', 'monitor:operlog:remove'),
(27, 24, '日志清空',  '',        '',                         '',                 3, 1, 'F', 'monitor:operlog:clean'),
(28, 20, '登录日志',  'loginlog','monitor/loginlog/index',   'promotion',        3, 1, 'C', ''),
(29, 28, '登录日志查询', '',     '',                         '',                 1, 1, 'F', 'monitor:loginlog:query'),
(30, 28, '登录日志删除', '',     '',                         '',                 2, 1, 'F', 'monitor:loginlog:remove'),
(31, 28, '登录日志清空', '',     '',                         '',                 3, 1, 'F', 'monitor:loginlog:clean'),
-- 系统工具
(32, 0,  '系统工具', '/tool',   '',                         'toolbox',          3, 1, 'M', ''),
(33, 32, '数据字典',  'dict',    'system/dict/index',        'collection',       1, 1, 'C', ''),
(34, 33, '字典查询',  '',        '',                         '',                 1, 1, 'F', 'tool:dict:query'),
(35, 33, '字典新增',  '',        '',                         '',                 2, 1, 'F', 'tool:dict:add'),
(36, 33, '字典修改',  '',        '',                         '',                 3, 1, 'F', 'tool:dict:edit'),
(37, 33, '字典删除',  '',        '',                         '',                 4, 1, 'F', 'tool:dict:remove'),
-- 其他
(38, 1,  '文件上传',  'upload',  'system/upload/index',      'ep:upload-filled', 5, 1, 'C', 'system:upload:list'),
(39, 1,  '个人中心',  'profile', 'system/profile/index',     'ep:user-filled',   6, 0, 'C', 'system:user:profile'),
(40, 1,  '通知公告',  'notice',  'system/notice/index',      'ep:bell',          7, 1, 'C', 'system:notice:list');

-- -----------------------------------------------------------
-- 角色-菜单关联（超级管理员拥有所有菜单）
-- -----------------------------------------------------------
INSERT INTO `chien-role_menu` (`role_id`, `menu_id`) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),
(1,20),(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),
(1,28),(1,29),(1,30),(1,31),(1,32),(1,33),(1,34),(1,35),
(1,36),(1,37),(1,38),(1,39),(1,40);

-- -----------------------------------------------------------
-- 字典类型
-- -----------------------------------------------------------
INSERT INTO `chien-dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`) VALUES
(1, '用户性别', 'sys_user_gender',   1, '用户性别列表'),
(2, '系统状态', 'sys_normal_disable', 1, '系统开关列表'),
(3, '菜单类型', 'sys_menu_type',     1, '菜单类型列表'),
(4, '操作类型', 'sys_oper_type',     1, '操作日志业务类型'),
(5, '系统是否', 'sys_yes_no',        1, '是否选项'),
(6, '通知类型', 'sys_notice_type',   1, '通知公告类型'),
(7, '通知状态', 'sys_notice_status', 1, '通知公告状态'),
(8, '任务状态', 'sys_task_status',   1, '定时任务状态'),
(9, '文件类型', 'sys_file_type',     1, '上传文件类型');

-- -----------------------------------------------------------
-- 字典数据
-- -----------------------------------------------------------
INSERT INTO `chien-dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort_order`, `status`, `remark`) VALUES
-- 用户性别
('sys_user_gender',    '男',   '1',        1, 1, '性别男'),
('sys_user_gender',    '女',   '2',        2, 1, '性别女'),
('sys_user_gender',    '未知', '0',        3, 1, '未知性别'),
-- 系统状态
('sys_normal_disable', '正常', '1',        1, 1, '正常状态'),
('sys_normal_disable', '停用', '0',        2, 1, '停用状态'),
-- 菜单类型
('sys_menu_type',      '目录', 'M',        1, 1, '目录类型'),
('sys_menu_type',      '菜单', 'C',        2, 1, '菜单类型'),
('sys_menu_type',      '按钮', 'F',        3, 1, '按钮类型'),
-- 操作类型
('sys_oper_type',      '其他', '0',        1, 1, '其他操作'),
('sys_oper_type',      '新增', '1',        2, 1, '新增操作'),
('sys_oper_type',      '修改', '2',        3, 1, '修改操作'),
('sys_oper_type',      '删除', '3',        4, 1, '删除操作'),
('sys_oper_type',      '导出', '4',        5, 1, '导出操作'),
('sys_oper_type',      '导入', '5',        6, 1, '导入操作'),
-- 系统是否
('sys_yes_no',         '是',   '1',        1, 1, '是'),
('sys_yes_no',         '否',   '0',        2, 1, '否'),
-- 通知类型
('sys_notice_type',    '通知', '1',        1, 1, '系统通知'),
('sys_notice_type',    '公告', '2',        2, 1, '系统公告'),
-- 通知状态
('sys_notice_status',  '草稿',   '0',      1, 1, '草稿状态'),
('sys_notice_status',  '已发布', '1',      2, 1, '已发布状态'),
-- 任务状态
('sys_task_status',    '运行中', '1',      1, 1, '任务运行中'),
('sys_task_status',    '已暂停', '0',      2, 1, '任务已暂停'),
-- 文件类型
('sys_file_type',      '图片',   'image',    1, 1, 'jpg/png/gif/webp'),
('sys_file_type',      '文档',   'document', 2, 1, 'doc/docx/pdf/xls/xlsx'),
('sys_file_type',      '视频',   'video',    3, 1, 'mp4/avi/mov'),
('sys_file_type',      '音频',   'audio',    4, 1, 'mp3/wav'),
('sys_file_type',      '压缩包', 'archive',  5, 1, 'zip/rar/7z'),
('sys_file_type',      '其他',   'other',    6, 1, '其他文件类型');


-- =============================================================
-- 以下为运行时表，系统运行后自动产生数据，无需初始导入：
--   chien-notice       通知公告
--   chien-oper_log     操作日志
--   chien-login_log    登录日志
--   chien-file_record  文件记录
--   在线用户通过 Redis 管理，不建表
-- =============================================================
