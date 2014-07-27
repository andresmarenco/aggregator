--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: ir_vertical; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical (
    id character varying(20) NOT NULL,
    name character varying(100),
    vertical_category_id character varying(20) NOT NULL
);


ALTER TABLE public.ir_vertical OWNER TO aggregator;

--
-- Name: ir_vertical_category; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical_category (
    id character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.ir_vertical_category OWNER TO aggregator;

--
-- Name: ir_vertical_category_fedweb; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical_category_fedweb (
    vertical_category_id character varying(20) NOT NULL,
    fedweb_code character varying(20) NOT NULL
);


ALTER TABLE public.ir_vertical_category_fedweb OWNER TO aggregator;

--
-- Name: ir_vertical_fedweb; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical_fedweb (
    vertical_id character varying(20) NOT NULL,
    fedweb_code character varying(20) NOT NULL
);


ALTER TABLE public.ir_vertical_fedweb OWNER TO aggregator;

--
-- Data for Name: ir_vertical; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical (id, name, vertical_category_id) FROM stdin;
test	Test Vertical	general
arxiv	arXiv.org	academic
ccsb	CCSB	academic
\.


--
-- Data for Name: ir_vertical_category; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical_category (id, name, description) FROM stdin;
general	General	Standard web pages
video	Video	Online videos
academic	Academic	Research technical report
photo	Photo/Pictures	Online pictures
jobs	Jobs	Job posts
encyclopedia	Encyclopedia	Encyclopedic entries
travel	Travel	Travel pages
shopping	Shopping	Product shopping page
tech	Tech	Technology pages
health	Health	Health related pages
kids	Kids	Cartoon pages
recipes	Recipes	Recipes page
news	News	News articles
social	Social	Social network pages
books	Books	Book review pages
sports	Sports	Sports pages
blogs	Blogs	Blog articles
games	Games	Electronic game pages
jokes	Jokes	Joke threads
entertainment	Entertainment	Entertainment pages
audio	Audio	Online audios
questions	Q&A	Answers to questions
software	Software	Software downloading pages
local	Local	Local information pages
\.


--
-- Data for Name: ir_vertical_category_fedweb; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical_category_fedweb (vertical_category_id, fedweb_code) FROM stdin;
general	FW14-v001
video	FW14-v002
jobs	FW14-v003
academic	FW14-v004
photo	FW14-v005
encyclopedia	FW14-v006
travel	FW14-v007
shopping	FW14-v008
tech	FW14-v009
health	FW14-v010
kids	FW14-v011
recipes	FW14-v012
news	FW14-v013
social	FW14-v014
books	FW14-v015
sports	FW14-v016
games	FW14-v017
blogs	FW14-v018
jokes	FW14-v019
entertainment	FW14-v020
questions	FW14-v021
audio	FW14-v022
software	FW14-v023
local	FW14-v024
\.


--
-- Data for Name: ir_vertical_fedweb; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical_fedweb (vertical_id, fedweb_code) FROM stdin;
arxiv	FW14-e001
ccsb	FW14-e002
\.


--
-- Name: ir_vertical_category_fedweb_fedweb_code_uk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_category_fedweb
    ADD CONSTRAINT ir_vertical_category_fedweb_fedweb_code_uk UNIQUE (fedweb_code);


--
-- Name: ir_vertical_category_fedweb_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_category_fedweb
    ADD CONSTRAINT ir_vertical_category_fedweb_pk PRIMARY KEY (vertical_category_id, fedweb_code);


--
-- Name: ir_vertical_category_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_category
    ADD CONSTRAINT ir_vertical_category_pk PRIMARY KEY (id);


--
-- Name: ir_vertical_fedweb_fedweb_code_uk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_fedweb
    ADD CONSTRAINT ir_vertical_fedweb_fedweb_code_uk UNIQUE (fedweb_code);


--
-- Name: ir_vertical_fedweb_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_fedweb
    ADD CONSTRAINT ir_vertical_fedweb_pk PRIMARY KEY (vertical_id, fedweb_code);


--
-- Name: ir_vertical_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical
    ADD CONSTRAINT ir_vertical_pk PRIMARY KEY (id);


--
-- Name: ir_vertical_category_fedweb_vertical_category_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical_category_fedweb
    ADD CONSTRAINT ir_vertical_category_fedweb_vertical_category_id_fk FOREIGN KEY (vertical_category_id) REFERENCES ir_vertical_category(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_vertical_fedweb_vertical_id; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical_fedweb
    ADD CONSTRAINT ir_vertical_fedweb_vertical_id FOREIGN KEY (vertical_id) REFERENCES ir_vertical(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_vertical_vertical_category_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical
    ADD CONSTRAINT ir_vertical_vertical_category_id_fk FOREIGN KEY (vertical_category_id) REFERENCES ir_vertical_category(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

