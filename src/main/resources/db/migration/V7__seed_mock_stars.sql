-- V7__seed_mock_stars.sql
-- 目的：补充一批“真实/常用”的恒星数据，保证多个星座能画出明显的线/面
-- 说明：不使用变量、不使用循环，Flyway 可直接执行（MySQL 8+ 兼容）

START TRANSACTION;

-- 可选：如果你想每次都“补齐缺失”，但不重复插入
-- 这里采用 INSERT ... SELECT ... WHERE NOT EXISTS 的方式，哪怕没有唯一索引也不会重复。

INSERT INTO tb_celestial_body
(name, type, constellation, alias, ra_hours, dec_degrees, magnitude, distance_ly, spectral_type)
SELECT * FROM (
                  -- ===== Orion (Ori) =====
                  SELECT 'Betelgeuse' AS name, 'STAR' AS type, 'Orion' AS constellation, NULL AS alias,
                         5.9195 AS ra_hours, 7.4070 AS dec_degrees, 0.42 AS magnitude, 548 AS distance_ly, 'M1-2Ia-ab' AS spectral_type
                  UNION ALL
                  SELECT 'Rigel','STAR','Orion',NULL, 5.2423,-8.2016, 0.12, 863,'B8Ia'
                  UNION ALL
                  SELECT 'Bellatrix','STAR','Orion',NULL, 5.4189, 6.3497, 1.64, 243,'B2III'
                  UNION ALL
                  SELECT 'Saiph','STAR','Orion',NULL, 5.7959,-9.6696, 2.07, 650,'B0.5Ia'
                  UNION ALL
                  SELECT 'Mintaka','STAR','Orion',NULL, 5.5334,-0.2991, 2.25, 1200,'O9.5II'
                  UNION ALL
                  SELECT 'Alnilam','STAR','Orion',NULL, 5.6036,-1.2019, 1.69, 2000,'B0Ia'
                  UNION ALL
                  SELECT 'Alnitak','STAR','Orion',NULL, 5.6793,-1.9426, 1.74, 800,'O9.5Iab'
                  UNION ALL
                  SELECT 'Meissa','STAR','Orion',NULL, 5.5881, 9.9342, 3.39, 1100,'O8III'

                  -- ===== Canis Major (CMa) =====
                  UNION ALL
                  SELECT 'Sirius','STAR','Canis Major','tianlangxing', 6.7525,-16.7161,-1.46, 8.6,'A1V'
                  UNION ALL
                  SELECT 'Mirzam','STAR','Canis Major',NULL, 6.3783,-17.9559, 1.98, 500,'B1II'
                  UNION ALL
                  SELECT 'Adhara','STAR','Canis Major',NULL, 6.9771,-28.9721, 1.50, 430,'B2II'
                  UNION ALL
                  SELECT 'Wezen','STAR','Canis Major',NULL, 7.1399,-26.3932, 1.83, 1600,'F8Ia'
                  UNION ALL
                  SELECT 'Aludra','STAR','Canis Major',NULL, 7.4016,-29.3031, 2.45, 2000,'B5Ia'

                  -- ===== Lyra (Lyr) =====
                  UNION ALL
                  SELECT 'Vega','STAR','Lyra',NULL, 18.6156,38.7837, 0.03, 25,'A0V'
                  UNION ALL
                  SELECT 'Sheliak','STAR','Lyra',NULL, 18.8346,33.3627, 3.52, 960,'B7V'
                  UNION ALL
                  SELECT 'Sulafat','STAR','Lyra',NULL, 18.9820,32.6896, 3.25, 620,'B9III'

                  -- ===== Ursa Major (UMa) - Big Dipper =====
                  UNION ALL
                  SELECT 'Dubhe','STAR','Ursa Major',NULL, 11.0621,61.7508, 1.79, 123,'K0III'
                  UNION ALL
                  SELECT 'Merak','STAR','Ursa Major',NULL, 11.0307,56.3824, 2.37, 79,'A1V'
                  UNION ALL
                  SELECT 'Phecda','STAR','Ursa Major',NULL, 11.8972,53.6948, 2.44, 84,'A0V'
                  UNION ALL
                  SELECT 'Megrez','STAR','Ursa Major',NULL, 12.2571,57.0326, 3.32, 81,'A3V'
                  UNION ALL
                  SELECT 'Alioth','STAR','Ursa Major',NULL, 12.9005,55.9598, 1.76, 81,'A0p'
                  UNION ALL
                  SELECT 'Mizar','STAR','Ursa Major',NULL, 13.3987,54.9254, 2.06, 86,'A2V'
                  UNION ALL
                  SELECT 'Alkaid','STAR','Ursa Major',NULL, 13.7923,49.3133, 1.85, 104,'B3V'

                  -- ===== Polaris / Little Dipper (UMi) =====
                  UNION ALL
                  SELECT 'Polaris','STAR','Ursa Minor',NULL, 2.5303,89.2641, 1.98, 433,'F7Ib'

              ) AS src
WHERE NOT EXISTS (
    SELECT 1 FROM tb_celestial_body t
    WHERE t.name = src.name
);

COMMIT;
