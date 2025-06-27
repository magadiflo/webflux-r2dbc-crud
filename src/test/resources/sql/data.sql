INSERT INTO authors(first_name, last_name, birthdate)
VALUES
('Belén', 'Velez', '2006-06-15'),
('Marco', 'Salvador', '1995-06-09'),
('Greys', 'Briones', '2001-10-03'),
('Luis', 'Sánchez', '1997-09-25');

INSERT INTO books(title, publication_date, online_availability)
VALUES
('Los ríos profundos', '1999-01-15', true),
('La ciudad y los perros', '1985-03-18', true),
('El zorro de arriba y el zorro de abajo', '2002-05-06', false),
('Redoble por Rancas', '1988-07-15', true);

-- Book 1: tiene como author a Belén y Marco
INSERT INTO book_authors(book_id, author_id)
VALUES
(1, 1),
(1, 2);

-- Book 2: tiene como author a Greys
INSERT INTO book_authors(book_id, author_id)
VALUES
(2, 3);
