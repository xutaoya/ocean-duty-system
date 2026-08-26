-- 默认管理员账号 admin / admin123
INSERT OR IGNORE INTO sys_user (id, username, password, real_name, role, status)
VALUES (1, 'admin', 'admin123', '系统管理员', 'admin', 1);

-- 默认值班账号 duty / duty123
INSERT OR IGNORE INTO sys_user (id, username, password, real_name, role, status)
VALUES (2, 'duty', 'duty123', '值班人员', 'duty', 1);

-- 初始化监控网站
INSERT OR IGNORE INTO monitor_site (id, site_name, site_url, site_type, status)
VALUES
    (1, '中国海洋预报网', 'https://www.oceanguide.org.cn/IndexHome', 'portal', 1),
    (2, '国家海洋预报中心门户网站', 'https://www.nmefc.cn/', 'portal', 1),
    (3, '海洋灾害子场景', 'https://www.nmefc.cn/', 'subscene', 1),
    (4, 'NEARGOOS网站', 'https://neargoos.nmefc.cn/#/index', 'portal', 1),
    (5, 'MaCOM网站', 'https://macom.oceanguide.org.cn/', 'portal', 1);

-- 初始化灾害预警模块
INSERT OR IGNORE INTO monitor_module (id, site_id, module_name, module_url, expected_time, status)
VALUES
    (1, 2, '台风海浪警报', 'https://www.nmefc.cn/zhyj/hljb/tfhljb', '08:00', 1),
    (2, 2, '温带海浪警报', 'https://www.nmefc.cn/zhyj/hljb/wdhljb', '08:00', 1),
    (3, 2, '台风风暴潮警报', 'https://www.nmefc.cn/zhyj/fbcjb/tffbcjb', '08:00', 1),
    (4, 2, '温带风暴潮警报', 'https://www.nmefc.cn/zhyj/fbcjb/wdfbcjb', '08:00', 1),
    (5, 2, '海冰警报', 'https://www.nmefc.cn/zhyj/hbjb', '08:00', 1),
    (6, 2, '海啸消息/警报', 'https://www.nmefc.cn/zhyj/hx', '08:00', 1);
