create database if NOT EXISTS paypal;
use paypal;


create table users(
	id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY,first_name VARCHAR(30) NOT NULL,last_name VARCHAR(30) NOT NULL,balance DOUBLE DEFAULT 0);