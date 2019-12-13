CREATE TYPE UNIT_TYPE AS ENUM ('METRIC', 'US_CUSTOMARY');
CREATE TYPE DATE_TYPE AS ENUM ('TYPE_1', 'TYPE_2', 'TYPE_3', 'TYPE_4', 'TYPE_5');
CREATE TYPE TIME_TYPE AS ENUM ('TWENTY_FOUR', 'TWELVE');

CREATE TABLE setting (
  id                int primary key,
  country           varchar(10)  not null,
  language          varchar(10)  not null,
  zone_id           varchar(100) not null,
  date_type         DATE_TYPE    not null,
  time_type         TIME_TYPE    not null,
  unit_type         UNIT_TYPE    not null,
  password          varchar(255) not null,
  is_auto_safe_off  boolean      not null,
  auto_safe_off_min int          not null
);

insert into setting
values (1, 'US', 'en', 'Asia/Seoul', 'TYPE_1', 'TWELVE', 'METRIC', 'admin', true, 5);