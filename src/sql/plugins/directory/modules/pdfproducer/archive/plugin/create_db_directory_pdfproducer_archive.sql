DROP TABLE IF EXISTS directory_zip_basket;
DROP TABLE IF EXISTS directory_zip_basket_action;

/*==============================================================*/
/* New zip basket table to export large zip files				*/
/*==============================================================*/
CREATE TABLE directory_zip_basket (
	id_zip_basket INT DEFAULT 0 NOT NULL,
	name varchar(100) NOT NULL,
	url varchar(255) DEFAULT NULL,
	zip_state varchar(100) NOT NULL,
	id_user INT NOT NULL,
	id_directory INT NOT NULL,
	id_record INT DEFAULT NULL ,
	archive_item_key INT DEFAULT NULL ,
	date_creation timestamp,
	PRIMARY KEY (id_zip_basket)
);

/*==============================================================*/
/*Table structure for table directory_zip_basket_action			*/
/*==============================================================*/
CREATE TABLE directory_zip_basket_action (
  id_action INT DEFAULT 0 NOT NULL,
  name_key VARCHAR(100) DEFAULT NULL ,
  description_key VARCHAR(100) DEFAULT NULL  ,
  action_url VARCHAR(255) DEFAULT NULL ,
  icon_url VARCHAR(255) DEFAULT NULL,
  action_permission VARCHAR(255) DEFAULT NULL,
  directory_state SMALLINT DEFAULT 0,
  PRIMARY KEY (id_action)
);