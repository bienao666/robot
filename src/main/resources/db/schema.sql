-- ----------------------------
-- Table structure for systemParam
-- ----------------------------
create table if not exists systemParam (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           code varchar UNIQUE,
                                           codeName varchar,
                                           value varchar,
                                           isShow INTEGER DEFAULT 1,
                                           created_time varchar DEFAULT (datetime('now', 'localtime')),
                                           updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists user (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           wxid varchar UNIQUE,
                                           wxName varchar,
                                           jd_pt_pin varchar,
                                           wxpusheruid varchar,
                                           city varchar,
                                           ip varchar,
                                           functionType INTEGER,
                                           status INTEGER DEFAULT 0,
                                           `level` INTEGER DEFAULT 0,
                                           created_time varchar DEFAULT (datetime('now', 'localtime')),
                                           updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
ALTER TABLE `user` ADD COLUMN `status` INTEGER DEFAULT 0;
ALTER TABLE `user` ADD COLUMN `level` INTEGER DEFAULT 0;
create table if not exists `group` (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       groupid varchar,
                                       groupName varchar,
                                       functionType INTEGER,
                                       created_time varchar DEFAULT (datetime('now', 'localtime')),
                                       updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `ql` (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       url varchar UNIQUE,
                                       clientID varchar,
                                       clientSecret INTEGER,
                                       tokenType varchar,
                                       token varchar,
                                       head varchar,
                                       remark varchar UNIQUE,
                                       `type` INTEGER DEFAULT 0,
                                       created_time varchar DEFAULT (datetime('now', 'localtime')),
                                       updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
alter TABLE  `ql` add `type` INTEGER DEFAULT 0;
create table if not exists `wire` (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    activity_name varchar,
                                    script varchar UNIQUE,
                                    set_head INTEGER DEFAULT 0,
                                    status INTEGER DEFAULT 0,
                                    created_time varchar DEFAULT (datetime('now', 'localtime')),
                                    updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
alter TABLE  `wire` add set_head INTEGER DEFAULT 0;
create table if not exists `wireKey` (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      wireId INTEGER,
                                      key varchar,
                                      created_time varchar DEFAULT (datetime('now', 'localtime')),
                                      updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `wirelist` (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         script varchar,
                                         content varchar,
                                         result varchar,
                                         created_time varchar DEFAULT (datetime('now', 'localtime')),
                                         updated_time varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `jdck` (
                                      `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                                      `ck` varchar,
                                      `pt_pin` varchar,
                                      `remark` varchar,
                                      `status` INTEGER DEFAULT '0',
                                      `level` INTEGER DEFAULT '2',
                                      `jd` INTEGER DEFAULT '0',
                                      `ql_remark` varchar,
                                      `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                                      `expiry_time` varchar,
                                      `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
alter TABLE  `jdck` add `ql_remark` varchar;
create table if not exists `jdFruit` (
                           `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                           `help_code` varchar,
                           `ckid` INTEGER UNIQUE,
                           `is_fruit_hei` INTEGER DEFAULT '0',
                           `help_status` INTEGER DEFAULT '0',
                           `to_help_status` INTEGER DEFAULT '1',
                           `help_lottery_status` INTEGER DEFAULT '0',
                           `to_help_lottery_status` INTEGER DEFAULT '1',
                           `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                           `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `jdjd` (
                                      `date` varchar PRIMARY KEY,
                                      `jdCount` INTEGER DEFAULT '0',
                                      `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                                      `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `jdPet` (
                         `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                         `help_code` varchar,
                         `ckid` INTEGER UNIQUE,
                         `is_pet_hei` INTEGER DEFAULT '0',
                         `help_status` INTEGER DEFAULT '0',
                         `to_help_status` INTEGER DEFAULT '1',
                         `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                         `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `jdPlant UNIQUE` (
                           `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                           `help_code` varchar,
                           `ckid` INTEGER,
                           `is_Plant_hei` INTEGER DEFAULT '0',
                           `help_status` INTEGER DEFAULT '0',
                           `to_help_status` INTEGER DEFAULT '1',
                           `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                           `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `jdZqdyj` (
                           `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                           `help_code` varchar,
                           `ckid` INTEGER UNIQUE,
                           `is_hei` INTEGER DEFAULT '0',
                           `help_status` INTEGER DEFAULT '0',
                           `to_help_status` INTEGER DEFAULT '1',
                           `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                           `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `ylgy` (
                                         `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                                         `uid` varchar,
                                         `token` INTEGER,
                                         `times` INTEGER DEFAULT '100000',
                                         `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                                         `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `wxbs` (
                                      `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                                      `username` varchar,
                                      `password` varchar,
                                      `minstep` INTEGER DEFAULT 30000,
                                      `maxstep` INTEGER DEFAULT 60000,
                                      `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                                      `expiry_time` varchar,
                                      `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `command` (
                                      `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                                      `command` varchar UNIQUE,
                                      `function` varchar,
                                      `reply` varchar,
                                      `is_built_in` INTEGER DEFAULT 0,
                                      `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                                      `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
create table if not exists `forward` (
                                         `id` INTEGER PRIMARY KEY AUTOINCREMENT,
                                         `from` varchar ,
                                         `fromname` varchar ,
                                         `fromtype` INTEGER,
                                         `to` varchar,
                                         `toname` varchar ,
                                         `totype` INTEGER,
                                         `created_time` varchar DEFAULT (datetime('now', 'localtime')),
                                         `updated_time` varchar DEFAULT (datetime('now', 'localtime'))
);
