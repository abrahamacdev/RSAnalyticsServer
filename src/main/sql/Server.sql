DROP DATABASE IF EXISTS RSAnalytics;

CREATE DATABASE IF NOT EXISTS RSAnalytics;

CREATE TABLE IF NOT EXISTS usuario(
	id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(30) NOT NULL,
    primer_apellido VARCHAR(60) NOT NULL,
    segundo_apellido VARCHAR(60) NOT NULL,
    telefono CHAR(9),
    correo VARCHAR(120) NOT NULL,
    contrasenia BLOB NOT NULL,
    salt BLOB NOT NULL,
    id_rol INT NOT NULL,
    CONSTRAINT us_correo_uk UNIQUE (correo)
);

CREATE TABLE IF NOT EXISTS rol(
	id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(30),
    CONSTRAINT rol_nom_uk UNIQUE (nombre)
);

ALTER TABLE usuario ADD CONSTRAINT us_rol_fk FOREIGN KEY (id_rol) REFERENCES rol(id);

INSERT INTO rol (nombre) VALUES ('Administrador'), ('Usuario Normal');