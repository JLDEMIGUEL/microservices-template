create table "ORDERS"
(
    ID                 UUID                  not null primary key,
    CREATED_BY         CHARACTER VARYING(50) not null,
    CREATED_DATE       TIMESTAMP WITH TIME ZONE,
    LAST_MODIFIED_BY   CHARACTER VARYING(50),
    LAST_MODIFIED_DATE TIMESTAMP WITH TIME ZONE,
    PRODUCT_ID         UUID,
    USER_ID            UUID
);