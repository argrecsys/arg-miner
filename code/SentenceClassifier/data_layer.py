# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Version: 0.1.0
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
    
    # Create a MYSQL Connection
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
    
    # Execute MySQL query
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
