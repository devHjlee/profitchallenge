CREATE USER 'profit'@'%' IDENTIFIED BY '1234';
GRANT Create role ON *.* TO 'profit'@'%';
GRANT Create user ON *.* TO 'profit'@'%';
GRANT Drop role ON *.* TO 'profit'@'%';
GRANT Event ON *.* TO 'profit'@'%';
GRANT File ON *.* TO 'profit'@'%';
GRANT Process ON *.* TO 'profit'@'%';
GRANT Reload ON *.* TO 'profit'@'%';
GRANT Replication client ON *.* TO 'profit'@'%';
GRANT Replication slave ON *.* TO 'profit'@'%';
GRANT Show databases ON *.* TO 'profit'@'%';
GRANT Shutdown ON *.* TO 'profit'@'%';
GRANT Super ON *.* TO 'profit'@'%';
GRANT Create tablespace ON *.* TO 'profit'@'%';
GRANT Usage ON *.* TO 'profit'@'%';
GRANT SYSTEM_USER ON *.* TO 'profit'@'%';
GRANT XA_RECOVER_ADMIN ON *.* TO 'profit'@'%';
GRANT SHOW_ROUTINE ON *.* TO 'profit'@'%';
GRANT SYSTEM_VARIABLES_ADMIN ON *.* TO 'profit'@'%';
GRANT SET_USER_ID ON *.* TO 'profit'@'%';
GRANT SESSION_VARIABLES_ADMIN ON *.* TO 'profit'@'%';
GRANT TABLE_ENCRYPTION_ADMIN ON *.* TO 'profit'@'%';
GRANT SERVICE_CONNECTION_ADMIN ON *.* TO 'profit'@'%';
GRANT SENSITIVE_VARIABLES_OBSERVER ON *.* TO 'profit'@'%';
GRANT ROLE_ADMIN ON *.* TO 'profit'@'%';
GRANT RESOURCE_GROUP_USER ON *.* TO 'profit'@'%';
GRANT APPLICATION_PASSWORD_ADMIN ON *.* TO 'profit'@'%';
GRANT REPLICATION_APPLIER ON *.* TO 'profit'@'%';
GRANT CLONE_ADMIN ON *.* TO 'profit'@'%';
GRANT CONNECTION_ADMIN ON *.* TO 'profit'@'%';
GRANT REPLICATION_SLAVE_ADMIN ON *.* TO 'profit'@'%';
GRANT FLUSH_USER_RESOURCES ON *.* TO 'profit'@'%';
GRANT BINLOG_ENCRYPTION_ADMIN ON *.* TO 'profit'@'%';
GRANT BINLOG_ADMIN ON *.* TO 'profit'@'%';
GRANT PERSIST_RO_VARIABLES_ADMIN ON *.* TO 'profit'@'%';
GRANT INNODB_REDO_LOG_ARCHIVE ON *.* TO 'profit'@'%';
GRANT FLUSH_STATUS ON *.* TO 'profit'@'%';
GRANT FLUSH_OPTIMIZER_COSTS ON *.* TO 'profit'@'%';
GRANT FIREWALL_EXEMPT ON *.* TO 'profit'@'%';
GRANT RESOURCE_GROUP_ADMIN ON *.* TO 'profit'@'%';
GRANT ENCRYPTION_KEY_ADMIN ON *.* TO 'profit'@'%';
GRANT AUDIT_ABORT_EXEMPT ON *.* TO 'profit'@'%';
GRANT GROUP_REPLICATION_STREAM ON *.* TO 'profit'@'%';
GRANT GROUP_REPLICATION_ADMIN ON *.* TO 'profit'@'%';
GRANT PASSWORDLESS_USER_ADMIN ON *.* TO 'profit'@'%';
GRANT FLUSH_TABLES ON *.* TO 'profit'@'%';
GRANT BACKUP_ADMIN ON *.* TO 'profit'@'%';
GRANT AUTHENTICATION_POLICY_ADMIN ON *.* TO 'profit'@'%';
GRANT INNODB_REDO_LOG_ENABLE ON *.* TO 'profit'@'%';
GRANT AUDIT_ADMIN ON *.* TO 'profit'@'%';
GRANT Grant option ON *.* TO 'profit'@'%';
GRANT ALL PRIVILEGES ON profit.* TO 'profit'@'%';
FLUSH PRIVILEGES;
-----------------------------------------------------------------
-----------------------------------------------------------------
-----------------------------------------------------------------

drop table SYMBOL;
CREATE TABLE SYMBOL (
                        symbol VARCHAR(255) PRIMARY KEY,
                        min_Order_Qty double,
                        min_Price double
);
drop table upbit;
CREATE TABLE upbit (
                       id    VARCHAR(255) PRIMARY KEY,
                       title VARCHAR(255),
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
drop table bithub;
CREATE TABLE bithub (
    title VARCHAR(255) PRIMARY KEY,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table coinbase;
CREATE TABLE coinbase (
                          symbol VARCHAR(255) PRIMARY KEY,
                          title VARCHAR(255),
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
drop table bybit;
CREATE TABLE bybit (
                       symbol    VARCHAR(255) PRIMARY KEY,
                       title VARCHAR(255),
                       description VARCHAR(255),
                       url VARCHAR(255),
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
drop table buy;
CREATE TABLE buy (
                     symbol VARCHAR(255),
                     exchange VARCHAR(255),
                     price double,
                     stop_price double,
                     take_price double,
                     qty double,
                     success_yn VARCHAR(255),
                     error_message TEXT,
                     buy_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table exchange;
CREATE TABLE exchange (
                          exchange VARCHAR(255) PRIMARY KEY,
                          margin double,
                          stop_loss double,
                          take_profit double
);

drop table job;
-- profit.exchange_job definition

CREATE TABLE `job` (
                                `job_name` varchar(255) NOT NULL,
                                `crontab` varchar(255) NOT NULL,
                                `stop_time` datetime DEFAULT NULL,
                                PRIMARY KEY (`job_name`)
);