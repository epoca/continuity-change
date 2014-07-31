CREATE table IF NOT EXISTS tag_table (
	id INT NOT NULL IDENTITY,
	entity_id varchar(255),
	product_id varchar(255),
	attribute varchar(255),
	time INT
);

CREATE index tag_index ON tag_table (entity_id, time, attribute);