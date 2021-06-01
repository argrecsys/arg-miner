# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Version: 0.4.0
    Description: 
"""

# Import NLP libraries
import stanza

# Text annotator class
class Annotator:
    
    # Default class constructor
    def __init__(self, lang:str, linkers:dict) -> None:
        self.stanza_pl = stanza.Pipeline(lang)
        self.END_SENTENCE = "."
        self.SPLIT_TOKEN = " "
        self.JOIN_TOKEN = "-"
        self.linkers = linkers
        self.create_ngrams()
    
    # Create n-grams
    def create_ngrams(self) -> None:        
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
    
    # Splits sentences by END_SENTENCE token
    def split_sentences(self, text:str, simple:bool=False) -> list:
        sentences = []
        
        if simple:
            sentences = [sentence.strip() for sentence in text.split(self.END_SENTENCE) if sentence.strip() != '']
            print(sentences)
        else:
            nlp_doc = self.stanza_pl(text)
            sentences = [sentence.text.strip() for sentence in nlp_doc.sentences if sentence.text.strip() != '']
        
        return sentences    
    
    # Function that labels text (proposals or comments) from a list of linkers
    def label_text(self, key:str, text:str)->dict:
        annotation = { 'key': key, 'text': text, 'linkers': {} }
        
        # Get sentences
        sentences = self.split_sentences(text)
        
        for i in range(len(sentences)):
            sentence = sentences[i]
            tokens = sentence.split(self.SPLIT_TOKEN)
            
            j = 0
            while j < len(tokens):
                    
                for k, n_gram_list in self.n_grams.items():
                    found_token = False
                    
                    if j + k <= len(tokens):
                        l = 0
                        
                        while l < len(n_gram_list) and not found_token:
                            linker = list(n_gram_list.keys())[l]
                            if linker == self.JOIN_TOKEN.join(tokens[j:(j+k)]):
                                found_token = True
                                index = str(i)+'-'+str(j)
                                annotation['linkers'][index] = linker
                            
                            l += 1
                    
                    # Update index
                    if found_token:
                        break
                    
                # Update index
                if found_token:
                    j = j + k
                else:
                    j += 1
        
        return annotation

