-- V1：表結構（商品管理）。已套用過的 migration 不能修改，要改結構請新增下一版。


CREATE TABLE category (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL COMMENT '分類名稱',
  enabled     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否啟用 0:停用 1:啟用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) COMMENT='商品分類';

CREATE TABLE product (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  sku            VARCHAR(64)   NOT NULL COMMENT '商品編號',
  name           VARCHAR(200)  NULL     COMMENT '商品名稱',
  category_id    BIGINT        NOT NULL COMMENT '分類',
  price          DECIMAL(10,2) NOT NULL COMMENT '售價',
  cost_price     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '成本價',
  stock          INT           NOT NULL DEFAULT 0 COMMENT '庫存',
  main_image_url VARCHAR(500)  NULL     COMMENT '主圖網址',
  description    TEXT          NULL     COMMENT '商品描述',
  status         VARCHAR(16)   NOT NULL DEFAULT 'DRAFT' COMMENT '狀態 DRAFT / ON_SHELF / OFF_SHELF / SOLD_OUT',
  on_shelf_time  DATETIME      NULL     COMMENT '最近一次上架時間',
  create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sku (sku)
) COMMENT='商品';

CREATE TABLE product_stock_log (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  product_id    BIGINT       NOT NULL COMMENT '商品',
  quantity      INT          NOT NULL COMMENT '調整量，正數加、負數扣',
  stock_before  INT          NOT NULL COMMENT '調整前庫存',
  stock_after   INT          NOT NULL COMMENT '調整後庫存',
  status_before VARCHAR(16)  NOT NULL COMMENT '調整前狀態',
  status_after  VARCHAR(16)  NOT NULL COMMENT '調整後狀態',
  reason        VARCHAR(100) NOT NULL COMMENT '調整原因',
  operator      VARCHAR(64)  NOT NULL COMMENT '操作人（X-Operator）',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_product_id (product_id)
) COMMENT='商品庫存調整紀錄（只記成功的調整）';
