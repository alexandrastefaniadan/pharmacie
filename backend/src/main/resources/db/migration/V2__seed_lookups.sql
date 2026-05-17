-- =====================================================================
-- V2 — Seed lookup tables with reasonable defaults for a Moroccan officine
-- =====================================================================
-- All labels in French (primary language in Moroccan pharmacies).
-- Codes are STABLE identifiers (ASCII, UPPER_SNAKE_CASE) — never rename.
-- =====================================================================

-- ---------------- Pharmaceutical forms (formes galéniques) ----------------
INSERT INTO pharmaceutical_form (code, label_fr, sort_order) VALUES
    ('TABLET',            'Comprimé',                       10),
    ('TABLET_COATED',     'Comprimé pelliculé',             11),
    ('TABLET_EFFERVESCENT','Comprimé effervescent',         12),
    ('CAPSULE',           'Gélule',                         20),
    ('SACHET',            'Sachet (poudre à diluer)',       30),
    ('SYRUP',             'Sirop',                          40),
    ('SOLUTION_ORAL',     'Solution buvable',               41),
    ('SUSPENSION',        'Suspension buvable',             42),
    ('DROPS_ORAL',        'Gouttes buvables',               43),
    ('CREAM',             'Crème',                          50),
    ('OINTMENT',          'Pommade',                        51),
    ('GEL',               'Gel',                            52),
    ('LOTION',            'Lotion',                         53),
    ('SUPPOSITORY',       'Suppositoire',                   60),
    ('OVULE',             'Ovule',                          61),
    ('EYE_DROPS',         'Collyre',                        70),
    ('EAR_DROPS',         'Gouttes auriculaires',           71),
    ('NOSE_DROPS',        'Gouttes nasales',                72),
    ('NASAL_SPRAY',       'Spray nasal',                    73),
    ('SPRAY',             'Spray',                          74),
    ('INHALER',           'Inhalateur',                     80),
    ('PATCH',             'Patch',                          90),
    ('INJECTION',         'Injectable',                    100),
    ('PARAPHARMACY',      'Parapharmacie',                 200);


-- ---------------- Age groups ----------------
INSERT INTO age_group (code, label_fr, sort_order) VALUES
    ('INFANT',     'Nourrisson (0–2 ans)',     10),
    ('CHILD',      'Enfant (2–12 ans)',        20),
    ('ADOLESCENT', 'Adolescent (12–18 ans)',   30),
    ('ADULT',      'Adulte',                    40),
    ('ELDERLY',    'Personne âgée',             50),
    ('PREGNANCY',  'Grossesse / allaitement',   60);


-- ---------------- Therapeutic classes ----------------
INSERT INTO therapeutic_class (code, label_fr, sort_order) VALUES
    ('ANTIBIOTIC',         'Antibiotique',                10),
    ('ANTIVIRAL',          'Antiviral',                   11),
    ('ANTIFUNGAL',         'Antifongique',                12),
    ('ANTISEPTIC',         'Antiseptique',                13),
    ('ANALGESIC',          'Antalgique',                  20),
    ('ANTIPYRETIC',        'Antipyrétique',               21),
    ('ANTI_INFLAMMATORY',  'Anti-inflammatoire (AINS)',   22),
    ('ANTIHISTAMINE',      'Antihistaminique',            30),
    ('ANTITUSSIVE',        'Antitussif',                  31),
    ('EXPECTORANT',        'Expectorant',                 32),
    ('BRONCHODILATOR',     'Bronchodilatateur',           33),
    ('VITAMIN',            'Vitamine / complément',       40),
    ('MINERAL',            'Minéraux',                    41),
    ('DERMATOLOGY',        'Dermatologie',                50),
    ('OPHTHALMOLOGY',      'Ophtalmologie',               51),
    ('OTOLOGY',            'ORL',                         52),
    ('GASTRO',             'Gastro-entérologie',          60),
    ('LAXATIVE',           'Laxatif',                     61),
    ('ANTIDIARRHEAL',      'Antidiarrhéique',             62),
    ('CARDIO',             'Cardiologie',                 70),
    ('HYPERTENSION',       'Antihypertenseur',            71),
    ('DIABETES',           'Antidiabétique',              72),
    ('RESPIRATORY',        'Voies respiratoires',         80),
    ('CONTRACEPTION',      'Contraception',               90),
    ('HORMONAL',           'Hormonal',                    91);


-- ---------------- Indications / usages ----------------
INSERT INTO indication (code, label_fr, sort_order) VALUES
    ('COUGH_DRY',         'Toux sèche',                  10),
    ('COUGH_WET',         'Toux grasse',                 11),
    ('FEVER',             'Fièvre',                      20),
    ('PAIN',              'Douleur',                     21),
    ('HEADACHE',          'Mal de tête',                 22),
    ('MIGRAINE',          'Migraine',                    23),
    ('SORE_THROAT',       'Mal de gorge',                30),
    ('COLD',              'Rhume',                       31),
    ('FLU',               'Grippe',                      32),
    ('SINUSITIS',         'Sinusite',                    33),
    ('OTITIS',            'Otite',                       34),
    ('BRONCHITIS',        'Bronchite',                   35),
    ('ASTHMA',            'Asthme',                      36),
    ('WOUND_HEALING',     'Cicatrisation',               40),
    ('BURN',              'Brûlure',                     41),
    ('ECZEMA',            'Eczéma',                      42),
    ('ACNE',              'Acné',                        43),
    ('MYCOSIS',           'Mycose cutanée',              44),
    ('ITCHING',           'Démangeaisons',               45),
    ('CONJUNCTIVITIS',    'Conjonctivite',               50),
    ('DRY_EYE',           'Œil sec',                     51),
    ('URINARY_INFECTION', 'Infection urinaire',          60),
    ('VAGINAL_INFECTION', 'Infection vaginale',          61),
    ('DIARRHEA',          'Diarrhée',                    70),
    ('CONSTIPATION',      'Constipation',                71),
    ('NAUSEA',            'Nausées / vomissements',      72),
    ('HEARTBURN',         'Brûlures d''estomac',         73),
    ('BLOATING',          'Ballonnements',               74),
    ('ALLERGY',           'Allergie',                    80),
    ('INSOMNIA',          'Insomnie',                    81),
    ('ANXIETY',           'Anxiété légère',              82),
    ('FATIGUE',           'Fatigue',                     83),
    ('IMMUNE_BOOST',      'Renforcement immunitaire',    84);

