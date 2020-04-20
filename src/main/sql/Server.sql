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

# Usuario
ALTER TABLE usuario ADD CONSTRAINT us_rol_fk FOREIGN KEY (rol_id) REFERENCES rol(id);

# TokenAcceso
ALTER TABLE tokenAcceso ADD CONSTRAINT tkAc_idUs_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;

# Grupo
ALTER TABLE grupo ADD CONSTRAINT grup_idResp_fk FOREIGN KEY (responsable_id) REFERENCES usuario(id) ON DELETE CASCADE;

# Usuario_Grupo
ALTER TABLE usuario_grupo ADD CONSTRAINT usGrup_idUs_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;
ALTER TABLE usuario_grupo ADD CONSTRAINT usGrup_idGrup_fk FOREIGN KEY (grupo_id) REFERENCES grupo(id) ON DELETE CASCADE;


INSERT INTO rol (nombre) VALUES ('Administrador'), ('Usuario Normal');