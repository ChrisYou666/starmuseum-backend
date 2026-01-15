ALTER TABLE user_privacy_setting
  ADD COLUMN default_location_visibility VARCHAR(16) NOT NULL DEFAULT 'HIDDEN' AFTER post_visibility_default,
    ADD COLUMN exact_location_public_strategy VARCHAR(16) NOT NULL DEFAULT 'FUZZY' AFTER default_location_visibility;

-- 兜底（理论上 NOT NULL + DEFAULT 已足够，但企业里通常会保守补一刀）
UPDATE user_privacy_setting
SET default_location_visibility = 'HIDDEN'
WHERE default_location_visibility IS NULL;

UPDATE user_privacy_setting
SET exact_location_public_strategy = 'FUZZY'
WHERE exact_location_public_strategy IS NULL;
