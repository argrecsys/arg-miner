# -*- coding: utf-8 -*-
"""
    Created By: Andres Segura-Tinoco
    Created On: May 22, 2021
    Description: 
"""

# Import libraries
import mysql.connector
from mysql.connector import errorcode

# Create a MYSQL Connection
def create_mysql_connection(user_name:str, password:str, host_name:str, db_name:str):
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
def execute_query(query:str):
    dataset = []
    user_name = "root"
    password = "Ovs001993"
    host_name = "127.0.0.1"
    db_name = "decide.madrid_2019_09"
    
    conn = create_mysql_connection(user_name, password, host_name, db_name)
    
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
