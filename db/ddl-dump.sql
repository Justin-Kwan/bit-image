-- Table Definition ----------------------------------------------

CREATE TABLE users (
    id uuid PRIMARY KEY,
    created_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, CURRENT_TIMESTAMP),
    updated_at timestamp without time zone NOT NULL DEFAULT timezone('UTC'::text, CURRENT_TIMESTAMP),
    image_upload_limit integer,
    image_upload_count integer
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX pk_users ON users(id uuid_ops);

-- Table Definition ----------------------------------------------

CREATE TABLE images (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    name character varying(250) NOT NULL,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hash_md5 uuid NOT NULL,
    size_bytes bigint NOT NULL,
    file_format character varying(50) NOT NULL,
    is_private boolean NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX pk_images ON images(id uuid_ops);
CREATE INDEX ix_images_user_id ON images(user_id uuid_ops);
CREATE INDEX ix_images_user_id_id ON images(user_id uuid_ops,id uuid_ops);
CREATE INDEX ix_images_user_id_name ON images(user_id uuid_ops,name text_ops);
CREATE INDEX ix_images_updated_at ON images(updated_at timestamp_ops DESC);

-- Table Definition ----------------------------------------------

CREATE TABLE tags (
    id uuid PRIMARY KEY,
    name character varying(250) NOT NULL
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX pk_tags ON tags(id uuid_ops);
CREATE UNIQUE INDEX ux_tags_name ON tags(name text_ops);

-- Table Definition ----------------------------------------------

CREATE TABLE image_tags (
    image_id uuid REFERENCES images(id) ON DELETE CASCADE,
    tag_id uuid REFERENCES tags(id) ON DELETE CASCADE,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    CONSTRAINT pk_image_tags PRIMARY KEY (image_id, tag_id)
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX pk_image_tags ON image_tags(image_id uuid_ops,tag_id uuid_ops);

-- Table Definition ----------------------------------------------

CREATE TABLE content_labels (
    id uuid PRIMARY KEY,
    name character varying(250) NOT NULL,
    content_category character varying(250) NOT NULL,
    CONSTRAINT uk_content_labels_name_content_category UNIQUE (name, content_category)
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX pk_content_labels ON content_labels(id uuid_ops);
CREATE UNIQUE INDEX uk_content_labels_name_content_category ON content_labels(name text_ops,content_category text_ops);
CREATE INDEX ux_content_labels_name_content_category ON content_labels(name text_ops,content_category text_ops);

-- Table Definition ----------------------------------------------

CREATE TABLE image_content_labels (
    image_id uuid REFERENCES images(id) ON DELETE CASCADE,
    label_id uuid REFERENCES content_labels(id) ON DELETE CASCADE,
    label_confidence_score double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    CONSTRAINT pk_image_content_labels PRIMARY KEY (image_id, label_id)
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX pk_image_content_labels ON image_content_labels(image_id uuid_ops,label_id uuid_ops);
