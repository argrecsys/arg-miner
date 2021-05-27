# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Version: 0.3.0
    Description: 
"""

# Import libraries
import mysql.connector
from mysql.connector import errorcode

# Data layer class
class DataLayer:
    
    # Default class constructor
    def __init__(self, db_login:dict) -> None:
        self.db_login = db_login
    
    # DB function - Query list of proposal
    def get_proposals(self, encoding:str='UTF-8')->dict:
        proposal_list = {}
        
        query = """
                SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation
                  FROM proposals;
                """
                #LIMIT 10;
        
        data = self.execute_mysql_query(query)
        
        if len(data):
            for row in data:
                pid, url, code, title, user_id, date, summary, text, num_comments, status, num_supports, is_association = row
                item = {'url': url, 'code': code, 'user_id': user_id, 'summary': summary.decode(encoding),
                        'text': text.decode(encoding), 'num_comments': num_comments, 'status': status,
                        'num_supports': num_supports, 'is_association': is_association}
                proposal_list[pid] = item
            
        return proposal_list
    
    # DB function - Query list of proposal's comments
    def get_proposal_comments(self, proposal_id:int, encoding:str='UTF-8')->dict:
        comment_list = {}
        
        query = """
                SELECT id, parentId, page, userId, userType, date, time, text, numVotes, numPositiveVotes, numNegativeVotes
                  FROM proposal_comments
                 WHERE proposalId = {};
                """
                #LIMIT 5;
        
        query = query.format(proposal_id)
        
        data = self.execute_mysql_query(query)
        
        if len(data):
            for row in data:
                cid, parent_id, page, user_id, user_type, date, time, text, num_votes, num_positive_votes, num_negative_votes = row
                item = {'parent_id': parent_id, 'page': page, 'user_id': user_id, 'user_type': user_type, 
                        'date': date, 'time': time, 'text': text.decode(encoding), 'num_votes': num_votes, 
                        'num_positive_votes': num_positive_votes, 'num_negative_votes': num_negative_votes}
                comment_list[cid] = item
            
        return comment_list
    
    # Create a MySQL Connection
    def create_mysql_connection(self, user_name:str, password:str, host_name:str, db_name:str):
        cnx = None
        
        try:
            config = {
                'user': user_name,
                'password': password,
                'host': host_name,
                'database': db_name,
                'raise_on_warnings': True
            }
            
            cnx = mysql.connector.connect(**config)
        
        except mysql.connector.Error as err:
            cnx = None
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                print("Something is wrong with your user name or password")
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                print("Database does not exist")
            else:
                print(err)
        
        return cnx
    
    # Execute a MySQL query
    def execute_mysql_query(self, query:str) -> list:
        dataset = []
        user_name = self.db_login['user_name']
        password = self.db_login['password']
        host_name = self.db_login['host_name']
        db_name = self.db_login['db_name']
        
        conn = self.create_mysql_connection(user_name, password, host_name, db_name)
        
        if conn != None:
            try:
                cursor = conn.cursor()
                cursor.execute(query)
                
                for row in cursor:
                    dataset.append(row)
                
            except:
                print('Error executing the query:', query)
                
            finally:
                cursor.close()
                conn.close()
        else:
            print('Failed connection')
            
        return dataset
