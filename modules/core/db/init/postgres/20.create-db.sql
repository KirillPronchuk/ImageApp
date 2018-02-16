-- begin APP_IMAGE
alter table APP_IMAGE add constraint FK_APP_IMAGE_IMAGE_FILE foreign key (IMAGE_FILE_ID) references SYS_FILE(ID)^
create index IDX_APP_IMAGE_IMAGE_FILE on APP_IMAGE (IMAGE_FILE_ID)^
-- end APP_IMAGE
