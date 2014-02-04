cd ..
cp -r bin/code .
start java code.client.BankClient 1 127.0.0.1 1141
start java code.client.BankClient 2 127.0.0.1 1142
start java code.client.BankClient 3 127.0.0.1 1143
start java code.client.BankClient 4 127.0.0.1 1144
start java code.client.BankClient 5 127.0.0.1 1145
start java code.client.BankClient 6 127.0.0.1 1146
start java code.client.BankClient 7 127.0.0.1 1147
start java code.server.BankServer 8 127.0.0.1 1148
start java code.server.BankServer 9 127.0.0.1 1149
sleep 1
start java code.server.BankServer 10 127.0.0.1 1150 40
cd scripts