create schema security version '1.2';

create sequence security_log_sequence;

create table email
(
    email             varchar(255) not null primary key,
    /**
      Verification code generated for this email
     */
    verification_code varchar(16)  not null,
    /**
      The latest time the verification code was sent.
      We will not send the code more often than once in an hour.
     */
    code_sent         datetime,
    /**
      Is this email verified?
     */
    verified          bit          not null default false,
    /**
      Number of attempts to enter the wrong code
     */
    attempts_left     int          not null default 5
);

create table user
(
    username     varchar(255) not null primary key,
    password     varchar(127) not null,
    is_active    bit          not null default true,
    email        varchar(255) null foreign key references email(email),
    admin        bit          not null default false,
    operator     bit          not null default false
);

create table SecurityLog (
  id int not null default nextval(security_log_sequence) primary key,
  username varchar(255) not null foreign key references user(username),
    /**{
        "approvalValue": "GETDATE"
        }*/
  timestamp datetime    not null default getdate(),
  description varchar(255) not null,
  entity_type varchar(255) not null,
  entity_id int not null,
  affiliate_id int null,
  duration_ms int not null
);

create index security_log_aff_idx on SecurityLog(affiliate_id);
create index security_log_usr_idx on SecurityLog(username);
