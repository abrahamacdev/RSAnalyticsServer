DROP DATABASE IF EXISTS RSAnalytics;

CREATE DATABASE IF NOT EXISTS RSAnalytics;

USE RSAnalytics;

CREATE TABLE IF NOT EXISTS usuario(
	id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(30) NOT NULL,
    primer_apellido VARCHAR(60) NOT NULL,
    segundo_apellido VARCHAR(60) NOT NULL,
    telefono CHAR(9),
    correo VARCHAR(120) NOT NULL,
    contrasenia BLOB NOT NULL,
    salt BLOB NOT NULL,
    rol_id INT NOT NULL,
    genero ENUM('H','M'),
    CONSTRAINT us_correo_uk UNIQUE (correo)
);

CREATE TABLE IF NOT EXISTS rol(
	id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(30),
    CONSTRAINT rol_nom_uk UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS tokenAcceso(
    id INT PRIMARY KEY AUTO_INCREMENT,
    publico_id CHAR(36),
    usuario_id INT,
    CONSTRAINT tkAc_idPub_uk UNIQUE tokenAcceso(publico_id)
);

CREATE TABLE IF NOT EXISTS grupo(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255),
    responsable_id INT,
    CONSTRAINT grup_nom_uk UNIQUE grupo(nombre)
);

CREATE TABLE IF NOT EXISTS usuario_grupo(
    usuario_id INT,
    grupo_id INT,
    fecha_ingreso DATE DEFAULT NOW(),
    CONSTRAINT usGrup_idUsIdGrup_pk PRIMARY KEY (usuario_id, grupo_id)
);

CREATE TABLE IF NOT EXISTS notificacion(
    id INT PRIMARY KEY AUTO_INCREMENT,
    mensaje VARCHAR(255),
    fecha_envio DATE NOT NULL DEFAULT NOW(),
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    emisor_id INT NOT NULL,
    receptor_id INT NOT NULL,
    accion_id INT
);

CREATE TABLE IF NOT EXISTS accion (
    id INT PRIMARY KEY AUTO_INCREMENT,
    completada BOOLEAN NOT NULL DEFAULT FALSE,
    grupo_id INT,
    tipo_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    CONSTRAINT tip_nom_uk UNIQUE tipo(nombre)
);

