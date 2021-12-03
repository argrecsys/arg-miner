USE `decide.madrid_2019_09`;

-- Grouping comments by proposalsId
SELECT proposalId, COUNT(*) AS "count"
  FROM proposal_comments
 WHERE proposalId IN (5, 7, 8, 10, 11, 12, 14, 15, 16, 17, 19, 20)
 GROUP BY proposalId
 ORDER BY count DESC, proposalId;

-- Search comments by proposalsId
SELECT id, proposalId, parentId, userId, text
  FROM proposal_comments
 WHERE id IN (151201, 181174, 181259)
 ORDER BY proposalId, parentId;

-- Search comments by content
SELECT *
  FROM proposal_comments
 WHERE (text LIKE '% tambien %') OR (text LIKE '%, tambien %') OR (text LIKE '%...tambien %');
