-- Representative synthetic data (fictional names only).
UPDATE hubs SET opening_time = '07:00', closing_time = '23:00' WHERE name IN ('Hub Bloco A', 'Hub Bloco B');

INSERT INTO scooters (code, hub_id, status)
SELECT s.code, h.id, s.status
FROM (VALUES
        ('TR-001', 'Hub Bloco A', 'AVAILABLE'),
        ('TR-002', 'Hub Bloco A', 'AVAILABLE'),
        ('TR-003', 'Hub Bloco A', 'MAINTENANCE'),
        ('TR-004', 'Hub Bloco B', 'AVAILABLE'),
        ('TR-005', 'Hub Garagem Central', 'AVAILABLE'),
        ('TR-006', 'Hub Garagem Central', 'AVAILABLE')
     ) AS s (code, hub_name, status)
JOIN hubs h ON h.name = s.hub_name;

INSERT INTO residents (name, subfleet_id)
SELECT r.name, f.id
FROM (VALUES ('Tiago Mendes'), ('Rita Faria'), ('Hugo Pires')) AS r (name)
CROSS JOIN subfleets f
WHERE f.name = 'Residencial Quinta das Acácias';
