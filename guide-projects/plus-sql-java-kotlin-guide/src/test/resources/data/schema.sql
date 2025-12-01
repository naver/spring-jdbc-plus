CREATE TABLE IF NOT EXISTS n_order (
  id INT AUTO_INCREMENT,
  price INT,
  status VARCHAR(20),
  purchaser_no VARCHAR(36),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_board (
  id INT AUTO_INCREMENT,
  name VARCHAR(255),
  board_memo VARCHAR(255),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_label (
  id INT AUTO_INCREMENT,
  name VARCHAR(255),
  board_id INT,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_post (
  id INT AUTO_INCREMENT,
  post_no INT,
  title VARCHAR(255),
  content VARCHAR(255),
  board_id INT,
  board_index INT,
  memo VARCHAR(255),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_tag (
  id INT AUTO_INCREMENT,
  content VARCHAR(255),
  post_id INT,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_comment (
  id INT AUTO_INCREMENT,
  content VARCHAR(255),
  post_id INT,
  post_index INT,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_audit (
  id INT AUTO_INCREMENT,
  name VARCHAR(255),
  board_id INT,
  post_id INT,
  comment_id INT,
  memo VARCHAR(255),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_audit_secret (
  id INT AUTO_INCREMENT,
  secret VARCHAR(255),
  audit_id INT,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_config (
  id INT AUTO_INCREMENT,
  config_key VARCHAR(255),
  config_value VARCHAR(255),
  board_id INT,
  post_id INT,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_pay (
  id INT AUTO_INCREMENT,
  amount INT,
  order_id INT,
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS n_pay_adms (
  id INT AUTO_INCREMENT,
  amount INT,
  pay_id INT,
  pay_method_type VARCHAR(20),
  PRIMARY KEY(id));

CREATE TABLE IF NOT EXISTS cupn_pubt (
  pubt_nm VARCHAR(255),
  ver BIGINT,
  discount_amt DECIMAL(20, 2),
  PRIMARY KEY(pubt_nm, ver));

CREATE TABLE IF NOT EXISTS cupn (
  cupn_no VARCHAR(255),
  discount_amt DECIMAL(20, 2),
  pubt_nm VARCHAR(255),
  ver BIGINT,
  PRIMARY KEY(cupn_no));
