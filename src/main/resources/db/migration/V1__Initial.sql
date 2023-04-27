create table short_url
(
    short_url   varchar(255)          not null
        constraint short_url_pk primary key,
    long_url    varchar(512)     not null,
    visit_count BIGINT default 0 not null,
    last_visit  DATE,
    username    varchar(255)         not null

);

create table "user"
(
    username varchar(255)     not null
        constraint user_pk
            primary key,
    password varchar(512) not null
);