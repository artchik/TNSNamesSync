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
import java.util.*;

//@SuppressWarnings("unused")
public class OracleHomeFinder {


	private static final String regCmd32bitSwitch = "/reg:32";
	private static final String regCmd64bitSwitch = "/reg:64";

	private static final String cmdRegQuery = "reg query";
	private static final String oracleHomeRegValueParams = "/v ORACLE_HOME";

	private static final String oracleRegistryPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\ORACLE";
	//private static final String oracleRegPath32 = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\ORACLE";



	private static final String oracleRegKeyPattern1 = "^.*\\\\KEY_.*$";
	private static final String oracleRegKeyPattern2 = "^.*ORACLE_HOME.*$";

	private static final String oracleRegKeyPrefix = "KEY_";

	private Boolean is64bit = false;



	//@SuppressWarnings("CanBeFinal")
	private Map<String, OracleHome> oracleHomes = new HashMap<>();
	private List<String> oracleRegKeys = new ArrayList<>();


	//START singleton declarations
	//@SuppressWarnings("CanBeFinal")
	private static OracleHomeFinder ourInstance = new OracleHomeFinder();

	//@SuppressWarnings("unused")
	public static OracleHomeFinder getInstance() {
		return ourInstance;
	}

	private OracleHomeFinder() {

		//if it's a 64 bit system, we'll need both the 64 and 32 bit registry queries
		this.is64bit = this.isOS64Bit();

	}
	//END singleton declarations



	/**
	 * Courtesy of http://stackoverflow.com/questions/1856565/how-do-you-determine-32-or-64-bit-architecture-of-windows-using-java
	 * Determine if the OS 64 bit
	 * @return boolean true is 64 bit
	 */
	private Boolean isOS64Bit() {
		Boolean is64bit;

		if (System.getProperty("os.name").contains("Windows"))
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		else
			is64bit = (System.getProperty("os.arch").contains("64"));

		return is64bit;
	}


	//@SuppressWarnings("unused")
	public Map<String, OracleHome> find() throws Exception {

		OracleHome oh;
		String oracleHomeName;
		List<String> oracleHomeValues;

		//find the oracle home registry keys
		this.oracleRegKeys = this.findOracleRegistryKeys();

		//for each of the keys, find the oracle home name and path
		for (String oracleRegKey : this.oracleRegKeys) {

			oracleHomeName = this.getOracleHomeNameFromRegKey(oracleRegKey);
			oracleHomeValues = this.getOracleHomePathsFromRegKey(oracleRegKey);


			oh = new OracleHome(oracleHomeName, oracleHomeValues);
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
		List<String> cmdRes;

		Command cmd = new Command(this.parseCommand(cmdRegQuery + " " + oracleRegistryPath), oracleRegKeyPattern1);
		cmdRes = cmd.execute();
		if (!cmdRes.isEmpty())
			this.oracleRegKeys.addAll(cmdRes);


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

	private List<String> getOracleHomePathsFromRegKey(String key) throws IOException, InterruptedException {

		//sometimes the same oracle home can contain 2 dirs: one for 32 and one for 64 bit versions
		//so that's why we'll build a list
		List<String> homePaths = new ArrayList<>();

		/*
			The followingcommand passed will return the following itself, but with the pattern we passed, we'll get 2nd
			line only, which is what we want

			HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraClient11g_home1
			ORACLE_HOME    REG_SZ    c:\oracle\product\11.2.0\client_1
		*/
		Command cmd = new Command(this.parseCommand(cmdRegQuery + " " + key + " " + oracleHomeRegValueParams), oracleRegKeyPattern2);


		String[] regValuePieces;
		List<String> output = cmd.execute();
		for (String oracleHomeRegOutputString : output) {
			oracleHomeRegOutputString = oracleHomeRegOutputString.trim();
			if (oracleHomeRegOutputString.isEmpty())
				continue;

			/*
				oracleHomeRegOutputString has the following line:

				"ORACLE_HOME    REG_SZ    c:\oracle\product\11.2.0\client_1"

				We just need the last part with the path, so we'll split.
			*/
			regValuePieces =  oracleHomeRegOutputString.split("\\s+");
			homePaths.add(regValuePieces[regValuePieces.length - 1]);

		}

		return homePaths;
	}

	private String parseCommand(String cmdString) {

		if (this.is64bit)
			cmdString = cmdString + " " + regCmd32bitSwitch + " & " + cmdString + " " + regCmd64bitSwitch;

		return cmdString;

	}


}
