USE `decide.madrid_2019_09`;

-- Grouping comments by proposalsId
SELECT proposalId, COUNT(*)
  FROM proposal_comments
 WHERE proposalId IN (23219, 7, 1419)
 GROUP BY proposalId
 ORDER BY proposalId, parentId;
 
-- Search comments by proposalsId
SELECT id, proposalId, parentId, userId, text
  FROM proposal_comments
 WHERE proposalId IN (23219, 7, 1419)
 ORDER BY proposalId, parentId;
