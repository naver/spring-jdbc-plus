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
