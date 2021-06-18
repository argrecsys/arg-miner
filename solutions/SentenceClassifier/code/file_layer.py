# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: June 18, 2021
    Version: 0.6.0
    Description: File(s) layer.
"""

# Import libraries
import csv
import util_lib as ul

# File function - Get database credentials
def get_db_credentials():
    yaml_path = 'config\db_config.yml'
    db_login = ul.get_dict_from_yaml(yaml_path)
    return db_login

# File function - Get linker list (Spanish or English)
def get_lexicon(lang:str='en', encoding='utf-8')->dict:
    lexicon = {}
    filepath = "../../../data/lexicon.csv"
    
    linker_ix = -1
    if lang == 'en':
        linker_ix = 3
    elif lang == 'es':
        linker_ix = 4
    
    if linker_ix > -1:
        with open(filepath, mode='r', encoding=encoding) as file:
            reader = csv.reader(file)
            for row in reader:
                if row[0] != 'category':
                    linker = row[linker_ix]
                    info = { 'category': row[0], 'sub_category': row[1], 'relation_type': row[2] }
                    lexicon[linker] = info
        
    return lexicon
