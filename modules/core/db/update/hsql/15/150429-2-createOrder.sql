alter table APP_ORDER add constraint FK_APP_ORDER_CUSTOMER_ID foreign key (CUSTOMER_ID) references APP_CUSTOMER(ID);
create index IDX_APP_ORDER_CUSTOMER on APP_ORDER (CUSTOMER_ID);
