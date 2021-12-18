create schema foo version '1.0';

create sequence plan_sequence;
create table plan
(
    id   int not null default nextval(plan_sequence),
    point varchar(30),
    constraint pk_plan primary key (id)
);
