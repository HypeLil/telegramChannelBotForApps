CREATE TABLE channels(
    id VARCHAR(32) PRIMARY KEY ,
    url VARCHAR(50)
);
CREATE TABLE messages(
    id VARCHAR(32) PRIMARY KEY ,
    chat_id VARCHAR(32)
);
CREATE TABLE users(
    id VARCHAR(32) PRIMARY KEY ,
    position VARCHAR(32)
);