-- ----------------------------
-- Table structure for systemParam
-- ----------------------------
create table if not exists systemParam (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           code varchar UNIQUE,
                                           codeName varchar,
                                           value varchar,
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
                                       created_time datetime DEFAULT CURRENT_TIMESTAMP,
                                       updated_time datetime DEFAULT CURRENT_TIMESTAMP
);