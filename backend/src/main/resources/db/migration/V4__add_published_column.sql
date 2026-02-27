-- Ajouter la colonne published avec une valeur par défaut false
ALTER TABLE images ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT false;

-- Mettre à jour les enregistrements existants
UPDATE images SET published = false WHERE published IS NULL; 