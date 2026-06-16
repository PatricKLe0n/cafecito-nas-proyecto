-- Contraseñas demo: admin123 y user123 (BCrypt fuerza 10, verificadas)
INSERT INTO usuario (username, password, rol) VALUES
 ('admin', '$2b$10$NUuSY.FC87W/vthhfVnPRe1oFU9meLixGpcnoKbEF8iZsLui/xs9W', 'ADMIN'),
 ('user',  '$2b$10$9z83hMZxztYsr28OTGJ9/OOfdocbjo1mZ2XG8Jg7wL7aFFJd3nL5u', 'USER');

INSERT INTO finca (pais, region, nombre, productor, altitud_msnm, variedad, proceso) VALUES
 ('Colombia', 'Huila',  'Finca El Mirador',  'Ana Gómez',   1750, 'Caturra', 'LAVADO'),
 ('Etiopía',  'Yirgacheffe', 'Konga Coop',  'Konga',       1950, 'Heirloom', 'NATURAL'),
 ('Perú',     'Cajamarca', 'Finca La Palma', 'José Ruiz',   1600, 'Bourbon', 'HONEY');

INSERT INTO lote_cafe_verde (codigo, finca_id, peso_kg, humedad_porcentaje, puntaje_sca, fecha_recepcion, estado) VALUES
 ('LV-2026-001', 1, 60.00, 10.5, 86.50, '2026-02-10', 'DISPONIBLE'),
 ('LV-2026-002', 2, 50.00, 11.0, 88.00, '2026-02-15', 'DISPONIBLE'),
 ('LV-2026-003', 3, 45.00, 10.0, 85.00, '2026-03-01', 'DISPONIBLE');

INSERT INTO lote_tostado (codigo, lote_verde_id, perfil_tueste, peso_entrada_kg, peso_salida_kg, merma_porcentaje, fecha_tueste, estado) VALUES
 ('LT-2026-001', 1, 'MEDIUM', 10.00, 8.50, 15.00, '2026-03-05 09:30:00', 'REGISTRADO');
-- Ajuste de stock del lote verde 1 por el tostado anterior:
UPDATE lote_cafe_verde SET peso_kg = peso_kg - 10.00 WHERE id = 1;
