USE `decide.madrid_2019_09`;

SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
  FROM proposals
 WHERE id IN (1432, 4696, 15004, 15707, 23248);

-- summary: El parque de madrid rio se creo prometiendo que el transporte publico llearia hasta alli, pero es falso, metro de legazpi queda lejos y los autobuses son nulos.
UPDATE proposals
   SET summary = 'El parque de Madrid Río se creó prometiendo que el transporte público llegaría hasta allí, pero es falso, metro de Legazpi queda lejos y los autobuses son nulos.'
 WHERE id = 1432;

-- title: Iluminacion de pasos de cebra
-- summary: Se ha vuelto un peligro cruzar en algunos pasos de cebra, debido a la baja iluminacion que tienen.
UPDATE proposals
   SET title = 'Iluminación de pasos de cebra'
     , summary = 'Se ha vuelto un peligro cruzar en algunos pasos de cebra, debido a la baja iluminación que tienen.'
 WHERE id = 4696;

-- summary: Estaria bien que hubiese un bus de la EMT de Alonso Martinez a Moncloa porque facilitaria el transporte a las personas que necesitan ir hacia ayi y se formaria menos atascos
UPDATE proposals
   SET summary = 'Estaria bien que hubiese un bus de la EMT de Alonso Martinez a Moncloa porque facilitaria el transporte a las personas que necesitan ir hacia allí y se formaria menos atascos'
 WHERE id = 15004;

-- summary: El bus 5 pasa por general martinez campos pero no para ayi
UPDATE proposals
   SET summary = 'El bus 5 pasa por general martinez campos pero no para allí.'
 WHERE id = 15707;

-- summary: En Carabanchel han aumentado los delitos por robo en personas y viviendas.sin embargo la dotacion policial para el distrito se ha disminuido.
UPDATE proposals
   SET summary = 'En Carabanchel han aumentado los delitos por robo en personas y viviendas, sin embargo la dotacion policial para el distrito se ha disminuido.'
 WHERE id = 23248;
