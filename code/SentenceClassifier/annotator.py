# -*- coding: utf-8 -*-
"""
Created on Wed May 26 13:24:48 2021

@author: ansegura
"""

# Text annotator class
class Annotator:
    
    # Default class constructor
    def __init__(self, linkers:dict) -> None:
        self.linkers = linkers
    
    # Function that labels text (proposals or comments) from a list of linkers
    def label_text(self, text:str)->dict:
        annotation = {}
        n = len(text)
        
        labels = {}
        categories = {}
        
        i = 0
        while i < n:
            found_token = False
            
            # Find linkers occurrence
            for linker, category in self.linkers.items():
                m = len(linker)            
                
                if text[i:i+m] == linker:
                    labels[linker] = labels.get(linker, 0) + 1
                    categories[category] = categories.get(category, 0) + 1
                    found_token = True
                    break
                    
            # Update index
            if found_token:
                i = i + m
            else:
                i += 1
        
        annotation = {'linker': labels, 'category': categories, 'text': text}
        
        return annotation