CREATE TABLE IF NOT EXISTS procedencia (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    url VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS anuncio (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fecha_obtencion DATE NOT NULL DEFAULT NOW(),
    municipio_id INT NOT NULL,
    procedencia_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS claveAtributoAnuncio (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE,
    CONSTRAINT claAtAn_nom_uk UNIQUE KEY claveAtributoAnuncio(nombre)
);

CREATE TABLE IF NOT EXISTS atributoAnuncio (
    claveAtributoAnuncio_id INT,
    anuncio_id INT,
    valor_numerico DECIMAL(24,8),
    valor_cadena VARCHAR(255),
    CONSTRAINT atriAnun_ids_pk PRIMARY KEY (claveAtributoAnuncio_id, anuncio_id)
);

CREATE TABLE IF NOT EXISTS municipio (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    codigo_postal CHAR(5) NOT NULL,
    provincia_id INT NOT NULL,
    CONSTRAINT mun_nomCp_uk UNIQUE KEY municipio(nombre,codigo_postal)
);

CREATE TABLE IF NOT EXISTS provincia (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    CONSTRAINT pro_nom_uk UNIQUE KEY provincia(nombre)
);

CREATE TABLE IF NOT EXISTS inmueble (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipoInmueble_id INT NOT NULL,
    municipio_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS tipoContrato (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    CONSTRAINT tipCon_nom_uk UNIQUE KEY tipoContrato(nombre)
);

CREATE TABLE IF NOT EXISTS tipoInmueble (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    CONSTRAINT tipInm_nom_uk UNIQUE KEY tipoInmueble(nombre)
);

CREATE TABLE IF NOT EXISTS claveAtributoInmueble (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE,
    CONSTRAINT claAtIn_nom_uk UNIQUE KEY claveAtributoInmueble(nombre)
);

CREATE TABLE IF NOT EXISTS tipoInmueble_claveAtributoInmueble (
    tipoInmueble_id INT,
    claveAtributoInmueble_id INT,
    CONSTRAINT tipoIn_clavAtrIn_ids_pk PRIMARY KEY (claveAtributoInmueble_id, tipoInmueble_id)
);

CREATE TABLE IF NOT EXISTS atributoInmueble (
    claveAtributoInmueble_id INT,
    inmueble_id INT,
    valor_numerico DECIMAL(24,8),
    valor_cadena VARCHAR(255),
    CONSTRAINT atriIn_ids_pk PRIMARY KEY (claveAtributoInmueble_id, inmueble_id)
);

CREATE TABLE IF NOT EXISTS anuncio_tipoContrato_inmueble (
    inmueble_id INT,
    anuncio_id INT,
    tipoContrato_id INT,
    CONSTRAINT an_tipCon_in_ids_pk PRIMARY KEY (inmueble_id, anuncio_id, tipoContrato_id)
);

# Usuario
ALTER TABLE usuario ADD CONSTRAINT us_rol_fk FOREIGN KEY (rol_id) REFERENCES rol(id);

# TokenAcceso
ALTER TABLE tokenAcceso ADD CONSTRAINT tkAc_idUs_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;

# Grupo
ALTER TABLE grupo ADD CONSTRAINT grup_idResp_fk FOREIGN KEY (responsable_id) REFERENCES usuario(id) ON DELETE CASCADE;

# Usuario_Grupo
ALTER TABLE usuario_grupo ADD CONSTRAINT usGrup_idUs_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;
ALTER TABLE usuario_grupo ADD CONSTRAINT usGrup_idGrup_fk FOREIGN KEY (grupo_id) REFERENCES grupo(id) ON DELETE CASCADE;

# Notificacion
ALTER TABLE notificacion ADD CONSTRAINT not_emId_fk FOREIGN KEY (emisor_id) REFERENCES usuario(id) ON DELETE CASCADE;
ALTER TABLE notificacion ADD CONSTRAINT not_recId_fk FOREIGN KEY (receptor_id) REFERENCES usuario(id) ON DELETE CASCADE;
ALTER TABLE notificacion ADD CONSTRAINT not_accId_fk FOREIGN KEY (accion_id) REFERENCES accion(id) ON DELETE CASCADE;

# Accion
ALTER TABLE accion ADD CONSTRAINT acc_gruId_fk FOREIGN KEY (grupo_id) REFERENCES grupo(id) ON DELETE CASCADE;
ALTER TABLE accion ADD CONSTRAINT acc_tipId_fk FOREIGN KEY (tipo_id) REFERENCES tipo(id) ON DELETE CASCADE;

# Anuncio
ALTER TABLE anuncio ADD CONSTRAINT an_procId_fk FOREIGN KEY (procedencia_id) REFERENCES procedencia(id) ON UPDATE CASCADE;
ALTER TABLE anuncio ADD CONSTRAINT an_munId_fk FOREIGN KEY (municipio_id) REFERENCES municipio(id) ON UPDATE CASCADE ON DELETE CASCADE;

# AtributoAnuncio
ALTER TABLE atributoAnuncio ADD CONSTRAINT atriAnun_clavAtrAnId_fk FOREIGN KEY (claveAtributoAnuncio_id) REFERENCES claveAtributoAnuncio(id);
ALTER TABLE atributoAnuncio ADD CONSTRAINT atriAnun_anId_fk FOREIGN KEY (anuncio_id) REFERENCES anuncio(id);

# Municipio
ALTER TABLE municipio ADD CONSTRAINT mun_proId_fk FOREIGN KEY (provincia_id) REFERENCES provincia(id);

# Anuncio_TipoContrato_Inmueble
ALTER TABLE anuncio_tipoContrato_inmueble ADD CONSTRAINT an_tipCon_in_inId_fk FOREIGN KEY (inmueble_id) REFERENCES inmueble(id);
ALTER TABLE anuncio_tipoContrato_inmueble ADD CONSTRAINT an_tipCon_in_anId_fk FOREIGN KEY (anuncio_id) REFERENCES anuncio(id);
ALTER TABLE anuncio_tipoContrato_inmueble ADD CONSTRAINT an_tipCon_in_tipConId_fk FOREIGN KEY (tipoContrato_id) REFERENCES tipoContrato(id);

# Inmueble
ALTER TABLE inmueble ADD CONSTRAINT in_tipInId_fk FOREIGN KEY (tipoInmueble_id) REFERENCES tipoInmueble(id);
ALTER TABLE inmueble ADD CONSTRAINT in_munId_fk FOREIGN KEY (municipio_id) REFERENCES municipio(id);

# AtributoInmueble
ALTER TABLE atributoInmueble ADD CONSTRAINT atriIn_clavAtrInId_fk FOREIGN KEY (claveAtributoInmueble_id) REFERENCES claveAtributoInmueble(id);
ALTER TABLE atributoInmueble ADD CONSTRAINT atriIn_inId_fk FOREIGN KEY (inmueble_id) REFERENCES inmueble(id);

# TipoInmueble_ClaveAtributoInmueble
ALTER TABLE tipoInmueble_claveAtributoInmueble ADD CONSTRAINT tipIn_clavAtrIn_clavAtrInId_fk FOREIGN KEY (claveAtributoInmueble_id) REFERENCES claveAtributoInmueble(id);
ALTER TABLE tipoInmueble_claveAtributoInmueble ADD CONSTRAINT tipIn_clavAtrIn_tipInId_fk FOREIGN KEY (tipoInmueble_id) REFERENCES tipoInmueble(id);


# Datos iniciales
INSERT INTO rol (nombre) VALUES ('Administrador'), ('Usuario Normal');
INSERT INTO tipo (nombre) VALUES ('Invitacion a grupo');
INSERT INTO procedencia (nombre,url) VALUES ('Fotocasa','www.fotocasa.es');

INSERT INTO claveAtributoAnuncio(nombre) VALUES
    ('Aire acondicionado'),('Armarios'),('Calefacción'),('Cocina Equipada'),('Jardín'),('Terraza'),('Trastero'),
    ('Z. Comunitaria'),('Alarma'),('Domótica'),('Patio'),('Energía Solar'),('Piscina'),('Videoportero'),('Suite'),
    ('Zona Deportiva'),('Zona Infantil'),('Puerta Blindada'),('Electrodomésticos'),('Horno'),('Lavadora'),
    ('Nevera'),('Serv. portería'),('TV'),('Balcón'),('Lavadero'),('Internet'),('Bodega'),('Planta'),('Escalera'),
    ('Edificio'),('Numero'),('Ascensor'), ('Orientacion'), ('Id Orientacion');
INSERT INTO claveAtributoAnuncio(nombre, es_principal) VALUES
    ('Numero Habitaciones', TRUE),('Banos', TRUE),('Consumo', TRUE),('Emisiones', TRUE),
    ('Numero Imagenes', TRUE),('Precio', TRUE),('Longitud', TRUE),('Latitud', TRUE),
    ('Antiguedad', TRUE),('Tipo Anunciante', TRUE),('M2', TRUE),('Tipo Inmueble', TRUE),('Tipo Contrato', TRUE),
    ('Id Anuncio', TRUE),('Id Anunciante', TRUE),('Nombre Anunciante', TRUE),('Numero de Contacto', TRUE),
    ('Url Anunciante', TRUE), ('Fecha Publicacion', TRUE), ('Referencia Anunciante', TRUE), ('Id Tipo Inmueble', TRUE),
    ('Id Subtipo Inmueble', TRUE);

INSERT INTO tipoInmueble(id,nombre) VALUES (1,'Vivienda');

INSERT INTO claveAtributoInmueble (id, nombre) VALUES
    (1,'Aire acondicionado'),(2,'Armarios'),(3,'Calefacción'),(4,'Cocina Equipada'),(5,'Jardín'),(6,'Terraza'),
    (7,'Trastero'),(8,'Z. Comunitaria'),(9,'Alarma'),(10,'Domótica'),(11,'Patio'),(12,'Energía Solar'),(13,'Piscina'),
    (14,'Videoportero'),(15,'Suite'),(16,'Zona Deportiva'),(17,'Zona Infantil'),(18,'Puerta Blindada'),(19,'Electrodomésticos'),
    (20,'Horno'),(21,'Lavadora'),(22,'Nevera'),(23,'Serv. portería'),(24,'TV'),(25,'Balcón'),(26,'Lavadero'),
    (27,'Internet'),(28,'Bodega'),(29,'Planta'),(30,'Escalera'),(31,'Edificio'),(32,'Numero'),(33,'Ascensor'),
    (34, 'Orientacion'), (35, 'Id Orientacion');
INSERT INTO claveAtributoInmueble(id, nombre, es_principal) VALUES
    (50,'Numero Habitaciones', TRUE),(51,'Banos', TRUE),(52,'Consumo', TRUE),(53,'Emisiones', TRUE),
    (54,'Numero Imagenes', TRUE),(55,'Precio', TRUE),(56,'Longitud', TRUE),(57,'Latitud', TRUE), (58,'Antiguedad', TRUE),
    (59,'Tipo Anunciante', TRUE),(60,'M2', TRUE),(61,'Id Anuncio', TRUE),(62,'Id Anunciante', TRUE),
    (63,'Nombre Anunciante', TRUE),(64,'Numero de Contacto', TRUE),(65,'Url Anunciante', TRUE),(66,'Fecha Publicacion', TRUE),
    (67,'Referencia Anunciante', TRUE),(68,'Id Tipo Inmueble', TRUE),(69,'Id Subtipo Inmueble', TRUE);

# Ligamos los atributos propios de una vivienda + atributos obligatorios de todos los inmuebles con el tipo de inmueble 'Vivienda'
INSERT INTO tipoInmueble_claveAtributoInmueble VALUES
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),
    (1,18),(1,19),(1,20),(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),(1,31),(1,32),(1,33),
    (1,34),(1,35),(1,50),(1,51),(1,52),(1,53),(1,54),(1,55),(1,56),(1,57),(1,58),(1,59),(1,60),(1,61),(1,62),(1,63),
    (1,64),(1,65),(1,66),(1,67),(1,68),(1,69);
