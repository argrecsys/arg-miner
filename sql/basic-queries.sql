USE `decide.madrid_2019_09`;

SELECT id, date, LENGTH(summary) AS 'length', title, summary, text, numComments, numSupports
  FROM proposals
 WHERE summary LIKE '% dado que %'
 ORDER BY LENGTH(summary)
 LIMIT 20;

SELECT id, title, summary, numComments
  FROM proposals
 WHERE id IN (867, 11890, 17080)
 ORDER BY id;
