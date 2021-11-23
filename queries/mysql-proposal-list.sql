USE `decide.madrid_2019_09`;

SELECT p.id, p.date, p.title, 
       IFNULL(GROUP_CONCAT(DISTINCT pc.category), '') AS categories,
       IFNULL(GROUP_CONCAT(DISTINCT pd.district), '') AS districts,
       IFNULL(GROUP_CONCAT(DISTINCT pt.topic), '') AS topic
  FROM proposals AS p
  LEFT OUTER JOIN
       proposal_categories AS pc ON p.id = pc.id
  LEFT OUTER JOIN
       proposal_locations AS pd ON p.id = pd.id
  LEFT OUTER JOIN
       proposal_topics AS pt ON p.id = pt.id
 WHERE p.id IN (1287, 1432, 1985, 4107, 4671, 6600, 8116, 11530, 15004, 17562, 24693)
 GROUP BY p.id, p.date, p.title;
