-- Datos demo adicionales para poblar la cadena de trazabilidad.
-- Las claves foráneas se resuelven por clave natural (nombre de finca / código de lote)
-- para no depender de los IDs autogenerados.

-- ── Fincas de origen ──────────────────────────────────────────────────────────
INSERT INTO finca (pais, region, nombre, productor, altitud_msnm, variedad, proceso) VALUES
 ('Guatemala', 'Acatenango',  'Finca La Soledad',    'Carlos Méndez',     1500, 'Bourbon',     'LAVADO'),
 ('Kenia',     'Nyeri',       'Gichathaini AB',      'Coop. Gichathaini', 1800, 'SL28',        'LAVADO'),
 ('Brasil',    'Sul de Minas','Fazenda Recanto',     'Joana Pereira',     1200, 'Mundo Novo',  'NATURAL'),
 ('Costa Rica','Tarrazú',     'Finca El Cedral',     'Luis Vargas',       1700, 'Catuaí',      'HONEY'),
 ('Honduras',  'Marcala',     'Finca Las Nubes',     'María López',       1450, 'Lempira',     'LAVADO'),
 ('Panamá',    'Boquete',     'Hacienda Esmeralda',  'Familia Peterson',  1650, 'Geisha',      'NATURAL');

-- ── Lotes de café verde ───────────────────────────────────────────────────────
-- peso_kg = stock restante (ya descontado por los tostados de este lote, salvo anulados)
INSERT INTO lote_cafe_verde (codigo, finca_id, peso_kg, humedad_porcentaje, puntaje_sca, fecha_recepcion, estado) VALUES
 ('LV-2026-004', (SELECT id FROM finca WHERE nombre='Finca La Soledad'),   55.00, 10.8, 85.50, '2026-03-10', 'DISPONIBLE'),
 ('LV-2026-005', (SELECT id FROM finca WHERE nombre='Gichathaini AB'),      40.00, 10.2, 89.00, '2026-03-12', 'DISPONIBLE'),
 ('LV-2026-006', (SELECT id FROM finca WHERE nombre='Fazenda Recanto'),      0.00, 11.5, 83.00, '2026-02-20', 'AGOTADO'),
 ('LV-2026-007', (SELECT id FROM finca WHERE nombre='Finca El Cedral'),     25.00, 10.5, 87.50, '2026-03-15', 'DISPONIBLE'),
 ('LV-2026-008', (SELECT id FROM finca WHERE nombre='Finca Las Nubes'),     48.00, 10.9, 84.00, '2026-03-18', 'DISPONIBLE'),
 ('LV-2026-009', (SELECT id FROM finca WHERE nombre='Hacienda Esmeralda'),  12.00, 10.0, 92.00, '2026-03-20', 'DISPONIBLE'),
 ('LV-2026-010', (SELECT id FROM finca WHERE nombre='Finca El Mirador'),    30.00, 10.6, 86.00, '2026-03-22', 'DISPONIBLE'),
 ('LV-2026-011', (SELECT id FROM finca WHERE nombre='Konga Coop'),           0.00, 11.0, 88.50, '2026-02-25', 'AGOTADO'),
 ('LV-2026-012', (SELECT id FROM finca WHERE nombre='Finca La Palma'),      38.00, 10.3, 85.00, '2026-03-25', 'DISPONIBLE');

-- ── Lotes tostados ────────────────────────────────────────────────────────────
-- merma_porcentaje = (entrada - salida) / entrada * 100  (coherente con cada par de pesos)
INSERT INTO lote_tostado (codigo, lote_verde_id, perfil_tueste, peso_entrada_kg, peso_salida_kg, merma_porcentaje, fecha_tueste, estado) VALUES
 ('LT-2026-002', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-004'), 'MEDIUM', 15.00, 12.75, 15.00, '2026-03-11 08:30:00', 'REGISTRADO'),
 ('LT-2026-003', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-005'), 'LIGHT',  10.00,  8.70, 13.00, '2026-03-13 09:00:00', 'REGISTRADO'),
 ('LT-2026-004', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-006'), 'MEDIUM', 20.00, 17.00, 15.00, '2026-02-21 07:45:00', 'REGISTRADO'),
 ('LT-2026-005', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-006'), 'DARK',   15.00, 12.45, 17.00, '2026-02-22 08:10:00', 'REGISTRADO'),
 ('LT-2026-006', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-007'), 'MEDIUM', 12.00, 10.20, 15.00, '2026-03-16 10:00:00', 'REGISTRADO'),
 ('LT-2026-007', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-008'), 'MEDIUM',  9.00,  7.65, 15.00, '2026-03-19 08:20:00', 'REGISTRADO'),
 ('LT-2026-008', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-009'), 'LIGHT',   4.00,  3.40, 15.00, '2026-03-21 09:30:00', 'REGISTRADO'),
 ('LT-2026-009', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-010'), 'DARK',    8.00,  6.80, 15.00, '2026-03-23 07:55:00', 'REGISTRADO'),
 ('LT-2026-010', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-011'), 'MEDIUM', 25.00, 21.25, 15.00, '2026-02-26 08:40:00', 'REGISTRADO'),
 ('LT-2026-011', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-008'), 'DARK',   11.00,  9.13, 17.00, '2026-03-24 11:00:00', 'REGISTRADO'),
 ('LT-2026-012', (SELECT id FROM lote_cafe_verde WHERE codigo='LV-2026-012'), 'MEDIUM', 10.00,  8.50, 15.00, '2026-03-26 09:15:00', 'ANULADO');
