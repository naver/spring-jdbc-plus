CREATE TABLE IF NOT EXISTS n_order (
  id INT AUTO_INCREMENT,
  status VARCHAR(20),
  purchaser_id VARCHAR(36),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_order_discount (
  id INT AUTO_INCREMENT,
  order_id INT,
  origin_price DOUBLE,
  discount_price DOUBLE,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_order_item (
  id INT AUTO_INCREMENT,
  order_id INT,
  product_no VARCHAR(36),
  name VARCHAR(200),
  quantity BIGINT,
  price DOUBLE,
  seller_id VARCHAR(36),
  idx INTEGER,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_shipping (
  id VARCHAR(36),
  order_id INT,
  receiver_address VARCHAR(255),
  memo VARCHAR(255),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS boolean_state_articles (
    id BIGINT AUTO_INCREMENT,
    writer_id VARCHAR(20),
    contents TEXT,
    created_at TIMESTAMP,
    last_modified_at TIMESTAMP,
    visible TINYINT,
    PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS enum_state_articles (
    id BIGINT AUTO_INCREMENT,
    writer_id VARCHAR(20),
    contents TEXT,
    created_at TIMESTAMP,
    last_modified_at TIMESTAMP,
    version INT,
    article_state VARCHAR(2),
    PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS soft_delete_products (
    id BIGINT AUTO_INCREMENT,
    product_name VARCHAR(20),
    created_at TIMESTAMP,
    last_modified_at TIMESTAMP,
    visible TINYINT,
    PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS soft_delete_reviews (
    id BIGINT AUTO_INCREMENT,
    product_id BIGINT,
    contents TEXT,
    created_at TIMESTAMP,
    last_modified_at TIMESTAMP,
    visible TINYINT,
    PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS plain_products (
    id BIGINT AUTO_INCREMENT,
    product_name VARCHAR(20),
    created_at TIMESTAMP,
    last_modified_at TIMESTAMP,
    PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS plain_reviews (
    id BIGINT AUTO_INCREMENT,
    product_id BIGINT,
    contents TEXT,
    created_at TIMESTAMP,
    last_modified_at TIMESTAMP,
    visible TINYINT,
    PRIMARY KEY(id));
