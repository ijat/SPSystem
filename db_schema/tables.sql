drop database spsdb_v1;
CREATE database spsdb_v1;
USE spsdb_v1;
CREATE TABLE user_data
(
UID int NOT NULL unique,
USERNAME varchar(255) NOT NULL unique,
FULLNAME text NOT NULL,
PASS text NOT NULL,
IR_ID int NOT NULL unique,
COUNTER int NOT NULL,
UHASH TEXT NOT NULL,
PRIMARY KEY (UID)
)
