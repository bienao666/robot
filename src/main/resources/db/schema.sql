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