USE `decide.madrid_2019_09`;

SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
  FROM proposals
 WHERE id IN (15707, 23248);

-- El bus 5 pasa por general martinez campos pero no para ayi
UPDATE proposals
   SET summary = 'El bus 5 pasa por general martinez campos pero no para allí.'
 WHERE id = 15707;

-- En Carabanchel han aumentado los delitos por robo en personas y viviendas.sin embargo la dotacion policial para el distrito se ha disminuido.
UPDATE proposals
   SET summary = 'En Carabanchel han aumentado los delitos por robo en personas y viviendas, sin embargo la dotacion policial para el distrito se ha disminuido.'
 WHERE id = 23248;
