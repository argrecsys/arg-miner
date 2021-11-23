USE `decide.madrid_2019_09`;

-- Grouping comments by proposalsId
SELECT proposalId, COUNT(*) AS "count"
  FROM proposal_comments
 WHERE proposalId IN (23219, 7, 1419, 64, 15711, 59, 1085, 2145, 3206, 18750)
 GROUP BY proposalId
 ORDER BY count DESC, proposalId;
 
-- Search comments by proposalsId
SELECT id, proposalId, parentId, userId, text
  FROM proposal_comments
 WHERE proposalId IN (2145)
 ORDER BY proposalId, parentId;
