CREATE TABLE IF NOT EXISTS templates
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    parameters JSONB
);

COMMENT ON TABLE templates IS 'Шаблоны';
COMMENT ON COLUMN templates.id IS 'ID шаблона';
COMMENT ON COLUMN templates.name IS 'Название шаблона';
COMMENT ON COLUMN templates.parameters IS 'Параметры шаблона';