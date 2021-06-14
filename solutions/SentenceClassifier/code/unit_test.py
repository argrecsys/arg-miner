# -*- coding: utf-8 -*-
"""
Created on Wed Jun  2 11:44:46 2021

@author: ansegura
"""

import annotator as an
import enum
import csv
import stanza
import spacy
from stanza.server import CoreNLPClient
import os
import signal
import subprocess

# Using enum class create the Language enumeration
class Language(enum.Enum):
    ENGLISH = 'en'
    SPANISH = 'es'
    
    def __str__(self):
        return self.value

# DB function - Get linker list (Spanish or English)
def get_linker_list(lang:str='es')->dict:
    linkers = {}
    
    filepath = "../../../data/linkers_{}.csv".format(lang.lower())
    
    with open(filepath, mode='r', encoding='utf-8') as file:
        reader = csv.reader(file)
        linkers = { row[0] : row[1] for row in reader if 'category' not in row[1] }        
    
    return linkers
    
######################
# Step 1: Annotation #
######################
def test_annotation(es_text):
    
    # Create annotator object
    rht_linkers = get_linker_list(curr_lang.value)
    annotator = an.Annotator(curr_lang.value, rht_linkers)
    
    # Annotate the text
    print("\nAnnotation:")
    print("="*15)
    annotations = annotator.label_text(es_text)
    print(id_text, annotations)
    
    # Get phrases
    print("\nPhrases:")
    print("="*15)
    phrase_list = annotator.get_text_phrases_by_linkers(es_text, annotations['linkers'])
    print(phrase_list)

#######################
# Step 2: POS - spaCy #
#######################
def test_pos(es_text):
    # Create model
    print("\nCreate model.")
    print("="*15)
    nlp_spacy = spacy.load('es_core_news_sm')
    es_doc = nlp_spacy(es_text)
    
    print("\nPOS:")
    print("="*15)
    for token in es_doc:
        print(token.text, ':', token.pos_, '-', token.dep_)

    print("\nFind named entities, phrases and concepts:")
    print("="*15)
    for entity in es_doc.ents:
        print(entity.text, '=', entity.label_)
    
######################
# Step 3: Dependency #
######################
def test_dependency(es_text):
    print("\nShow dependency.")
    print("="*15)
    
    nlp_stanza = stanza.Pipeline(lang='es')
    es_doc = nlp_stanza(es_text)
    for sent in es_doc.sentences:
        print(sent.print_dependencies())

# Constituency parse of sentences
def test_constituency_parse(es_text):
    
    #properties='spanish', 
    try:
        constituency_parse = ''
        
        # Construct a CoreNLPClient with some basic annotators
        with CoreNLPClient(annotators=['tokenize','ssplit','pos','lemma','ner', 'parse', 'depparse','coref'], 
                           memory='2G', endpoint='http://localhost:9000', be_quiet=False) as client:
            # submit the request to the server
            es_doc = client.annotate(es_text)
    
            print('Constituency parse of sentences')
            print('---')
            for sentence in es_doc.sentence:
                # get the constituency parse of the current sentence
                constituency_parse += str(sentence.parseTree) + '\n'
                
    except Exception as e:
        constituency_parse = ''
        print(e)
    finally:
        close_process_by_port(9000)
        save_text_to_file(str(constituency_parse), '../result/constituency.txt')

# Dependency parse of sentences
def test_dependency_parse(es_text):
    
    #properties='spanish', 
    try:
        dependency_parse = ''
        
        # Construct a CoreNLPClient with some basic annotators
        with CoreNLPClient(annotators=['tokenize','ssplit','pos','lemma','ner', 'parse', 'depparse','coref'], 
                           memory='2G', endpoint='http://localhost:9000', be_quiet=False) as client:
            # submit the request to the server
            es_doc = client.annotate(es_text)
    
            print('Dependency parse of sentences')
            print('---')
            for sentence in es_doc.sentence:
                # get the dependency parse of the current sentence
                dependency_parse += str(sentence.basicDependencies) + '\n'
                    
    except Exception as e:
        dependency_parse = ''
        print(e)
    finally:
        close_process_by_port(9000)
        save_text_to_file(str(dependency_parse), '../result/dependency.txt')
    
print("=====")
print("Begin")
print("=====\n")

def close_process_by_port(port_id):
    command = "netstat -ano | findstr " + str(port_id) + ' | findstr LISTENING'
    c = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr = subprocess.PIPE)
    stdout, stderr = c.communicate()
    tokens = stdout.decode().strip().split(' ')
    if len(tokens) > 1:
        pid = int(tokens[-1])
        os.kill(pid, signal.SIGTERM)
        print('killed process:', pid)

# Util function - Save test to plain file
def save_text_to_file(text, filename, encoding='utf-8'):
    result = False
    
    if text != '':
        with open(filename, 'w', encoding=encoding) as f:
            f.write(text)
            result = True
    
    return result

# 1. Variables
curr_lang = Language.SPANISH
id_text = 3732
es_text = "Recientemente me mude de Mostoles (10.762 años ahi) a Batan y menudo cambio. Podrian pensar un poquito en dar a este barrio un lavado de cara , no? Por estar cerca de un parque de atracciones famoso... no debería ocurrir...estoy seguro!"
print(curr_lang.name, id_text, es_text)

# 2. Unit test
test_annotation(es_text)
test_pos(es_text)
test_dependency(es_text)
test_constituency_parse(es_text)
test_dependency_parse(es_text)

print("\n=====")
print("End")
print("=====")
