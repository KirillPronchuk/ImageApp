alter table APP_ORDER_LINE add constraint FK_APP_ORDER_LINE_PRODUCT foreign key (PRODUCT_ID) references APP_PRODUCT(ID);
alter table APP_ORDER_LINE add constraint FK_APP_ORDER_LINE_ORDER foreign key (ORDER_ID) references APP_ORDER(ID);
create index IDX_APP_ORDER_LINE_PRODUCT on APP_ORDER_LINE (PRODUCT_ID);
create index IDX_APP_ORDER_LINE_ORDER on APP_ORDER_LINE (ORDER_ID);
