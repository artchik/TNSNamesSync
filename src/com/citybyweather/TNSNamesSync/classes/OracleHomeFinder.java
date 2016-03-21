/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Artur Charukhchyan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.citybyweather.TNSNamesSync.classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@SuppressWarnings("unused")
public class OracleHomeFinder {


    private static final String commandRegQuery = "reg query";
    private static final String oracleHomeRegValueParams = "/v ORACLE_HOME";

    private static final String oracleRegPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\ORACLE";
    private static final String oracleRegPath32 = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432NodeORACLE\\ORACLE";



    private static final String oracleRegKeyPattern1 = "^.*\\\\KEY_.*$";
    private static final String oracleRegKeyPattern2 = "^.*ORACLE_HOME.*$";

    private static final String oracleRegKeyPrefix = "KEY_";



    //@SuppressWarnings("CanBeFinal")
    private Map<String, OracleHome> oracleHomes = new HashMap<>();
    private List<String> oracleRegKeys;


    //START singleton declarations
    //@SuppressWarnings("CanBeFinal")
    private static OracleHomeFinder ourInstance = new OracleHomeFinder();

    //@SuppressWarnings("unused")
    public static OracleHomeFinder getInstance() {
        return ourInstance;
    }

    private OracleHomeFinder() {
    }
    //END singleton declarations



    //@SuppressWarnings("unused")
    public Map<String, OracleHome> find() throws Exception {

        OracleHome oh;
        String oracleHomeName;
        String oracleHomeValue;

        //find the oracle home registry keys
        this.oracleRegKeys = this.findOracleRegistryKeys();

        //for each of the keys, find the oracle home name and path
        for (String oracleRegKey : this.oracleRegKeys) {

            oracleHomeName = this.getOracleHomeNameFromRegKey(oracleRegKey);
            oracleHomeValue = this.getOracleHomePathFromRegKey(oracleRegKey);

            oh = new OracleHome(oracleHomeName, oracleHomeValue);
            if (oh.exists()) {
                this.oracleHomes.put(oracleHomeName, oh);
            }
        }

        //if no paths are valid, throw an exception
        //if (oracleHomes.isEmpty())
        //    throw new Exception("No valid oracle homes found");

        return this.oracleHomes;
    }




    private List<String>  findOracleRegistryKeys() throws IOException, InterruptedException {


        //this will return items like HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1
        //we're only interested if they have KEY_
        Command cmd1 = new Command(commandRegQuery + " " + oracleRegPath, oracleRegKeyPattern1);
        this.oracleRegKeys = cmd1.execute();

        //if we are on a 64 bit machine, and there are 32 bit Oracle installation, they are stored in the
        //oracleRegPath32 key, so let's get those too and merge
        Command cmd2 = new Command(commandRegQuery + " " + oracleRegPath32, oracleRegKeyPattern1);
        this.oracleRegKeys.addAll(cmd2.execute());

        return this.oracleRegKeys;
    }


    private String getOracleHomeNameFromRegKey(String registryKey) {

        /*
                let's get the actual ORACLE_HOME name, e.g.
                for HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1, we need OraClient11g_home1
                since split in Java accepts reg exprs only, we need 4 \ for one: see http://goo.gl/1VxIei
        */
        String oracleRegKeyPieces[] = registryKey.split("\\\\");
        String oracleHomeName = oracleRegKeyPieces[oracleRegKeyPieces.length - 1];
        return oracleHomeName.replaceAll(oracleRegKeyPrefix, "");
    }

    private String getOracleHomePathFromRegKey(String key) throws IOException, InterruptedException {

        Command cmd = new Command(commandRegQuery + " " +  key + " " + oracleHomeRegValueParams, oracleRegKeyPattern2);
        /*
                The command passed will return the following itself
                HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1
                ORACLE_HOME    REG_SZ    c:\oracle\product\11.2.0\client_1
        */

        List<String> output =  cmd.execute();
        String oracleHomeRegOutputString = output.get(0); //there shouldn't be more than one oracle home value

        /*
                oracleHomeRegOutputString has the following line:
                "ORACLE_HOME    REG_SZ    c:\oracle\product\11.2.0\client_1"
                We just need the last part with the path.
        */
        String[] regValuePieces =  oracleHomeRegOutputString.split("\\s+");

        return regValuePieces[regValuePieces.length - 1];
    }



}
