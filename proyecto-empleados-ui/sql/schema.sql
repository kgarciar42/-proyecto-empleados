CREATE DATABASE IF NOT EXISTS gestion_empleados
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE gestion_empleados;

DROP TABLE IF EXISTS empleados;

CREATE TABLE empleados (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    departamento        VARCHAR(50)   NOT NULL,
    salario             DECIMAL(10,2) NOT NULL,
    fecha_contratacion  DATE          NOT NULL,
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    anios_experiencia   INT           NOT NULL DEFAULT 0,

    CONSTRAINT chk_salario_positivo   CHECK (salario > 0),
    CONSTRAINT chk_experiencia_rango  CHECK (anios_experiencia BETWEEN 0 AND 50)
);

INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, activo, anios_experiencia) VALUES
('Ana Lucía Pérez',      'Sistemas',     8500.00, '2024-03-15', TRUE,  5),
('Carlos Roberto Mux',   'Ventas',       6200.00, '2024-05-02', TRUE,  3),
('Diana Sofía Cabrera',  'Contabilidad', 7100.00, '2023-11-10', FALSE, 10);