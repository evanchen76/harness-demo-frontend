#!/bin/bash
# 只在 volume 為空時執行一次：建庫與帳號。表結構不放這裡（見 db/migration）。
set -euo pipefail

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE shop      CHARACTER SET utf8mb4;

-- 只給 ./dev.sh 套用 db/migration/*.sql 用：可改結構
CREATE USER 'migrator'@'%' IDENTIFIED BY '${MIGRATOR_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON shop.*      TO 'migrator'@'%';

-- 服務、測試與 AI 查資料共用：只能讀寫資料，不能改結構
CREATE USER 'app_user'@'%' IDENTIFIED BY '${APP_DB_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE ON shop.*      TO 'app_user'@'%';
SQL
