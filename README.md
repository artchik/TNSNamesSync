# TNSNamesSync

TNSNameSync is a console application written in Java that allows you update **tnsnames.ora** and **sqlnet.ora** across multiple Oracle homes on a Windows computer.

  - Automatically finds all relevant Oracle Homes on the PC
  - Backs up existing **tnsnames.ora** and **sqlnet.ora**
  - Updates all of the **tnsnames.ora** and **sqlnet.ora** under all of the Oracle Homes






## Version
1.0

## Installation

There is no installation really. All you need to do is to download either the *executable TNSNamesSync.exe* or the *jar file (TNSNamesSync.jar)*. 
(Note that if you run the JAR file, it is compatible with Java 1.7 and up.)


## Use

1. Place the updated **tnsnames.ora** and/or **sqlnet.ora** files in the same directory as the executable (the program can update both or either one)
2. Run the executable

If you do not want to place the  **tnsnames.ora** and/or **sqlnet.ora** into the same folder as the executable, 
or you want to perhaps schedule to run this utility in the Windows Task Scheduler and have it pick up the refreshed files 
from a particular directory, you may use a command line passing it one parameter, which is the path to the files as follows:

```sh
C:\TNSNamesSync.exe path_to_tnsnames_and_sqlnet_files
```

*Note that you must use double quotes for the path if it includes spaces*

The program will always overwrite the existing files after making backups (see below). It will not prompt the user on whether s/he is sure
about the overwrite.


## Backups

The program automatically creates backups (if it finds any existing files) leaving them under the same directories (network\admin).
The backups are saved in the following format, *filename_current_timestamp.bak* (e.g. sqlnet.ora_2016-03-12_18-57-05.bak)

### Potential Issues

If you try to run the JAR file, and it won't open by simply double-clicking on it in Windows, you may need to open a command line
and run it manually as follows:

```sh
C:\java -jar TNSNamesSync.jar optional_path_to_files
```

*If you don't specify the optional_path_to_files, the program will be looking for them in the same directory where the executable resides. In the example above, it would be directly on C:\*


## Sample Output

```sh
Looking for files in:
 - C:\myfiles\

Found the following files in C:\myfiles\:
=====================================================
sqlnet.ora
- last modified 03/09/2016 09:10:36



Found the following Oracle Homes to update:
===========================================
OraClient11g_home1
- TNSNames location:  c:\oracle\product\11.2.0\client_1\network\admin




Backing up and copying:
===========================================
Creating a backup:
- c:\oracle\product\11.2.0\client_1\network\admin\sqlnet.ora_2016-03-12_23-02-29.bak
Backup created

Copying new sqlnet.ora to c:\oracle\product\11.2.0\client_1\network\admin
New File copied


Press Enter to close...
```


License
----

MIT


