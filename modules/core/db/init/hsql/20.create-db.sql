-- begin APP_ORDER
alter table APP_ORDER add constraint FK_APP_ORDER_CUSTOMER_ID foreign key (CUSTOMER_ID) references APP_CUSTOMER(ID)^
create index IDX_APP_ORDER_CUSTOMER on APP_ORDER (CUSTOMER_ID)^
-- end APP_ORDER
-- begin APP_ORDER_LINE
alter table APP_ORDER_LINE add constraint FK_APP_ORDER_LINE_PRODUCT_ID foreign key (PRODUCT_ID) references APP_PRODUCT(ID)^
alter table APP_ORDER_LINE add constraint FK_APP_ORDER_LINE_ORDER_ID foreign key (ORDER_ID) references APP_ORDER(ID)^
create index IDX_APP_ORDER_LINE_ORDER on APP_ORDER_LINE (ORDER_ID)^
create index IDX_APP_ORDER_LINE_PRODUCT on APP_ORDER_LINE (PRODUCT_ID)^
-- end APP_ORDER_LINE
-- begin APP_IMAGE
alter table APP_IMAGE add constraint FK_APP_IMAGE_IMAGE_FILE foreign key (IMAGE_FILE_ID) references SYS_FILE(ID)^
create index IDX_APP_IMAGE_IMAGE_FILE on APP_IMAGE (IMAGE_FILE_ID)^
-- end APP_IMAGE
