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
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


//@SuppressWarnings({"unused", "CanBeFinal"})
public class FileSetAtPath {

    private String pathNoName;
    //@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private HashMap<String, SimpleFile> files = new HashMap<>();
    private Integer numTriedToAdd = 0;


    public FileSetAtPath(String pathNoName) {
        this.pathNoName = pathNoName;

    }

    public HashMap<String, SimpleFile> getFiles() {
        return this.files;
    }


    public void add(String fileName) throws IOException, ParseException {

        SimpleFile sf = new SimpleFile(fileName, this.pathNoName);
        this.numTriedToAdd++;
        if (sf.exists())
            this.files.put(fileName, new SimpleFile(fileName, this.pathNoName));

    }

    public void remove(String fileName) {
        this.files.remove(fileName);
    }

    public Boolean anyExists() {
        return !this.files.isEmpty();
    }

    public Boolean allExist() {
        return this.numTriedToAdd == this.files.size();
    }


    /*
    public Boolean anyExists() {

        for (Map.Entry<String, SimpleFile> fileInSet : this.files.entrySet()) {
            if (fileInSet.getValue().getExists())
                return true;
        }

        return false;
    }
    */

    /*
    public Boolean allExist() {
        Boolean exist = true;

        for (Map.Entry<String, SimpleFile> fileInSet : this.files.entrySet()) {
            //noinspection ConstantConditions
            if (!(exist &= fileInSet.getValue().getExists()))
                break;
        }

        return exist;
    }
    */

    public void copy(String destinationPathNoName, Boolean backup) throws IOException {
        for (Map.Entry<String, SimpleFile> fileInSet : this.files.entrySet())
            fileInSet.getValue().copy(destinationPathNoName, backup);

    }





}
