create type attachment_storage_type as enum ('file', 'cdn');

create table attachment
(
    id           bigserial primary key,
    storage_type attachment_storage_type                not null,
    file_name    varchar(256)                           not null,
    mime_type    varchar(128),
    size         bigint                                 not null,
    resource_uri varchar(256)                           not null,
    created_at   timestamp with time zone default now() not null,
    updated_at   timestamp with time zone,
    deleted_at   timestamp with time zone
);