CREATE TABLE public.discount_card (
	id int8 NOT NULL,
	customer_name varchar(255) NOT NULL,
	discount_percentage numeric NOT NULL,
	CONSTRAINT discount_card_pk PRIMARY KEY (id)
);