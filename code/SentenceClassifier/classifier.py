# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Version: 0.2.0
    Description: 
"""

# Import custom libraries
import util_lib as ul
import data_layer as dl

# Import libraries
import csv
from datetime import datetime
import enum

# Using enum class create the Language enumeration
class Language(enum.Enum):
    ENGLISH = 'en'
    SPANISH = 'es'
    
    def __str__(self):
        return self.value

# DB function - Get database credentials
def get_db_credentials():
    yaml_path = 'config\db_config.yml'
    db_login = ul.get_dict_from_yaml(yaml_path)
    return db_login

# DB function - Get linker list (Spanish or English)
def get_linker_list(lang:str='es')->dict:
    linkers = {}
    
    filepath = "../../data/linkers_{}.csv".format(lang.lower())
    
    with open(filepath, mode='r', encoding='utf-8') as file:
        reader = csv.reader(file)
        connectors = { row[0] : row[1] for row in reader if 'category' not in row[1] }
        
        # Sort dictionary by key length 
        for k in sorted(connectors, key=len, reverse=True):
            linkers[k] = connectors[k]
        
    return linkers

# DB function - Query list of proposal
def get_proposals(db_layer:dl.DataLayer, encoding:str='UTF-8')->dict:
    proposal_list = {}
    
    query = """
            SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
              FROM proposals
             LIMIT 10;
            """
            
    data = db_layer.execute_mysql_query(query)
    print('n proposals:', len(data))
    
    if len(data):
        for row in data:
            pid, url, code, title, user_id, date, summary, text, num_comments, status, num_supports, is_association = row
            item = {'url': url, 'code': code, 'user_id': user_id, 'summary': summary.decode(encoding),
                    'text': text.decode(encoding), 'num_comments': num_comments, 'status': status,
                    'num_supports': num_supports, 'is_association': is_association}
            proposal_list[pid] = item
        
    return proposal_list

# DB function - Query list of proposal's comments
def get_proposal_comments(db_layer:dl.DataLayer, proposal_id:int, encoding:str='UTF-8')->dict:
    comment_list = {}
    
    query = """
            SELECT id, parentId, page, userId, userType, date, time, text, numVotes, numPositiveVotes, numNegativeVotes
              FROM proposal_comments
             WHERE proposalId = {};
            """
    query = query.format(proposal_id)
    
    data = db_layer.execute_mysql_query(query)
    print('n comments:', len(data))
    
    if len(data):
        for row in data:
            cid, parent_id, page, user_id, user_type, date, time, text, num_votes, num_positive_votes, num_negative_votes = row
            item = {'parent_id': parent_id, 'page': page, 'user_id': user_id, 'user_type': user_type, 
                    'date': date, 'time': time, 'text': text.decode(encoding), 'num_votes': num_votes, 
                    'num_positive_votes': num_positive_votes, 'num_negative_votes': num_negative_votes}
            comment_list[cid] = item
        
    return comment_list 

# Function that labels text (proposals or comments) from a list of linkers
def label_text(text:str, linkers:dict)->dict:
    annotation = {}
    n = len(text)
    
    labels = {}
    categories = {}
    
    i = 0
    while i < n:
        found_token = False
        
        # Find linkers occurrence
        for linker, category in linkers.items():
            m = len(linker)            
            
            if text[i:i+m] == linker:
                labels[linker] = labels.get(linker, 0) + 1
                categories[category] = categories.get(category, 0) + 1
                found_token = True
                break
                
        # Update index
        if found_token:
            i = i + m
        else:
            i += 1
    
    annotation = {'linker': labels, 'category': categories, 'text': text}
    
    return annotation

# Function that labels the proposals
def label_proposals(db_layer:dl.DataLayer, linkers:dict)->dict:
    result = {}
    
    proposal = get_proposals(db_layer)
    
    if len(proposal) and len(linkers):
        
        for key, value in proposal.items():
            summary = value['summary'].lower()
            result[key] = label_text(summary, linkers)
            print(result[key])
            
    return result

# Function that labels the proposal's comments
def label_comments(db_layer:dl.DataLayer, key:int, linkers:dict)->dict:
    result = {}
    
    comments = get_proposal_comments(db_layer, key)
    
    if len(comments) and len(linkers):
        
        for key, value in comments.items():
            summary = value['text'].lower()
            result[key] = label_text(summary, linkers)
    
    return result
    
# Start poing of the program
def main() -> None:
    print(">> START PROGRAM:", datetime.now())
    lang = Language.SPANISH.value
    
    # 1. Get database credentials
    db_login = get_db_credentials()
    
    # 2. Get linker list
    linkers = get_linker_list(lang)
    
    # 3. Create data layer object
    db_layer = dl.DataLayer(db_login)
    
    # Labeling proposals using linkers
    proposal_results = label_proposals(db_layer, linkers)
    print('end')
    return 0
    
    for pid, p_value in proposal_results.items():
        p_linkers = p_value['linker']
        p_categories = p_value['category']
        p_text = p_value['text']
        
        # Proposals with more linkers
        if len(p_linkers) > 5 and len(p_categories) > 3:
            print('++', pid, p_linkers, p_categories, p_text)
            
            # Labeling proposal's comments using linkers
            comment_results = label_comments(db_layer, pid, linkers)
            
            for cid, c_value in comment_results.items():
                c_linkers = c_value['linker']
                c_categories = c_value['category']
                c_text = c_value['text']
                
                # Proposals with more linkers
                if len(c_linkers) > 0 and len(c_categories) > 0:
                    print('  ', cid, c_linkers, c_categories, c_text)
    
    print(">> END PROGRAM:", datetime.now())

#####################
### Start Program ###
#####################
main()
#####################
#### End Program ####
#####################
