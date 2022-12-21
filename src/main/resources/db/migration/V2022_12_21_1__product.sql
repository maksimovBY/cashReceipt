CREATE TABLE public.product (
	id int8 NOT NULL,
	description varchar(255) NOT NULL,
	price numeric NOT NULL,
	promotional bool NOT NULL DEFAULT false,
	CONSTRAINT product_pk PRIMARY KEY (id)
);