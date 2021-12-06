# ADF-Spark-Notebook-pipeline

This ADF Pipeline has a very simple structure. 
The aim of the pipeline is to read data from Blob storage, transform it and then store the transformed data into an RDBMS table for reporting.
Connection strings, container names and output table names have been parameterised. 
So almost any execution can be achieved on this notebook but the output table's DDL query needs to be parameterized such that it builds the table based on the output file.

![image](https://user-images.githubusercontent.com/20545570/144915166-941ddf08-d589-44a9-90d8-2b2526845120.png)
