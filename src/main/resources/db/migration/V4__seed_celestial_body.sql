INSERT INTO `tb_celestial_body`
(`name`, `type`, `constellation`, `ra_hours`, `dec_degrees`, `magnitude`, `distance_ly`, `spectral_type`, `temperature_k`, `description`, `created_at`, `updated_at`, `is_deleted`)
VALUES
    ('Sirius', 'STAR', 'Canis Major', 6.7525, -16.7161, -1.46, 8.6, 'A1V', 9940, '天狼星：夜空中最亮的恒星之一。', NOW(), NOW(), 0),
    ('Betelgeuse', 'STAR', 'Orion', 5.9195, 7.4070, 0.42, 548.0, 'M1-2Ia-ab', 3500, '参宿四：猎户座肩部的红超巨星，亮度有变化。', NOW(), NOW(), 0),
    ('Rigel', 'STAR', 'Orion', 5.2423, -8.2016, 0.12, 863.0, 'B8Ia', 12100, '参宿七：猎户座脚部的蓝超巨星。', NOW(), NOW(), 0),
    ('Vega', 'STAR', 'Lyra', 18.6156, 38.7837, 0.03, 25.0, 'A0V', 9602, '织女星：夏季大三角之一。', NOW(), NOW(), 0),
    ('Mars', 'PLANET', NULL, NULL, NULL, -2.9, NULL, NULL, NULL, '火星：太阳系行星之一（此处仅示例数据）。', NOW(), NOW(), 0);