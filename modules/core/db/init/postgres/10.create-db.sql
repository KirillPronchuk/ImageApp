-- begin APP_IMAGE
create table APP_IMAGE (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    IMAGE_FILE_ID uuid,
    --
    primary key (ID)
)^
-- end APP_IMAGE
