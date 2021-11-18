USE `decide.madrid_2019_09`;

-- Search proposals by linker
SELECT id, date, LENGTH(summary) AS 'length', title, summary, text, numComments, numSupports
  FROM proposals
 WHERE summary LIKE '% dado que %'
 ORDER BY LENGTH(summary)
 LIMIT 10;

-- Search proposals by ID
SELECT id, title, summary, numComments
  FROM proposals
 WHERE id IN (867, 11088, 11890, 17080)
 ORDER BY id;

-- Search proposals by topic and linker
SELECT COUNT(*)
  FROM (
SELECT id, date, LENGTH(summary) AS 'length', title, summary, text, numComments, numSupports
  FROM proposals
 WHERE (summary LIKE '% porque %' OR summary LIKE '% debido a %' OR summary LIKE '% ya que %' OR
        summary LIKE '% pues %' OR summary LIKE '% dado que %' OR summary LIKE '% pero %' OR 
        summary LIKE '% sin embargo %' OR summary LIKE '% a diferencia de %' OR 
        summary LIKE '% por el contrario %' OR summary LIKE '% aunque %')
 ORDER BY LENGTH(summary)) AS t;
 
-- Search comments by proposalsId
SELECT id, proposalId, parentId, userId, text
  FROM proposal_comments
 WHERE proposalId IN (340,867,992,1267,1287,1432,1985,4065,4107,4671)
 ORDER BY proposalId, parentId;
