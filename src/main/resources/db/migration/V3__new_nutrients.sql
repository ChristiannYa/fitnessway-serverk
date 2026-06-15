-- 1. Create nutrient_config table
CREATE TABLE nutrient_config
(
    nutrient_id INTEGER PRIMARY KEY REFERENCES nutrients (id) ON DELETE CASCADE,
    parent_id   INTEGER REFERENCES nutrients (id) ON DELETE CASCADE,
    sort_order  INTEGER NOT NULL UNIQUE
);

-- 2. Insert config for existing nutrients
INSERT INTO nutrient_config (nutrient_id, sort_order)
VALUES (1, 1),   -- Calories
       (2, 2),   -- Carbs
       (4, 3),   -- Fats
       (6, 9),   -- Protein
       (3, 10),  -- Cholesterol
       (5, 11),  -- Fiber
       (7, 13),  -- Sodium
       (8, 14),  -- Sugar
       (9, 16),  -- Vitamin A
       (10, 23), -- Vitamin B12
       (11, 24), -- Vitamin C
       (12, 25), -- Vitamin D
       (13, 28), -- Calcium
       (14, 29), -- Iron
       (15, 30), -- Magnesium
       (16, 31);
-- Potassium

-- 3. Insert new nutrients
INSERT INTO nutrients (name, symbol, unit, type, is_premium)
VALUES
    -- Basic: Fat subtypes
    ('Saturated Fat', NULL, 'g', 'basic', true),
    ('Trans Fat', NULL, 'g', 'basic', true),
    ('Polyunsaturated Fat', NULL, 'g', 'basic', true),
    ('Monounsaturated Fat', NULL, 'g', 'basic', true),
    ('Omega-3', NULL, 'g', 'basic', true),
    -- Basic: Sugar subtype
    ('Added Sugar', NULL, 'g', 'basic', true),
    -- Basic: Fiber subtype
    ('Dietary Fiber', NULL, 'g', 'basic', true),
    -- Vitamins
    ('Thiamin', 'B1', 'mg', 'vitamin', true),
    ('Niacin', 'B3', 'mg', 'vitamin', true),
    ('Pantothenic Acid', 'B5', 'mg', 'vitamin', true),
    ('B6', 'B6', 'mg', 'vitamin', true),
    ('Biotin', 'B7', 'mcg', 'vitamin', true),
    ('Folate', 'B9', 'mcg', 'vitamin', true),
    ('E', 'E', 'mg', 'vitamin', true),
    ('K', 'K', 'mcg', 'vitamin', true),
    -- Minerals
    ('Zinc', 'Zn', 'mg', 'mineral', true),
    ('Selenium', 'Se', 'mcg', 'mineral', true),
    ('Phosphorus', 'P', 'mg', 'mineral', true),
    ('Manganese', 'Mn', 'mg', 'mineral', true),
    ('Iodine', 'I', 'mcg', 'mineral', true),
    ('Copper', 'Cu', 'mg', 'mineral', true);

-- 4. Insert config for new nutrients
INSERT INTO nutrient_config (nutrient_id, parent_id, sort_order)
VALUES
    -- Fat subtypes (children of Fats, id=4)
    ((SELECT id FROM nutrients WHERE name = 'Saturated Fat'), 4, 4),
    ((SELECT id FROM nutrients WHERE name = 'Trans Fat'), 4, 5),
    ((SELECT id FROM nutrients WHERE name = 'Polyunsaturated Fat'), 4, 6),
    ((SELECT id FROM nutrients WHERE name = 'Monounsaturated Fat'), 4, 7),
    ((SELECT id FROM nutrients WHERE name = 'Omega-3'), 4, 8),
    -- Fiber subtype (child of Fiber, id=5)
    ((SELECT id FROM nutrients WHERE name = 'Dietary Fiber'), 5, 12),
    -- Sugar subtype (child of Sugar, id=8)
    ((SELECT id FROM nutrients WHERE name = 'Added Sugar'), 8, 15),
    -- Vitamins
    ((SELECT id FROM nutrients WHERE name = 'Thiamin'), NULL, 17),
    ((SELECT id FROM nutrients WHERE name = 'Niacin'), NULL, 18),
    ((SELECT id FROM nutrients WHERE name = 'Pantothenic Acid'), NULL, 19),
    ((SELECT id FROM nutrients WHERE name = 'B6'), NULL, 20),
    ((SELECT id FROM nutrients WHERE name = 'Biotin'), NULL, 21),
    ((SELECT id FROM nutrients WHERE name = 'Folate'), NULL, 22),
    ((SELECT id FROM nutrients WHERE name = 'E'), NULL, 26),
    ((SELECT id FROM nutrients WHERE name = 'K'), NULL, 27),
    -- Minerals
    ((SELECT id FROM nutrients WHERE name = 'Zinc'), NULL, 32),
    ((SELECT id FROM nutrients WHERE name = 'Selenium'), NULL, 33),
    ((SELECT id FROM nutrients WHERE name = 'Phosphorus'), NULL, 34),
    ((SELECT id FROM nutrients WHERE name = 'Manganese'), NULL, 35),
    ((SELECT id FROM nutrients WHERE name = 'Iodine'), NULL, 36),
    ((SELECT id FROM nutrients WHERE name = 'Copper'), NULL, 37);
