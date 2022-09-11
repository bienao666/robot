-- ----------------------------
-- Table structure for systemParam
-- ----------------------------
create table if not exists systemParam (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           code varchar UNIQUE,
                                           codeName varchar,
                                           value varchar,
                                           isShow INTEGER DEFAULT 1,
                                           created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                           updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
create table if not exists user (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           wxid varchar,
                                           wxName varchar,
                                           city varchar,
                                           ip varchar,
                                           functionType INTEGER,
                                           created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                           updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
create table if not exists `group` (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       groupid varchar,
                                       groupName varchar,
                                       functionType INTEGER,
                                       created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                       updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
create table if not exists `ql` (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       url varchar UNIQUE,
                                       clientID varchar,
                                       clientSecret INTEGER,
                                       head varchar,
                                       remark varchar,
                                       created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                       updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
create table if not exists `wire` (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    activity_name varchar,
                                    script varchar UNIQUE,
                                    status varchar DEFAULT '未执行',
                                    created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                    updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
create table if not exists `wireKey` (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      wireId INTEGER,
                                      key varchar,
                                      created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                      updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
create table if not exists `wirelist` (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         script varchar,
                                         content varchar UNIQUE,
                                         result varchar,
                                         created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                         updated_time datetime DEFAULT CURRENT_TIMESTAMP
);
alter table `ql` add `tokenType` varchar after clientSecret;
alter table `ql` add `token` varchar after tokenType;
