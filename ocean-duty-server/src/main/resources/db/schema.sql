-- 网站监控表
CREATE TABLE IF NOT EXISTS monitor_site (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    site_name       VARCHAR(100) NOT NULL,
    site_url        VARCHAR(500) NOT NULL,
    site_type       VARCHAR(50)  NOT NULL DEFAULT 'portal',
    status          TINYINT      NOT NULL DEFAULT 1,
    http_status     INTEGER,
    response_time   INTEGER,
    last_check_time DATETIME,
    error_message   VARCHAR(1000),
    timeout_ms      INTEGER      NOT NULL DEFAULT 10000,
    response_threshold INTEGER     NOT NULL DEFAULT 3000,
    deleted_flag    TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 模块监控表
CREATE TABLE IF NOT EXISTS monitor_module (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    site_id         INTEGER      NOT NULL,
    module_name     VARCHAR(100) NOT NULL,
    module_url      VARCHAR(500) NOT NULL,
    module_category VARCHAR(50)  NOT NULL DEFAULT 'disaster_warning',
    module_group    VARCHAR(50),
    check_type      VARCHAR(50)  NOT NULL DEFAULT 'WARN_HISTORY',
    check_param     VARCHAR(500),
    data_update_time DATETIME,
    expected_time   VARCHAR(20),
    status          TINYINT      NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    alarm_title     VARCHAR(200),
    alarm_code      VARCHAR(100),
    alarm_level     VARCHAR(50),
    last_check_time DATETIME,
    deleted_flag    TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 监控历史记录表
CREATE TABLE IF NOT EXISTS monitor_record (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    site_id     INTEGER,
    module_id   INTEGER,
    check_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      TINYINT      NOT NULL DEFAULT 1,
    detail      TEXT,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 值班日志表
CREATE TABLE IF NOT EXISTS duty_log (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER,
    user_name     VARCHAR(50)  NOT NULL,
    duty_time     DATETIME     NOT NULL,
    duty_date       DATE,
    site_status   VARCHAR(500),
    module_status VARCHAR(2000),
    problem       VARCHAR(2000),
    solution      VARCHAR(2000),
    recover_time  DATETIME,
    log_source    VARCHAR(20)  DEFAULT 'manual',
    action_type   VARCHAR(20),
    previous_log_id INTEGER,
    state_fingerprint VARCHAR(500),
    change_summary TEXT,
    abnormal_count INTEGER DEFAULT 0,
    new_abnormal_count INTEGER DEFAULT 0,
    changed_count INTEGER DEFAULT 0,
    recovered_count INTEGER DEFAULT 0,
    snapshot_json TEXT,
    closure_summary TEXT,
    deleted_flag  TINYINT      NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 值班日志监控项明细
CREATE TABLE IF NOT EXISTS duty_log_item (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    log_id          INTEGER      NOT NULL,
    target_type     VARCHAR(20)  NOT NULL,
    target_id       INTEGER      NOT NULL,
    target_key      VARCHAR(100) NOT NULL,
    target_name     VARCHAR(200) NOT NULL,
    category        VARCHAR(50),
    check_type      VARCHAR(50),
    status          TINYINT      NOT NULL,
    status_label    VARCHAR(20),
    change_type     VARCHAR(20),
    previous_status TINYINT,
    state_token     VARCHAR(120),
    detail_json     TEXT,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 值班异常事件（异常-恢复闭环）
CREATE TABLE IF NOT EXISTS duty_incident (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    target_type     VARCHAR(20)  NOT NULL,
    target_id       INTEGER      NOT NULL,
    target_key      VARCHAR(100) NOT NULL,
    target_name     VARCHAR(200) NOT NULL,
    category        VARCHAR(50),
    check_type      VARCHAR(50),
    duty_date       DATE         NOT NULL,
    incident_status VARCHAR(20)  NOT NULL,
    first_status    TINYINT      NOT NULL,
    last_status     TINYINT,
    first_log_id    INTEGER      NOT NULL,
    last_log_id     INTEGER,
    recover_log_id  INTEGER,
    first_seen_time DATETIME     NOT NULL,
    last_seen_time  DATETIME,
    recovered_time  DATETIME,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 服务器数据检测表
CREATE TABLE IF NOT EXISTS server_check (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    directory   VARCHAR(500) NOT NULL,
    file_name   VARCHAR(200),
    modify_time DATETIME,
    file_status TINYINT      NOT NULL DEFAULT 1,
    file_size   BIGINT,
    file_count  INTEGER,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(200) NOT NULL,
    real_name   VARCHAR(50)  NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'duty',
    start_time  DATETIME,
    end_time    DATETIME,
    status      TINYINT      NOT NULL DEFAULT 1,
    deleted_flag TINYINT     NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_monitor_module_site_id ON monitor_module(site_id);
CREATE INDEX IF NOT EXISTS idx_monitor_record_check_time ON monitor_record(check_time);
CREATE INDEX IF NOT EXISTS idx_duty_log_duty_time ON duty_log(duty_time);
CREATE INDEX IF NOT EXISTS idx_duty_log_item_log_id ON duty_log_item(log_id);
CREATE INDEX IF NOT EXISTS idx_duty_log_item_target ON duty_log_item(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_duty_incident_duty_date ON duty_incident(duty_date);
CREATE INDEX IF NOT EXISTS idx_duty_incident_target ON duty_incident(target_key, incident_status);

-- 模块数据源配置表
CREATE TABLE IF NOT EXISTS monitor_datasource (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    ds_name         VARCHAR(100) NOT NULL,
    ds_type         VARCHAR(20)  NOT NULL DEFAULT 'mysql',
    host            VARCHAR(200) NOT NULL,
    port            INTEGER      NOT NULL DEFAULT 3306,
    database_name   VARCHAR(100) NOT NULL,
    username        VARCHAR(100) NOT NULL,
    password        VARCHAR(200) NOT NULL,
    table_name      VARCHAR(100) NOT NULL,
    status          TINYINT      NOT NULL DEFAULT 1,
    deleted_flag    TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
