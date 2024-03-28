CREATE TABLE IF NOT EXISTS templates
(
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parameters  JSONB
);

COMMENT ON TABLE templates IS 'Шаблоны';
COMMENT ON COLUMN templates.id IS 'ID шаблона';
COMMENT ON COLUMN templates.code IS 'Код шаблона';
COMMENT ON COLUMN templates.name IS 'Название шаблона';
COMMENT ON COLUMN templates.description IS 'Описание шаблона';
COMMENT ON COLUMN templates.parameters IS 'Параметры шаблона';