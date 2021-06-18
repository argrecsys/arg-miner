# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: June 18, 2021
    Version: 0.6.0
    Description: 
"""

# Import NLP libraries
import enum
import stanza
import spacy
import nltk.data

# Using enum class create the Language enumeration
class SplitType(enum.Enum):
    STANZA = 'stanza'
    SPACY = 'spacy'
    NLTK = 'nltk'
    
    def __str__(self):
        return self.value

# Text annotator class
class Annotator:
    
    # Default class constructor
    def __init__(self, lang:str, linkers:dict, split_mode:str=SplitType.SPACY.value) -> None:
        
        # Class variables
        self.ready = True
        self.linkers = linkers
        self.split_mode = split_mode
        self.END_SENTENCE = "."
        self.SPLIT_TOKEN = " "
        self.JOIN_TOKEN = "-"
        
        # Create models from the language
        if lang.lower() == 'en':
            self.nlp_stanza = stanza.Pipeline(lang='en')
            self.nlp_spacy = spacy.load('en_core_web_sm')
            self.nlp_nltk = nltk.data.load('tokenizers/punkt/english.pickle')
        
        elif lang.lower() == 'es':
            self.nlp_stanza = stanza.Pipeline(lang='es')
            self.nlp_spacy = spacy.load('es_core_news_sm')
            self.nlp_nltk = nltk.data.load('tokenizers/punkt/spanish.pickle')
        else:
            self.ready = False
            self.nlp_stanza = None
            self.nlp_spacy = None
            self.nlp_nltk = None
        
        # Create n-grams list
        self.__create_ngrams()
    
    # Splits sentences by END_SENTENCE token
    def split_sentences(self, text:str) -> list:
        sentences = []
        
        if self.ready:
            if self.split_mode == SplitType.STANZA.value:
                nlp_doc = self.nlp_stanza(text)
                sentences = [sentence.text.strip() for sentence in nlp_doc.sentences if sentence.text.strip() != '']
            
            elif self.split_mode == SplitType.SPACY.value:
                nlp_doc = self.nlp_spacy(text)
                sentences = [sentence.text.strip() for sentence in nlp_doc.sents if sentence.text.strip() != '']
            
            elif self.split_mode == SplitType.NLTK.value:
                sentences = self.nlp_nltk.tokenize(text)
        else:
            sentences = [sentence.strip() for sentence in text.split(self.END_SENTENCE) if sentence.strip() != '']
            
        return sentences    
    
    # Function that labels text (proposals or comments) from a list of linkers
    def label_text(self, ori_text:str) -> dict:
        corr_text = self.__fix_original_text(ori_text)
        annotation = { 'original_text': ori_text, 'corrected_text': corr_text, 'linkers': [] }
        
        # Get sentences from corrected text
        sentences = self.split_sentences(corr_text)
        
        glb_index = 0
        for i in range(len(sentences)):
            sentence = sentences[i]
            tokens = sentence.lower().split(self.SPLIT_TOKEN)
            
            j = 0
            while j < len(tokens):
                    
                for k, n_gram_list in self.n_grams.items():
                    found_token = False
                    
                    if j + k <= len(tokens):
                        l = 0
                        
                        while l < len(n_gram_list) and not found_token:
                            linker = list(n_gram_list.keys())[l]
                            linker_info = n_gram_list[linker]
                            raw_target = self.JOIN_TOKEN.join(tokens[j:(j+k)])
                            target = self.__clean_target(raw_target)
                            
                            if linker == target:
                                found_token = True
                                rel_index = str(i)+'-'+str(j)
                                lnk_data = {'linker': linker, 'rel_index': rel_index, 
                                            'glb_index': glb_index, 'category': linker_info['category'], 
                                            'sub_category': linker_info['sub_category'], 
                                            'relation_type': linker_info['relation_type']}
                                annotation['linkers'].append(lnk_data)
                            
                            l += 1
                    
                    # Update index
                    if found_token:
                        break
                    
                # Update index
                if found_token:
                    glb_index += len(raw_target) + len(self.SPLIT_TOKEN)
                    j = j + k
                else:
                    glb_index += len(tokens[j]) + len(self.SPLIT_TOKEN)
                    j += 1            
        
        # Return text annotation
        return annotation
    
    # Obtain text phrases by using the linkers to split them
    def get_text_phrases_by_linkers(self, text, linkers):
        phrase_list = []
        
        ix_prev = 0
        for link in linkers:
            lnk = link['linker']
            g_ix = link['glb_index']
            phrase = text[ix_prev:g_ix]
            phrase_list.append(phrase)
            ix_prev = g_ix + len(lnk)
        
        return phrase_list
    
    # Create n-grams structure of linkers
    def __create_ngrams(self) -> None:        
        n_grams = {}
        
        # Save n-grams list
        for k, v in self.linkers.items():
            n = len(k.split(self.SPLIT_TOKEN))
            n_gram = k.replace(self.SPLIT_TOKEN, self.JOIN_TOKEN)
            
            # Store them
            if n in n_grams:
                n_grams[n][n_gram] = v
            else:                
                gram_list = { n_gram: v }
                n_grams[n] = gram_list
        
        # Sort and save it
        self.n_grams = {}
        for k in sorted(n_grams.keys(), reverse=True):
            self.n_grams[k] = n_grams[k]    
    
    # Clean the target linker by removing the marks on the boundaries
    def __clean_target(self, raw_target) -> str:
        new_target = raw_target
        
        if len(new_target) > 1:
            # Removing init marks
            if new_target[0] == '¡':
                new_target = new_target[1:]
            if new_target[0] == '¿':
                new_target = new_target[1:]
            if new_target[0] == '(':
                new_target = new_target[1:]
            
            # Removing end marks
            if new_target[-1] == '!':
                new_target = new_target[:-1]
            if new_target[-1] == '?':
                new_target = new_target[:-1]
            if new_target[-1] == ')':
                new_target = new_target[:-1]
            if new_target[-1] == '.':
                new_target = new_target[:-1]
        
        return new_target
    
    # Fix text to make it more apt to be separated into sentences and identify linkers
    def __fix_original_text(self, ori_text:str) -> str:
        corr_text = ori_text
        # TO-DO
        return corr_text
        