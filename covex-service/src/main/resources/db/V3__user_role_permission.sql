-- =============================================
-- S3: User / Role / Permission tables
-- =============================================

-- 1. ins_user
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ins_user (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL DEFAULT 0,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    real_name     VARCHAR(50)  DEFAULT NULL,
    phone         VARCHAR(20)  DEFAULT NULL,
    email         VARCHAR(100) DEFAULT NULL,
    user_type     TINYINT      NOT NULL DEFAULT 1 COMMENT '1=内部用户 2=渠道用户 3=外部用户',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
    last_login_at DATETIME     DEFAULT NULL,
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    DATETIME     DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    INDEX idx_tenant (tenant_id),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. ins_role
CREATE TABLE IF NOT EXISTS ins_role (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL DEFAULT 0,
    role_code     VARCHAR(30)  NOT NULL,
    role_name     VARCHAR(50)  NOT NULL,
    description   VARCHAR(200) DEFAULT NULL,
    is_system     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1=系统内置 0=自定义',
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    DATETIME     DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. ins_permission
CREATE TABLE IF NOT EXISTS ins_permission (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL DEFAULT 0,
    permission_code VARCHAR(50)  NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    module          VARCHAR(30)  NOT NULL COMMENT '所属模块',
    action          VARCHAR(20)  NOT NULL COMMENT '操作类型：create/read/update/delete',
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at      DATETIME     DEFAULT NULL,
    created_by      VARCHAR(50)  DEFAULT NULL,
    updated_by      VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_permission_code (permission_code),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. ins_user_role
CREATE TABLE IF NOT EXISTS ins_user_role (
    id         BIGINT    AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT    NOT NULL DEFAULT 0,
    user_id    BIGINT    NOT NULL,
    role_id    BIGINT    NOT NULL,
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    DATETIME     DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. ins_role_permission
CREATE TABLE IF NOT EXISTS ins_role_permission (
    id            BIGINT    AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT    NOT NULL DEFAULT 0,
    role_id       BIGINT    NOT NULL,
    permission_id BIGINT    NOT NULL,
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    DATETIME     DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 6. ins_data_scope
CREATE TABLE IF NOT EXISTS ins_data_scope (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL DEFAULT 0,
    role_id     BIGINT       NOT NULL,
    scope_type  TINYINT      NOT NULL COMMENT '1=全部 2=本部门 3=本人 4=自定义',
    scope_value VARCHAR(100) DEFAULT NULL COMMENT '自定义范围值',
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    DATETIME     DEFAULT NULL,
    created_by    VARCHAR(50)  DEFAULT NULL,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据范围表';
