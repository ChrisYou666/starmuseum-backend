-- V2: 初始化数据（尽量真实一些）
INSERT INTO `location` (`name`, `country`, `province`, `city`, `latitude`, `longitude`, `timezone`, `altitude_m`, `remark`)
VALUES
    ('北京·天安门', '中国', '北京', '北京', 39.9087220, 116.3974990, 'Asia/Shanghai', 44, '示例地点：北京市中心'),
    ('上海·外滩', '中国', '上海', '上海', 31.2400000, 121.4900000, 'Asia/Shanghai', 4, '示例地点：黄浦江沿岸'),
    ('雅加达·市中心', '印度尼西亚', '雅加达首都特区', '雅加达', -6.2000000, 106.8166667, 'Asia/Jakarta', 8, '示例地点：印尼首都'),
    ('纽约·中央公园', '美国', '纽约州', '纽约', 40.7850910, -73.9682850, 'America/New_York', 15, '示例地点：Central Park');