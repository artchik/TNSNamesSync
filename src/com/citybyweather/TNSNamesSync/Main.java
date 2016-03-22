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

package com.citybyweather.TNSNamesSync;

import com.citybyweather.TNSNamesSync.classes.FileSetAtPath;
import com.citybyweather.TNSNamesSync.classes.OracleHome;
import com.citybyweather.TNSNamesSync.classes.SimpleFile;
import com.citybyweather.TNSNamesSync.enums.FileNames;
import com.citybyweather.TNSNamesSync.exceptions.FilesNotFoundCurrentDirException;
import com.citybyweather.TNSNamesSync.classes.OracleHomeFinder;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;


public class Main {


	public static void main(String[] args) {

		//  TODO: Accept argument on whether to log or not.
		// If yes, create log file in current dir using local comp name and timestamp; output same as sout
		// See http://www.codeproject.com/Tips/315892/A-quick-and-easy-way-to-direct-Java-System-out-to
		try {

			FileSetAtPath newFiles = getFilePathsToCopy(args);
			Map<String, OracleHome> oracleHomes = getOracleHomesToCopyInto();
			copyFiles(newFiles, oracleHomes);

		} catch (FilesNotFoundCurrentDirException e) {

			System.out.println("Can't find the following files to copy:");
			System.out.println("(NOTE: they  must be in the same directory as this program)");

			System.out.println(e.getMessage());


		} catch (Exception e) {
			System.out.println(e.getMessage());

		} finally {
			System.out.println("COMPLETE: Press Enter to close...");
			try {
			  //@SuppressWarnings("UnusedAssignment")
				//noinspection ResultOfMethodCallIgnored
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 *
	 * @param args  command line arguments. We only need to examine the first, which could be the path to files
	 *              if it's not specified, it's assumed that the program will look in the current executable dir
	 * @return      returns FileSetAtPath of the files to copy
	 * @throws ParseException
	 * @throws IOException
	 *
	 * This function will examine the current executable dir or the dir passed in the args to try to find the tnsnames
	 * and the sqlnet ora files. If none are found, an exception will be thrown. Otherwise, it will build
	 * a FileSetAtPath and return it
	 */
	private static FileSetAtPath getFilePathsToCopy(String[] args) throws ParseException, IOException {

		//if they passed an argument on the string, take the first one
		String pathNoNames = args.length != 0 ? args[0] : System.getProperty("user.dir");
		FileSetAtPath fileSet = new FileSetAtPath(pathNoNames);

		//FileNames is an Enum - constants in other words
		for (FileNames fileName : FileNames.values())
			fileSet.add(fileName.toString());

		if (!fileSet.anyExists())
			throw new FilesNotFoundCurrentDirException(FileNames.listAll());


		System.out.println("Files to copy from [" + pathNoNames + "]:");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
		for (Map.Entry<String, SimpleFile> file : fileSet.getFiles().entrySet()) {
			System.out.print("- " + file.getValue().getName());
			System.out.println("[" + file.getValue().getLastModified() + "]");
		}
		System.out.println("\n");
		return fileSet;
	}


	/**
	 *
	 * @return  returns the hash map of oracle homes; key is the oracle home name
	 *          value is the path
	 * @throws Exception
	 */

	private static Map<String, OracleHome> getOracleHomesToCopyInto() throws Exception {


		//find all Oracle homes
		OracleHomeFinder ohf = OracleHomeFinder.getInstance();
		Map<String, OracleHome> oracleHomes = ohf.find();

		//if no paths are valid, throw an exception
		if (oracleHomes.isEmpty())
			throw new Exception("No valid oracle homes found");

		System.out.println("Oracle Homes to process:");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
		for (Map.Entry<String, OracleHome> oracleHome : oracleHomes.entrySet())
			System.out.println("- " + oracleHome.getValue().getName() + " [" + oracleHome.getValue().getPath() + "]");
		System.out.println("");

		return oracleHomes;
	}





	/**
	 *
	 * @param files hash map of the files to copy. Key is the file name, value is the instance of Path
	 * @param oracleHomes hash map of the oracle home paths;
	 *                    key is the oracle home name, and value is the path including network\admin
	 * @throws IOException
	 *
	 * This function copies the files from the source locations to the destination
	 */
	private static void copyFiles(FileSetAtPath files, Map<String, OracleHome> oracleHomes) throws IOException {



		for (Map.Entry<String, OracleHome> oracleHome : oracleHomes.entrySet()) {

			System.out.println("Processing Oracle Home [" + oracleHome.getValue().getName() + "]");
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++");

			for (Map.Entry<String, SimpleFile> file : files.getFiles().entrySet()) {
				file.getValue().copy(oracleHome.getValue().getTNSPath(), true);

				System.out.print("- file copied:");
				System.out.println(" [" + file.getValue().getName() + "] to [" + oracleHome.getValue().getTNSPath() + "]");
				System.out.print("- backup created:");
				System.out.println(" [" + file.getValue().getBackupFullPath() + "]");
				System.out.println("");

			}

			System.out.println("");
		}
	}



}


