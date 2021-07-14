USE `decide.madrid_2019_09`;

-- Search by linker
SELECT id, date, LENGTH(summary) AS 'length', title, summary, text, numComments, numSupports
  FROM proposals
 WHERE summary LIKE '% dado que %'
 ORDER BY LENGTH(summary)
 LIMIT 10;

-- Search by ID
SELECT id, title, summary, numComments
  FROM proposals
 WHERE id IN (867, 11890, 17080)
 ORDER BY id;

-- Search by topic and linker
SELECT id, date, LENGTH(summary) AS 'length', title, summary, text, numComments, numSupports
  FROM proposals
 WHERE LOWER(summary) LIKE '%transporte%'
   AND (summary LIKE '% debido a %' OR summary LIKE '% porque %' OR summary LIKE '% pues %' OR summary LIKE '% dado que %' OR
        summary LIKE '% pero %' OR summary LIKE '% sin embargo %' OR summary LIKE '% a diferencia de %' OR summary LIKE '% por el contrario %')
 ORDER BY LENGTH(summary)
 LIMIT 15;
