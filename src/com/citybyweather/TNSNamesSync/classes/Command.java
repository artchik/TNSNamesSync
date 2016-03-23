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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


//@SuppressWarnings("unused")
class Command {

    private String[] cmd;

    private Process p;
    private String outputPatternToMatch;
    //@SuppressWarnings("CanBeFinal")
    private List<String> resultingArray = new ArrayList<>();


    public Command() {  }

    //@SuppressWarnings("WeakerAccess")
    public Command(String cmd) {
        this.cmd = new String[] {
                "cmd.exe",
                "/c",
                cmd
        };
    }
    public Command(String[] cmd) {
        this.cmd = cmd;
    }

    public Command(String cmd, String outputPatternToMatch) {
        this(cmd);
        this.outputPatternToMatch = outputPatternToMatch;
    }

    public Command(String[] cmd, String outputPatternToMatch) {
        this(cmd);
        this.outputPatternToMatch = outputPatternToMatch;
    }

    public List<String> execute() throws InterruptedException, IOException {
        this.p = Runtime.getRuntime().exec(this.cmd);
        this.p.waitFor();
        this.parseOutput();

        return  this.resultingArray;
    }


    public void setCmd(String cmd) {
        this.cmd = new String[] {cmd};
    }

    public void setCommandStr(String[] commandStr) {
        this.cmd = commandStr ;
    }


    public List<String> getResultingArray() {
        return this.resultingArray;
    }

    public void setOutputPatternToMatch(String outputPatternToMatch) {
        this.outputPatternToMatch = outputPatternToMatch;
    }

    private void parseOutput() throws IOException {

        String commandOutputLine;

        BufferedReader reader = new BufferedReader(new InputStreamReader(this.p.getInputStream()));


        while ((commandOutputLine = reader.readLine()) != null) {

            commandOutputLine = commandOutputLine.trim();
            if (this.resultingArray.contains(commandOutputLine)) //exclude duplicates
                continue;

            if (this.outputPatternToMatch != null) { //if matching is defined, do matching
                if (Pattern.matches(outputPatternToMatch, commandOutputLine))
                    this.resultingArray.add(commandOutputLine);
            } else
                this.resultingArray.add(commandOutputLine);

        }
    }



}
