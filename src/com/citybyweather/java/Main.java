package com.citybyweather.java;

import com.citybyweather.java.enums.FileNames;
import com.citybyweather.java.exceptions.FilesNotFoundCurrentDirException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static final String commandRegQuery = "reg query";
    public static final String oracleRegPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\ORACLE";
    public static final String oracleHomeRegValueParams = "/v ORACLE_HOME";
    public static final String oracleTNSFileSubdir = "\\network\\admin";
    public static final String oracleRegKeyPattern1 = "^.*\\\\KEY_.*$";
    public static final String oracleRegKeyPattern2 = "^.*ORACLE_HOME.*$";


    public static void main(String[] args) {


        try {

            Map<FileNames, Path> fromPaths = getFilePathsToCopy(args);
            Map<String, String> oracleHomes = getOracleHomesToCopyInto();
            copyFiles(fromPaths, oracleHomes);





        } catch (FilesNotFoundCurrentDirException e) {

            System.out.println("Can't find the following files to copy:");
            System.out.println("(NOTE: they  must be in the same directory as this program)");

            System.out.println(e.getMessage());


        } catch (Exception e) {
            System.out.println(e.getMessage());

        } finally {
            System.out.println("Press Enter to close...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *
     * @param fromPaths hash map of the files to copy. Key is the file name, value is the instance of Path
     * @param oracleHomes hash map of the oracle home paths;
     *                    key is the oracle home name, and value is the path including network\admin
     * @throws IOException
     *
     * This function copies the files from the source locations to the destination
     */
    private static void copyFiles(Map<FileNames, Path> fromPaths, Map<String, String> oracleHomes) throws IOException {

        /*
                Ok, now we have the list of the oracle homes in the oracleHomes list/map;
                Now, let's go through each and copy
        */

        //now let's copy
        CopyOption[] copyOptions = new CopyOption[] {
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };


        //get the timestamp for file copying
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss" ).format(new Date());
        //System.out.println(timeStamp);

        String destinationPath, backupDestinationPath;
        System.out.println("Backing up and copying:");
        System.out.println("===========================================");
        for (Map.Entry<String, String> oracleHome : oracleHomes.entrySet()) {

            for (Map.Entry<FileNames, Path> fromPath : fromPaths.entrySet()) {
                //first create a backup
                destinationPath = oracleHome.getValue() + "\\" + fromPath.getKey();
                createBackup(copyOptions, timeStamp, destinationPath);

                System.out.println("Copying new " + fromPath.getKey() + " to " + oracleHome.getValue());
                Files.copy(fromPath.getValue(), Paths.get(destinationPath), copyOptions);
                System.out.println("New File copied\n\n");
            }

        }
    }

    /**
     *
     * @param copyOptions   CopyOptions are set in the copyFiles method
     * @param timeStamp     timestamp is also set in the copyFiles method. this is used for backup
     * @param destinationPath   This is the string containing the path of the file to back up
     * @throws IOException
     *
     * This method creates a backup in the same folder as the backuped file (destination) by adding _timestamp.bak
     */

    private static void createBackup(CopyOption[] copyOptions, String timeStamp, String destinationPath) throws IOException {
        String backupDestinationPath;//if the file already exists, creat a backup copy
        if (Files.exists(Paths.get(destinationPath))) {
            backupDestinationPath = destinationPath + "_" + timeStamp + ".bak";

            System.out.println("Creating a backup:");
            System.out.println("- " + backupDestinationPath);

            Files.copy(Paths.get(destinationPath), Paths.get(backupDestinationPath), copyOptions);

            System.out.println("Backup created\n");
        }
    }

    /**
     *
     * @return  returns the hash map of oracle homes; key is the oracle home name
     *          value is the path
     * @throws Exception
     */

    private static Map<String, String> getOracleHomesToCopyInto() throws Exception {
        //now we have the file paths; let's get the oracle homes

        //this will return items like HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1
        //we're only interested if they have KEY_
        List<String> oracleRegKeys = executeCommand(commandRegQuery + " " + oracleRegPath, oracleRegKeyPattern1);
        //so now we have the list of the all of the registry keys in this array
        //in the following format HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1

        //now let's go through each, and get the oracle home for it
        List<String> oracleHomeRegOutputStrings;
        Map<String, String> oracleHomes = new HashMap<>();
        String oracleHomePath;

        for (String oracleRegKey : oracleRegKeys) {
            /*
                let's get the actual ORACLE_HOME name, e.g.
                for HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1, we need OraClient11g_home1
                since split in Java accepts reg exprs only, we need 4 \ for one: see http://goo.gl/1VxIei
            */
            String oracleRegKeyPieces[] = oracleRegKey.split("\\\\");
            String oracleHomeName = oracleRegKeyPieces[oracleRegKeyPieces.length - 1];
            oracleHomeName = oracleHomeName.replaceAll("KEY_", "");

            oracleHomeRegOutputStrings = executeCommand(commandRegQuery + " " +  oracleRegKey + " " + oracleHomeRegValueParams, oracleRegKeyPattern2);
            /*
                The command passed will return the following itself
                HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1
                ORACLE_HOME    REG_SZ    c:\oracle\product\11.2.0\client_1

                we only need the second line, which is the "ORACLE_HOME" pattern attribute will accomplish

                So now we have an array list of items as follows, e.g.
                Item 1 : ORACLE_HOME    REG_SZ    c:\oracle\product\11.2.0\client_1
                Item 2 : ORACLE_HOME    REG_SZ    c:\oracle\product\10.1\client_3

                We need to split it, get just the last part for each

             */
            for (String oracleHomeRegOutputString : oracleHomeRegOutputStrings) {
                String[] regValuePieces =  oracleHomeRegOutputString.split("\\s+");
                /*
                    the last item would be what we need
                    we'll use the home name obtained above as the key, so we'll have something like this
                    key:    OraClient11g_home1
                    value:  c:\oracle\product\11.2.0\client_1

                    We'll add these to our oracleHomes maps ONLY if the path exists
                    (including the \network\admin part)
                */
                oracleHomePath = regValuePieces[regValuePieces.length - 1] + oracleTNSFileSubdir;

                if (Files.exists(Paths.get(oracleHomePath)))
                    oracleHomes.put(oracleHomeName, oracleHomePath);
            }
        }

        //if no paths are valid, throw an exception
        if (oracleHomes.isEmpty())
            throw new Exception("No valid oracle homes found");

        System.out.println("Found the following Oracle Homes to update:");
        System.out.println("===========================================");
        for (Map.Entry<String, String> oracleHome : oracleHomes.entrySet()) {
            System.out.print(oracleHome.getKey() + "\n- TNSNames location:  ");
            System.out.println(oracleHome.getValue());
            System.out.println("\n");
        }

        System.out.println("\n");



        return oracleHomes;
    }

    /**
     *
     * @param args  command line arguments. We only need to examine the first, which could be the path to files
     *              if it's not specified, it's assumed that the program will look in the current executable dir
     * @return      returns the hash map of file to copy. key is the FileName from the FileName enum,
     *              the value is the Path instance of the full path to the file
     * @throws ParseException
     * @throws IOException
     *
     * This function will examine the current executable dir or the dir passed in the args to try to find the tnsnames
     * and the sqlnet ora files. If none are found, an exception will be thrown. Otherwise, it will build
     * a hashmap and return it
     */
    private static Map<FileNames, Path> getFilePathsToCopy(String[] args) throws ParseException, IOException {
        //date conversion for file time modified
        DateFormat dateOutputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        DateFormat dateInputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateInputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateOutputFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));


        //if they passed an argument on the string, take the first one
        String currentDir = args.length != 0 ? args[0] : System.getProperty("user.dir");
        System.out.println("Looking for files in:\n - " + currentDir + "\n");

        Map<FileNames, Path> fromPaths = new HashMap<>();


        Path fromFilePath;
        //FileNames is an Enum - constants in other words
        for (FileNames fileName : FileNames.values()) {
            fromFilePath = Paths.get(currentDir, fileName.toString());
            if (Files.exists(fromFilePath))
                fromPaths.put(fileName, fromFilePath);
        }

        if (fromPaths.isEmpty())
            throw new FilesNotFoundCurrentDirException(FileNames.listAll());


        System.out.println("Found the following files in " + currentDir + ":");
        System.out.println("=====================================================");
        for (Map.Entry<FileNames, Path> fromPath : fromPaths.entrySet()) {
            System.out.print(fromPath.getKey() + "\n- last modified ");
            Date date = dateInputFormat.parse(Files.getLastModifiedTime(fromPath.getValue()).toString());
            System.out.println(dateOutputFormat.format(date));
            System.out.println("");
        }
        System.out.println("\n");
        return fromPaths;
    }


    /**
     *
     * @param commandString The command string to execute
     * @param outputPatternToMatch  Out of the returned lines of the command, which one do we want to grab and keep
     * @return  ArrayList containing all of the command lines that matched the outputPatternToMatch
     */
    public static List<String> executeCommand(String commandString, String outputPatternToMatch) throws Exception {

        Process p;
        String commandOutputLine;
        BufferedReader reader;
        List<String> resultingArray = new ArrayList<>();




        p = Runtime.getRuntime().exec(commandString);
        p.waitFor();
        reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        while ((commandOutputLine = reader.readLine()) != null) {

            commandOutputLine = commandOutputLine.trim();
            if (Pattern.matches(outputPatternToMatch, commandOutputLine)) {
                resultingArray.add(commandOutputLine);
            }
        }

        //System.out.println(resultingArray.isEmpty());

        return resultingArray;
    }




}


