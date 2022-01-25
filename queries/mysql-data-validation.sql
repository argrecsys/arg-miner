USE `decide.madrid_2019_09`;

-- Validation of repeated proposals
SELECT summary, COUNT(*) AS n
  FROM proposals
 WHERE summary <> ''
 GROUP BY summary
 ORDER BY n DESC;

-- Validation of repeated comments
SELECT text, COUNT(*) AS n
  FROM proposal_comments
 WHERE text <> ''
 GROUP BY text
 ORDER BY n DESC;

-- Specific validation on proposals and comments
SELECT *
  FROM proposals
 WHERE summary LIKE '%El Encinar solo tiene una entrada/salida que esta permanentemente colapsada. Una salida directa a la M40 prolongando la C/ Luis M Feduchi aliviaria tambien el trafico de la A-1. Gracias !%';

SELECT *
  FROM proposal_comments
 WHERE text LIKE '%En Aluche tenemos este problema con la suciedad y dejadez del barrio, por favor VOTAD%';
