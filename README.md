# CE303_new

2024.11.04

1. download GSON library from
   https://mvnrepository.com/artifact/com.google.code.gson/gson
   
   Then Click on the latest version and then select files (jar)

2. for compiling:

# a) compile with proper directory structure (in root directory)
# Use -d out to specify the output directory for compiled files. 
# This should retain the package structure
# (like SOMSServerJava and SOMSClientJava folders) within the out directory.
   
   javac -cp "libs/gson-2.11.0.jar" -d out src/SOMSServerJava/*.java src/SOMSClientJava/*.java
   
3. run the program:
   
# run the server:
   java -cp "out:libs/gson-2.11.0.jar" SOMSServerJava.SOMS

# run the client:
   java -cp "out:libs/gson-2.11.0.jar" SOMSClientJava.ClientProgram
   
2024.11.12:

When running the seller's function, the buyer must be in the user's database. Otherwise, the seller's function will not work.