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


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


//@SuppressWarnings("unused")
public class OracleHome {

	private String name;

	private List<String> paths = new ArrayList<>();
	@SuppressWarnings("CanBeFinal")
	private List<String> tnsPaths = new ArrayList<>();



	private static final String oracleTNSFileSubdir = "\\network\\admin\\";

	public OracleHome() {}

	public OracleHome(String name, String path) {
		this.name = name;
		this.addPath(path);
	}

	public OracleHome(String name, List<String> paths) {
		this.name = name;
		this.paths = paths;
		this.setTNSPaths();
	}

	private Boolean pathExists(String path) {
		return Files.exists(Paths.get(path));
	}



	public Boolean exists() {
	   return !this.paths.isEmpty();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPaths() {
		return this.paths;
	}

	private void setTNSPaths() {

		this.tnsPaths.clear();
		for (String path : this.paths)
			this.tnsPaths.add(path + oracleTNSFileSubdir);
	}



	public List<String> getTNSPaths() {
		return this.tnsPaths;
	}

	public void addPath(String path) {

		if (!this.paths.contains(path) && this.pathExists(path)) {
			this.paths.add(path);
			this.tnsPaths.add(path + oracleTNSFileSubdir);
		}
	}

	public String getPathsString() {

		StringBuilder sb = new StringBuilder();
		for (String s : this.paths)
		{
			sb.append("  [");
			sb.append(s);
			sb.append("]\n");
		}

		return sb.toString();
	}
}
