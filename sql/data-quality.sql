USE `decide.madrid_2019_09`;

SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
  FROM proposals
 WHERE id IN (4696, 15707, 23248);

-- title: Iluminacion de pasos de cebra
-- summary: Se ha vuelto un peligro cruzar en algunos pasos de cebra, debido a la baja iluminacion que tienen.
UPDATE proposals
   SET title = 'Iluminación de pasos de cebra'
     , summary = 'Se ha vuelto un peligro cruzar en algunos pasos de cebra, debido a la baja iluminación que tienen.'
 WHERE id = 4696;

-- summary: El bus 5 pasa por general martinez campos pero no para ayi
UPDATE proposals
   SET summary = 'El bus 5 pasa por general martinez campos pero no para allí.'
 WHERE id = 15707;

-- summary: En Carabanchel han aumentado los delitos por robo en personas y viviendas.sin embargo la dotacion policial para el distrito se ha disminuido.
UPDATE proposals
   SET summary = 'En Carabanchel han aumentado los delitos por robo en personas y viviendas, sin embargo la dotacion policial para el distrito se ha disminuido.'
 WHERE id = 23248;
