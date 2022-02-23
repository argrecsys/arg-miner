USE `decide.madrid_2019_09`;

SELECT proposalId, value
  FROM metrics_controversy
 WHERE name = 'AGGREGATION'
 ORDER BY value DESC;
