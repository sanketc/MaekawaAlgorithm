***********************************************************
Program: Distributed Banking System Demo: Maekawa Algorithm 
Author: Sanket Chandorkar
***********************************************************

About: 

Implementation of Maekawa's Quorum Based Mutual Exclusion Algorithm.

This project is Implementation of Paper:
M. Maekawa. A Root N Algorithm for Mutual Exclusion in Decentralized Systems. 
ACM Transactions on Computer Systems, pages 145{159, May 1985.


***********************************************************
STEPS TO RUN PROGRAM: (With Script)
***********************************************************

Run : scripts\run.bat

***********************************************************
STEPS TO RUN PROGRAM: (Without Script)
***********************************************************

1. Set the ConfigurationFile.txt and QuorumConfigurationFile.txt

2. LOGIN INTO netxx.utdallas.edu and RUN client commands as per ConfigurationFile.txt 
   NOTE: Run commands from low to high client ID.          
     > java code.client.BankClient 1 net01.utdallas.edu 1141
     > java code.client.BankClient 2 net02.utdallas.edu 1142
          ....
          
3. LOGIN INTO netxx.utdallas.edu and RUN server commands as per ConfigurationFile.txt
   NOTE: Run commands from low to high Server ID.
     > java code.server.BankServer 8 net08.utdallas.edu 1148
     > java code.server.BankServer 9 net09.utdallas.edu 1149
     > java code.server.BankServer 10 net10.utdallas.edu 1150 40
          
     NOTE: In the last command 40 is the number of iterations and this server
           acts as controller server.

4. Check output and log in "data" and "log" folders respectively.

***********************************************************
Known Issues
***********************************************************

The Program gets stuck as there in one condition in program thats is not handled as per the paper.
