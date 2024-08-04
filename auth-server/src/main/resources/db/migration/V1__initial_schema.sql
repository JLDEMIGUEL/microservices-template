create table "USER"
(
    ID       UUID not null primary key,
    EMAIL    CHARACTER VARYING(255)
        constraint UKOB8KQYQQGMEFL0ACO34AKDTPE unique,
    PASSWORD CHARACTER VARYING(255),
    ROLE     CHARACTER VARYING(255),
    USERNAME CHARACTER VARYING(255)
        constraint UKSB8BBOUER5WAK8VYIIY4PF2BX unique
);

create table "PRODUCT"
(
    ID                 UUID                  not null primary key,
    CREATED_BY         CHARACTER VARYING(50) not null,
    CREATED_DATE       TIMESTAMP WITH TIME ZONE,
    LAST_MODIFIED_BY   CHARACTER VARYING(50),
    LAST_MODIFIED_DATE TIMESTAMP WITH TIME ZONE,
    NAME               CHARACTER VARYING(255),
    PRICE              REAL
);

create table "ORDER"
(
    ID                 UUID                  not null primary key,
    CREATED_BY         CHARACTER VARYING(50) not null,
    CREATED_DATE       TIMESTAMP WITH TIME ZONE,
    LAST_MODIFIED_BY   CHARACTER VARYING(50),
    LAST_MODIFIED_DATE TIMESTAMP WITH TIME ZONE,
    PRODUCT_ID         UUID,
    USER_ID            UUID
);