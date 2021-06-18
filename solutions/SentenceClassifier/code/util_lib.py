# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura Tinoco
    Created On: June 18, 2021
    Version: 0.6.0
    Description: Library with utility functions
"""

# Import util libraries
import yaml
import csv
import os
import signal
import subprocess

############################
### Start Util Functions ###
############################

# Data quality function - Converts a string to a number (int or float) 
def dq_parse_num(n):
    v = 0
    
    if n == 'N/A':
        v = -1
    else:
        n = n.replace(',', '').replace('+', '').strip()
        
        if '.' in n:
            v = float(n)
        elif n != '':
            v = int(n)
    
    return v

# Data quality function - Clean html text and convert it to plain text
def dq_clean_html_text(html_text, encoding='utf-8'):
    plain_text = html_text.strip().replace(u'\n', u'')
    return plain_text.encode('ascii', 'ignore').decode(encoding)

# Util function - Read dict from yaml file
def get_dict_from_yaml(yaml_path):
    result = dict()
    
    with open(yaml_path) as f:
        yaml_file = f.read()
        result = yaml.load(yaml_file, Loader=yaml.FullLoader)
    
    return result

# Util function - Read CSV file from full filepath
def read_csv_file(filename, encoding='utf-8', delimiter=','):
    data = []
    
    with open(filename, 'r', encoding=encoding) as f:
        csv_file = csv.reader(f, delimiter=delimiter)
        for row in csv_file:
            data.append(row)
            
    return data

# Util function - Save test to plain file
def save_text_to_file(text, filename, encoding='utf-8'):
    result = False
    
    if text != '':
        with open(filename, 'w', encoding=encoding) as f:
            f.write(text)
            result = True
    
    return result

# Util function - Save data list to CSV file
def save_data_to_csv(dt, filename, header, encoding='utf-8'):
    result = False

    # Validating data
    if dt and len(dt):
        
        # Saving data in CSV file
        with open(filename, 'w', encoding=encoding, newline='') as f:
            wr = csv.writer(f, delimiter=',')
            wr.writerow(header)
            for row in dt:
                wr.writerow(row)
            result = True
    
    return result

# OS function - Forced close process by port
def close_process_by_port(port_id):
    command = "netstat -ano | findstr " + str(port_id) + ' | findstr LISTENING'
    c = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr = subprocess.PIPE)
    stdout, stderr = c.communicate()
    tokens = stdout.decode().strip().split(' ')
    if len(tokens) > 1:
        pid = int(tokens[-1])
        os.kill(pid, signal.SIGTERM)
        print('killed process:', pid)

##########################
### End Util Functions ###
##########################
