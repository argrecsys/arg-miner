# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Version: 0.4.0
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
        linkers = { row[0] : row[1] for row in reader if 'category' not in row[1] }        
    
    return linkers

# Function that labels the proposals
def label_proposals(db_layer:dl.DataLayer, annotator:an.Annotator)->dict:
    result = {}
    
    proposal = db_layer.get_proposals()
    n = len(proposal)
    print('n proposals:', n)
    
    if n > 0:
        count_sent_arg = 0
        for key, value in proposal.items():
            text = value['summary'].strip()
            annotation = annotator.label_text(text)
            
            if len(annotation['linkers']) > 0:
                result[key] = annotation
                count_sent_arg  += 1
        
        print('total sentences with linkers:', round(count_sent_arg / n, 4))
    
    return result

# Function that labels the proposal's comments
def label_comments(db_layer:dl.DataLayer, annotator:an.Annotator, key:int)->dict:
    result = {}
    
    comments = db_layer.get_proposal_comments(key)
    n = len(comments)
    print('n comments:', n)
    
    if n > 0:
        
        for key, value in comments.items():
            text = value['text'].strip().lower()
            annotation = annotator.label_text(text)
            
            if len(annotation['linkers']) > 0:
                result[key] = annotation
    
    return result

# Function that annotates proposals and their comments
def annotate_proposals(db_layer:dl.DataLayer, annotator:an.Annotator) -> None:
    
    # Labeling proposals using linkers
    proposal_results = label_proposals(db_layer, annotator)
    print(proposal_results)
    
    for pid, p_value in proposal_results.items():
        p_linkers = p_value['linkers']
        p_text = p_value['text']
        
        # Proposals with more linkers
        if len(p_linkers) > 0:
            print('++', pid, p_text, p_linkers)
            
            # Labeling proposal's comments using linkers
            comment_results = label_comments(db_layer, annotator, pid)
            
            for cid, c_value in comment_results.items():
                c_linkers = c_value['linkers']
                c_text = c_value['text']
                
                # Proposals with more linkers
                if len(c_linkers) > 0:
                    print('  ', cid, c_text, c_linkers)    

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
    annotator = an.Annotator(lang, rht_linkers)
    
    # 5. Annotate proposals and their comments
    annotate_proposals(db_layer, annotator)
    
    print(">> END PROGRAM:", datetime.now())

#####################
### Start Program ###
#####################
main()
#####################
#### End Program ####
#####################
