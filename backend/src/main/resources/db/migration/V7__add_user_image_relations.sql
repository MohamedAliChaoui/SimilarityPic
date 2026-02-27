-- Ajout des colonnes pour la gestion personnalisée des images par utilisateur
ALTER TABLE images ADD COLUMN IF NOT EXISTS user_favorite BOOLEAN DEFAULT FALSE;
ALTER TABLE images ADD COLUMN IF NOT EXISTS user_gallery BOOLEAN DEFAULT TRUE;
ALTER TABLE images ADD COLUMN IF NOT EXISTS last_viewed_at TIMESTAMP;
ALTER TABLE images ADD COLUMN IF NOT EXISTS view_count INTEGER DEFAULT 0; 