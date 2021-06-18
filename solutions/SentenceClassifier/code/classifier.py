# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: June 18, 2021
    Version: 0.6.0
    Description: 
"""

# Import custom libraries
import annotator as an
import data_layer as dl
import file_layer as fl

# Import libraries
import enum
from datetime import datetime

# Using enum class create the Language enumeration
class Language(enum.Enum):
    ENGLISH = 'en'
    SPANISH = 'es'
    
    def __str__(self):
        return self.value

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
    lang = Language.SPANISH.value
    
    # 1. Get database credentials
    db_login = fl.get_db_credentials()
    
    # 2. Create data layer object
    db_layer = dl.DataLayer(db_login)
    
    # 3. Get lexicon by language
    lexicon = fl.get_lexicon(lang)
    
    # 4. Create annotator object
    annotator = an.Annotator(lang, lexicon)
    
    # 5. Annotate proposals and their comments
    annotate_proposals(db_layer, annotator)

#####################
### START PROGRAM ###
#####################
if __name__ == "__main__":
    print(">> START PROGRAM:", datetime.now())
    main()
    print(">> END PROGRAM:", datetime.now())
#####################
#### End Program ####
#####################
