# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura Tinoco
    Created On: June 22, 2021
    Version: 0.7.0
    Description: Unit test for Spanish
"""

# Import custom libraries
import annotator as an
import file_layer as fl
import util_lib as ul

# Import libraries
import enum
import stanza
import spacy
from datetime import datetime
from stanza.server import CoreNLPClient

# Using enum class create the Language enumeration
class Language(enum.Enum):
    ENGLISH = 'en'
    SPANISH = 'es'
    
    def __str__(self):
        return self.value
    
######################
# Step 1: Annotation #
######################
def test_annotation(es_text):
    result = ""
    
    # Create annotator object
    lexicon = fl.get_lexicon(curr_lang.value)
    annotator = an.Annotator(curr_lang.value, lexicon)
    
    # Annotate the text
    result += "Annotation:\n"
    annotations = annotator.label_text(es_text)
    result += str(annotations) + "\n"
    
    # Get phrases
    result += "\nPhrases:\n"
    phrase_list = annotator.get_text_phrases_by_linkers(es_text, annotations['linkers'])
    result += str(phrase_list) + "\n"

    # Save result into a plain file
    ul.save_text_to_file(result, '../result/annotation.txt')

#######################
# Step 2: POS - spaCy #
#######################
def test_pos(es_text):
    result = ""
    
    # Create model
    nlp_spacy = spacy.load('es_core_news_sm')
    es_doc = nlp_spacy(es_text)
    
    result += "POS:\n"
    for token in es_doc:
        result += token.text + ': ' + token.pos_ + ' - ' + token.dep_ + "\n"

    result += "\nFind named entities, phrases and concepts:\n"
    for entity in es_doc.ents:
        result += entity.text + '=' + entity.label_ + "\n"
    
    # Save result into a plain file
    ul.save_text_to_file(result, '../result/pos.txt')
    
######################
# Step 3: Dependency #
######################
def test_dependency(es_text):
    result = ""
    
    result += "Show dependency:\n"
    nlp_stanza = stanza.Pipeline(lang='es')
    es_doc = nlp_stanza(es_text)
    
    for sent in es_doc.sentences:
        for word in sent.words:
            head = sent.words[word.head-1].text if word.head > 0 else "root"
            result += word.text + " - " + str(word.head) + " - " + word.deprel + " - " + head + "\n"
    
    # Save result into a plain file
    ul.save_text_to_file(result, '../result/dependencies.txt')

# Create constituency parse tree (CPT) of sentences
def test_constituency_parse(es_text, port=9000):
    
    try:
        constituency_parse = ''
        
        # Construct a CoreNLPClient with some basic annotators
        # Options: 'tokenize', 'ssplit', 'mwt', 'pos', 'lemma', 'ner', 'parse', 'depparse', 'kbp'
        with CoreNLPClient(properties='spanish', annotators=['pos', 'parse', 'depparse'], 
                           memory='2G', endpoint='http://localhost:' + str(port), be_quiet=False) as client:
            # submit the request to the server
            es_doc = client.annotate(es_text)
            
            for i in range(len(es_doc.sentence)):
                sentence = es_doc.sentence[i]
                
                # get the constituency parse of the current sentence
                constituency_parse += '- Sentence ' + str(i+1) + ':\n'
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
        # Options: 'tokenize', 'ssplit', 'mwt', 'pos', 'lemma', 'ner', 'parse', 'depparse', 'kbp'
        with CoreNLPClient(properties='spanish', annotators=['pos', 'parse', 'depparse'], 
                           memory='2G', endpoint='http://localhost:' + str(port), be_quiet=False) as client:
            # submit the request to the server
            es_doc = client.annotate(es_text)
            
            for i in range(len(es_doc.sentence)):
                sentence = es_doc.sentence[i]
                
                # get the dependency parse of the current sentence
                dependency_parse += '- Sentence ' + str(i+1) + ':\n'
                dependency_parse += str(sentence.basicDependencies) + '\n'
                    
    except Exception as e:
        dependency_parse = ''
        print('Error:', e)
    finally:
        ul.close_process_by_port(port)
        ul.save_text_to_file(dependency_parse, '../result/dependency.txt')

###################
### START TESTS ###
###################
if __name__ == "__main__":
    print(">> START TESTS:", datetime.now())
    
    # 1. Variables
    curr_lang = Language.SPANISH
    id_text = 3732
    es_text = "Recientemente me mude de Mostoles (10.762 años ahi) a Batan y menudo cambio." #" Podrian pensar un poquito en dar a este barrio un lavado de cara , no? Por estar cerca de un parque de atracciones famoso... no debería ocurrir...estoy seguro!"
    print("{} (id: {}) - {}".format(curr_lang.name, id_text, es_text))
    
    # 2. Unit test
    test_annotation(es_text)
    test_pos(es_text)
    test_dependency(es_text)
    test_constituency_parse(es_text)
    test_dependency_parse(es_text)
    
    print(">> END TESTS:", datetime.now())
###################
#### END TESTS ####
###################
