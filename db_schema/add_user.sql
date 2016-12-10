USE spsdb_v1;
CREATE USER 'sps_server'@'localhost' IDENTIFIED BY 'sps_passwd';
GRANT ALL PRIVILEGES ON spsdb_v1.* TO 'sps_server'@'localhost';