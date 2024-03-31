CREATE TABLE IF NOT EXISTS templates
(
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parameters  JSONB,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

COMMENT ON TABLE templates IS 'Шаблоны';
COMMENT ON COLUMN templates.id IS 'ID';
COMMENT ON COLUMN templates.code IS 'Код';
COMMENT ON COLUMN templates.name IS 'Название';
COMMENT ON COLUMN templates.description IS 'Описание';
COMMENT ON COLUMN templates.parameters IS 'Параметры';
COMMENT ON COLUMN templates.created_at IS 'Время создания';
COMMENT ON COLUMN templates.updated_at IS 'Время последнего изменения';