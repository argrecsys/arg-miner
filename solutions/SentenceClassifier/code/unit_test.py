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
import util_lib as ul

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

# Create constituency parse tree (CPT) of sentences
def test_constituency_parse(es_text, port=9000):
    
    try:
        constituency_parse = ''
        
        # Construct a CoreNLPClient with some basic annotators
        with CoreNLPClient(properties='spanish', annotators=['tokenize', 'ssplit', 'mwt', 'pos', 'lemma', 'ner', 'depparse', 'kbp'], 
                           memory='2G', endpoint='http://localhost:' + str(port), be_quiet=False) as client:
            # submit the request to the server
            es_doc = client.annotate(es_text)
            
            print('---')    
            print('Constituency parse of sentences')
            for i in range(len(es_doc.sentence)):
                sentence = es_doc.sentence[i]
                
                # get the constituency parse of the current sentence
                constituency_parse += 'Sentence ' + str(i+1) + ':\n'
                constituency_parse += str(sentence.parseTree) + '\n'
                
    except Exception as e:
        constituency_parse = ''
        print('Error:', e)
    finally:
        ul.close_process_by_port(port)
        ul.save_text_to_file(constituency_parse, '../result/constituency.txt')

# Create dependency parse tree (DPT) of sentences
def test_dependency_parse(es_text, port=9000):
    
    try:
        dependency_parse = ''
        
        # Construct a CoreNLPClient with some basic annotators
        with CoreNLPClient(properties='spanish', annotators=['tokenize', 'ssplit', 'mwt', 'pos', 'lemma', 'ner', 'depparse', 'kbp'], 
                           memory='2G', endpoint='http://localhost:' + str(port), be_quiet=False) as client:
            # submit the request to the server
            es_doc = client.annotate(es_text)

            print('---')    
            print('Dependency parse of sentences')
            for i in range(len(es_doc.sentence)):
                sentence = es_doc.sentence[i]
                
                # get the dependency parse of the current sentence
                dependency_parse += 'Sentence ' + str(i+1) + ':\n'
                dependency_parse += str(sentence.basicDependencies) + '\n'
                    
    except Exception as e:
        dependency_parse = ''
        print('Error:', e)
    finally:
        ul.close_process_by_port(port)
        ul.save_text_to_file(dependency_parse, '../result/dependency.txt')

print("=====")
print("Begin")
print("=====\n")

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
