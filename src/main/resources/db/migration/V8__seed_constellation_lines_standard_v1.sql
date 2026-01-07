-- V8__seed_constellation_lines_standard_v1.sql
-- 目的：插入“标准星座连线段（STANDARD_V1）”
-- 兼容：MySQL 8+ / Flyway（不使用变量/存储过程/循环）

START TRANSACTION;

-- =========================================================
-- Orion (Ori)
-- =========================================================
INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 1,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Betelgeuse' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Bellatrix' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 2,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Bellatrix' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Alnilam' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 3,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Alnilam' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Mintaka' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 4,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Mintaka' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Alnitak' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 5,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Alnitak' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Saiph' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 6,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Saiph' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Rigel' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 7,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Rigel' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Betelgeuse' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Ori','Orion', a.id, b.id, 8,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Meissa' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Bellatrix' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Ori'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

-- =========================================================
-- Canis Major（你之前用的 code 是 Can）
-- =========================================================
INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Can','Canis Major', a.id, b.id, 1,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Sirius' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Mirzam' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Can'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Can','Canis Major', a.id, b.id, 2,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Sirius' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Adhara' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Can'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Can','Canis Major', a.id, b.id, 3,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Adhara' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Wezen' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Can'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Can','Canis Major', a.id, b.id, 4,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Wezen' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Aludra' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Can'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

-- =========================================================
-- Lyra (Lyr)
-- =========================================================
INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Lyr','Lyra', a.id, b.id, 1,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Vega' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Sheliak' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Lyr'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Lyr','Lyra', a.id, b.id, 2,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Sheliak' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Sulafat' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Lyr'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'Lyr','Lyra', a.id, b.id, 3,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Sulafat' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Vega' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='Lyr'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

-- =========================================================
-- Ursa Major（如果你后面想画北斗，这组很值；如果不需要可以删掉这一段）
-- code 用 UMa / name 用 Ursa Major（你前端筛选如果只用 3 个星座，也不影响）
-- =========================================================
INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'UMa','Ursa Major', a.id, b.id, 1,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Dubhe' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Merak' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='UMa'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'UMa','Ursa Major', a.id, b.id, 2,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Merak' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Phecda' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='UMa'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'UMa','Ursa Major', a.id, b.id, 3,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Phecda' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Megrez' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='UMa'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'UMa','Ursa Major', a.id, b.id, 4,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Megrez' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Alioth' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='UMa'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'UMa','Ursa Major', a.id, b.id, 5,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Alioth' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Mizar' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='UMa'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

INSERT INTO constellation_line
(constellation_code, constellation_name, start_body_id, end_body_id, sort_order, remark, is_deleted, created_at, updated_at)
SELECT 'UMa','Ursa Major', a.id, b.id, 6,'STANDARD_V1',0,NOW(),NOW()
FROM (SELECT id FROM tb_celestial_body WHERE name='Mizar' LIMIT 1) a
         JOIN (SELECT id FROM tb_celestial_body WHERE name='Alkaid' LIMIT 1) b
WHERE NOT EXISTS (
    SELECT 1 FROM constellation_line cl
    WHERE cl.remark='STANDARD_V1' AND cl.constellation_code='UMa'
      AND cl.start_body_id=a.id AND cl.end_body_id=b.id
);

COMMIT;

-- 验证：看每个星座插入了多少段
SELECT constellation_code, constellation_name, COUNT(*) AS cnt
FROM constellation_line
WHERE remark='STANDARD_V1'
GROUP BY constellation_code, constellation_name
ORDER BY cnt DESC, constellation_code ASC;
