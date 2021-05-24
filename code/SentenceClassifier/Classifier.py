# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Description: 
"""

# Import custom libraries
import DataLayer as dl

# Import libraries
import csv
from datetime import datetime

# Get connector list (Spanish or English)
def get_connector_list(lang:str='es')->dict:
    connectors = {}
    
    filepath = "data/connectors_{}.csv".format(lang.lower())
    
    with open(filepath, mode='r', encoding='utf-8') as file:
        reader = csv.reader(file)
        connectors = { row[0] : row[1] for row in reader if 'category' not in row[1] }
    
    return connectors

# Query list of proposal
def get_proposals(encoding:str='UTF-8')->dict:
    proposal_list = {}
    
    query = """
            SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
              FROM proposals;
            """
            
    data = dl.execute_query(query)
    print('n data:', len(data))
    
    if len(data):
        for row in data:
            pid, url, code, title, user_id, date, summary, text, num_comments, status, num_supports, is_association = row
            item = {'url': url, 'code': code, 'user_id': user_id, 'summary': summary.decode(encoding),
                    'text': text.decode(encoding), 'num_comments': num_comments, 'status': status,
                    'num_supports': num_supports, 'is_association': is_association}
            proposal_list[pid] = item
        
    return proposal_list

# Query list of proposal's comments
def get_proposal_comments(proposal_id:int, encoding:str='UTF-8')->dict:
    comment_list = {}
    
    query = """
            SELECT id, parentId, page, userId, userType, date, time, text, numVotes, numPositiveVotes, numNegativeVotes
              FROM proposal_comments
             WHERE proposalId = {};
            """
    query = query.format(proposal_id)
    
    data = dl.execute_query(query)
    print('n data:', len(data))
    
    if len(data):
        for row in data:
            cid, parent_id, page, user_id, user_type, date, time, text, num_votes, num_positive_votes, num_negative_votes = row
            item = {'parent_id': parent_id, 'page': page, 'user_id': user_id, 'user_type': user_type, 
                    'date': date, 'time': time, 'text': text.decode(encoding), 'num_votes': num_votes, 
                    'num_positive_votes': num_positive_votes, 'num_negative_votes': num_negative_votes}
            comment_list[cid] = item
        
    return comment_list 

# Function that labels text (proposals or comments) from a list of connectors
def label_text(text:str, connectors:dict)->dict:
    annotation = {}
    
    labels = {}
    categories = {}
    
    for connector, category in connectors.items():
        tokens = text.split(" ")
        #n_occur = text.count(connector)
        if connector in text:
        #if connector in tokens:
            labels[connector] = labels.get(connector, 0) + 1
            categories[category] = categories.get(category, 0) + 1
    
    annotation = {'connector': labels, 'category': categories, 'text': text}
    
    return annotation

# Function that labels the proposals
def label_proposals(connectors:dict)->dict:
    result = {}
    
    proposal = get_proposals()
    
    if len(proposal) and len(connectors):
        
        for key, value in proposal.items():
            summary = value['summary'].lower()
            result[key] = label_text(summary, connectors)
            
    return result

# Function that labels the proposal's comments
def label_comments(key:int, connectors:dict)->dict:
    result = {}
    
    comments = get_proposal_comments(key)
    
    if len(comments) and len(connectors):
        
        for key, value in comments.items():
            summary = value['text'].lower()
            result[key] = label_text(summary, connectors)
    
    return result
    
# Start poing of the program
def main() -> None:
    print(">> START PROGRAM:", datetime.now())
    connectors = get_connector_list()
    
    # Labeling proposals using connectors
    proposal_results = label_proposals(connectors)
    
    for pid, p_value in proposal_results.items():
        p_connectors = p_value['connector']
        p_categories = p_value['category']
        p_text = p_value['text']
        
        # Proposals with more connectors
        if len(p_connectors) > 5 and len(p_categories) > 3:
            print('++', pid, p_connectors, p_categories, p_text)
            
            # Labeling proposal's comments using connectors
            comment_results = label_comments(pid, connectors)
            
            for cid, c_value in comment_results.items():
                c_connectors = c_value['connector']
                c_categories = c_value['category']
                c_text = c_value['text']
                
                # Proposals with more connectors
                if len(c_connectors) > 0 and len(c_categories) > 0:
                    print('  ', cid, c_connectors, c_categories, c_text)
    
    print(">> END PROGRAM:", datetime.now())

#####################
### Start Program ###
#####################
main()
#####################
#### End Program ####
#####################
