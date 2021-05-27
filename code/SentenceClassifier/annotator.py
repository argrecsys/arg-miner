# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Version: 0.3.0
    Description: 
"""

# Text annotator class
class Annotator:
    
    # Default class constructor
    def __init__(self, linkers:dict) -> None:
        self.END_SENTENCE = "."
        self.SPLIT_TOKEN = " "
        self.JOIN_TOKEN = "-"
        self.linkers = linkers
        self.create_ngrams()
    
    # Create n-grams
    def create_ngrams(self) -> None:        
        uni_grams = {}
        bi_grams = {}
        tri_grams = {}
        four_grams = {}
        five_grams = {}
        
        # Save n-grams list
        for k, v in self.linkers.items():
            n = len(k.split(self.SPLIT_TOKEN))
            n_gram = k.replace(self.SPLIT_TOKEN, self.JOIN_TOKEN)
            
            if n == 1:
                uni_grams[n_gram] = v
            elif n == 2:
                bi_grams[n_gram] = v
            elif n == 3:
                tri_grams[n_gram] = v
            elif n == 4:
                four_grams[n_gram] = v
            elif n == 5:
                five_grams[n_gram] = v
            else:
                print(n, k, v)
                
        # Store them
        n_grams = {}
        n_grams[1] = uni_grams
        n_grams[2] = bi_grams
        n_grams[3] = tri_grams
        n_grams[4] = four_grams
        n_grams[5] = five_grams
        
        self.n_grams = {}
        for k in sorted(n_grams.keys(), reverse=True):
            self.n_grams[k] = n_grams[k]
    
    # Splits sentences by END_SENTENCE token
    def split_sentences(self, text:str) -> list:
        sentences = [sentence.strip() for sentence in text.split(self.END_SENTENCE) if sentence.strip() != '']
        return sentences
    
    # Function that labels text (proposals or comments) from a list of linkers
    def label_text(self, text:str)->dict:
        annotation = { 'text': text, 'linkers': {}}
        
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
                                annotation['linkers'][j] = linker
                            
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

