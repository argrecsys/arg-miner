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
import annotator as an

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

# Function that labels the proposals
def label_proposals(db_layer:dl.DataLayer, annotator:an.Annotator)->dict:
    result = {}
    
    proposal = db_layer.get_proposals()
    print('n proposals:', len(proposal))
    
    if len(proposal):
        
        for key, value in proposal.items():
            summary = value['summary'].lower()
            result[key] = annotator.label_text(summary)
            print(result[key])
    
    return result

# Function that labels the proposal's comments
def label_comments(db_layer:dl.DataLayer, annotator:an.Annotator, key:int)->dict:
    result = {}
    
    comments = db_layer.get_proposal_comments(key)
    print('n comments:', len(comments))
    
    if len(comments):
        
        for key, value in comments.items():
            summary = value['text'].lower()
            result[key] = annotator.label_text(summary)
    
    return result

# Function that annotates proposals and their comments
def annotate_proposal(db_layer:dl.DataLayer, annotator:an.Annotator) -> None:
    
    # Labeling proposals using linkers
    proposal_results = label_proposals(db_layer, annotator)
    
    for pid, p_value in proposal_results.items():
        p_linkers = p_value['linker']
        p_categories = p_value['category']
        p_text = p_value['text']
        
        # Proposals with more linkers
        if len(p_linkers) > 0:
            print('++', pid, p_linkers, p_categories, p_text)
            
            # Labeling proposal's comments using linkers
            comment_results = label_comments(db_layer, annotator, pid)
            
            for cid, c_value in comment_results.items():
                c_linkers = c_value['linker']
                c_categories = c_value['category']
                c_text = c_value['text']
                
                # Proposals with more linkers
                if len(c_linkers) > 0:
                    print('  ', cid, c_linkers, c_categories, c_text)    

# Start poing of the program
def main() -> None:
    print(">> START PROGRAM:", datetime.now())
    lang = Language.SPANISH.value
    
    # 1. Get database credentials
    db_login = get_db_credentials()
    
    # 2. Create data layer object
    db_layer = dl.DataLayer(db_login)
    
    # 3. Get rhetorical linker list
    rht_linkers = get_linker_list(lang)
    
    # 4. Create annotator object
    annotator = an.Annotator(rht_linkers)
    
    # 5. Annotate proposals and their comments
    annotate_proposal(db_layer, annotator)
    
    print(">> END PROGRAM:", datetime.now())

#####################
### Start Program ###
#####################
main()
#####################
#### End Program ####
#####################
