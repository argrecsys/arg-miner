USE `decide.madrid_2019_09`;

SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
  FROM proposals
 WHERE id IN (1432, 4696, 7700, 15004, 15707, 23248);

-- summary: Me gustaria q alguien del.Ayuntamiento me diga, donde van los impuestos que pagamos los vecinos del Ensanche d Vallecas, esta todo sucio y lleno de escombros y ademas las zonas 'verdes' estan secas
UPDATE proposals
   SET summary = 'Me gustaria que alguien del Ayuntamiento me diga, donde van los impuestos que pagamos los vecinos del Ensanche de Vallecas, esta todo sucio y lleno de escombros y ademas las zonas ''verdes'' estan secas'
 WHERE id = 372;

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

-- summary: Que vuelva la linea nocturna de transporte publico 'L'. Cubren mejor el servicio y es conocida por todxs ya que siue el mismo recorrido del metro
UPDATE proposals
   SET summary = 'Que vuelva la linea nocturna de transporte publico \'L\'. Cubren mejor el servicio y es conocida por todos ya que sigue el mismo recorrido del metro'
 WHERE id = 7700;

-- summary: - Se entiende el motivo de los badenes , pero hay alternativas como Radar de tramo , mas vigilancia en ciertos lugares o incluso radar movil. Deberia ser ilegal poner obstaculos en la via.
UPDATE proposals
   SET summary = 'Se entiende el motivo de los badenes, pero hay alternativas como Radar de tramo, mas vigilancia en ciertos lugares o incluso radar movil. Deberia ser ilegal poner obstaculos en la via.'
 WHERE id = 11088;

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
 
-- text: -No es un problema solo de la Chopera, es del Puente Segovia, Virgen del Puerto,Ermita del Santo. Melancolicos. Muchos barrios entorno al Manzanares precemos autenticas islas en cuanto a transporte publico, o nos traen metro cosa que dudo o adecuan de una santa vez el transporte de autobuses de la EMT. Apoyo la propuesta.
UPDATE proposal_comments
   SET text = 'No es un problema solo de la Chopera, es del Puente Segovia, Virgen del Puerto,Ermita del Santo. Melancolicos. Muchos barrios entorno al Manzanares precemos autenticas islas en cuanto a transporte publico, o nos traen metro cosa que dudo o adecuan de una santa vez el transporte de autobuses de la EMT. Apoyo la propuesta.'
 WHERE id = 22184;

-- text: me parece buena idea, pero deberia habero otra tasa para los padres porq mas de 1, 2 y 3 veces he tenido q recoger la caca de ninos delante de los padres, con las bolsas de cacas de mi perros porq entre otras cosas los perros suelen comerse las cacas humanas.
UPDATE proposal_comments
   SET text = 'Me parece buena idea, pero deberia haber otra tasa para los padres porque mas de 1, 2 y 3 veces he tenido que recoger la caca de ninos delante de los padres, con las bolsas de cacas de mi perros porque entre otras cosas los perros suelen comerse las cacas humanas.'
 WHERE id = 30718;

-- text: Buenas tardes, lo primero que deben hacer es no seguir talando arboles sanos ,como estan haciendo en Colonia Manzanares. Mas de 230 arboles en tres semanas, con la excusa de que son muy peligrosos y se nos van a caer encima, pero , cuando empezaron a talar, solo 4 de los 230 estaban huecos por dentro. El resto ha sido un asesinato de seres vivos en toda regla y piensan seguir talando Luego nos hablan de contaminacion pero es una mentira, una incongruencia y un sinsentido. Todo esto aparte de que estamos al lado del Rio y estan renaturalizando para atraer mas aves y ,al mismo tiempo, les quitan arboles porque dicen que estan enfermos y se van a desplomar. Vamos una verguenza para un Ayuntamiento Verde. Deberian cuidarlos, podarlos y tratarlos , no matarlos Para mi es el principio del FINAL de este consistorio
UPDATE proposal_comments
   SET text = 'Buenas tardes. Lo primero que deben hacer es no seguir talando arboles sanos, como estan haciendo en Colonia Manzanares. Mas de 230 arboles en tres semanas, con la excusa de que son muy peligrosos y se nos van a caer encima, pero , cuando empezaron a talar, solo 4 de los 230 estaban huecos por dentro. El resto ha sido un asesinato de seres vivos en toda regla y piensan seguir talando. Luego nos hablan de contaminacion pero es una mentira, una incongruencia y un sinsentido. Todo esto aparte de que estamos al lado del rio y estan renaturalizando para atraer mas aves y, al mismo tiempo, les quitan arboles porque dicen que estan enfermos y se van a desplomar. Vamos una verguenza para un Ayuntamiento Verde. Deberian cuidarlos, podarlos y tratarlos, no matarlos. Para mi es el principio del FINAL de este consistorio.'
 WHERE id = 151201;
