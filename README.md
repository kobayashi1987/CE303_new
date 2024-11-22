# CE303


Shop Order Management System (SOMS)

The Shop Order Management System (SOMS) is a Java-based client-server application designed to facilitate seamless transactions between customers and sellers. 
Customers can browse available items, make purchases, view their account balances, and manage their purchase histories. 
Sellers have the capability to add or update items, process transactions, and review transaction histories.

Prerequisites:
1. Java Development Kit (JDK) 8 or Higher: Ensure Java is installed on your system. It can be downloaded from Oracle’s official website.
2. a) GSON Library: SOMS utilizes GSON for JSON serialization/deserialization.
   b) Download the GSON library (e.g., gson-2.11.0.jar) from the Maven Repository.
   c) Place the gson-2.11.0.jar file inside the libs/ directory in your project root.

Project Structure:

SOMS_Project/         \
|---accounts.json     \
|---users.json        \
|---items.json        \ 
|--- purchases.json   \
|--- libs/             \
     |---gson-2.11.0.jar       \
|---src/                       \
|---SOMSServerJava/            \
       |---Account.java        \
       |--- ClientHandler.java   \
       |--- Item.java            \
       |--- Purchase.java        \
       |---SOMS.java            \
       |--- SOMSUtils.java          \
       |---User.java            \
|---SOMSClientJava/         \
        |---Client.java          \
        |---ClientProgram.java      \
|---out/                       
       |--- production/             \
       |---SOMSServerJava/             \
         |--- Account.class            \
         |--- ClientHandler.class      \
         |---Item.class             \
         |---Purchase.class            \
         |---SOMS.class             \
         |--- SOMSUtils.class       \
         |---User.class               \
      |---SOMSClientJava/           \
          |---Client.class          \
          |---ClientProgram.class      


2024.11.04

1. download GSON library from
   https://mvnrepository.com/artifact/com.google.code.gson/gson
   
   Then Click on the latest version and then select files (jar)

2. for compiling:

# a) compile with proper directory structure (in root directory)
# Use -d out to specify the output directory for compiled files. 
# This should retain the package structure
# (like SOMSServerJava and SOMSClientJava folders) within the out directory.

3. Compilation Steps:

   a) to compile the server and client files:
   
   javac -cp "libs/gson-2.11.0.jar" -d out src/SOMSServerJava/*.java src/SOMSClientJava/*.java
   
4. run the program:
   
# run the server:
   java -cp "out:libs/gson-2.11.0.jar" SOMSServerJava.SOMS

Expected Server Output:
```
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMSUtils loadUsers
INFO: Successfully loaded users from users.json
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMSUtils loadAccounts
INFO: Successfully loaded accounts from accounts.json
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMSUtils loadItems
INFO: Successfully loaded items from items.json
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMSUtils loadPurchases
INFO: Successfully loaded purchases from purchases.json
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMS setupLogger
INFO: Logger initialized.
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMS main
INFO: Server starting on port 12345
Nov 09, 2024 5:00:00 PM SOMSServerJava.SOMS main
INFO: Server started. Waiting for clients...
```



# run the client:
   java -cp "out:libs/gson-2.11.0.jar" SOMSClientJava.ClientProgram
   
Sample Client Interactions:
```
Welcome to SOMS Server!
Enter your userID: john_doe
Enter your password: 123
Authentication successful.
You are logged in as a Customer.
Available Items:
Item Name           Price($)  Quantity  
-------------------------------------------------
Item1               100.00    50        
Item2               150.00    30        
Item3               200.00    20        
Item4               250.00    15        
Item5               300.00    10        

Enter a command: view credits
Your current balance: $1000.00

Enter a command: buy Item1 2
Purchase of "Item1" x2 reserved for $200.00. Awaiting seller confirmation.

Enter a command: exit
Goodbye!
```


Logging

	•	Server Logs: Stored in soms.log in the project root directory.
	•	Client Logs: Displayed on the console for severe errors.

Data Files

	•	users.json: Contains user credentials and roles.
	•	accounts.json: Contains account balances and reserved funds.
	•	items.json: Contains item details available for purchase.
	•	purchases.json: Contains purchase transactions.



2024.11.12:

When running the seller's function, the buyer must be in the user's database. Otherwise, the seller's function will not work.


2024.11.20:

in items.json, make sure all is lowercase. Otherwise, the program will not work.