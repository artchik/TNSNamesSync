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
import java.nio.file.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//@SuppressWarnings({"FieldCanBeLocal", "unused", "WeakerAccess"})
public class SimpleFile {

    private String name;
    private String pathNoName;
    private String fullPath;
    private Path pathAsPathType;
    private Boolean exists;
    private String lastModified;

    private final String DIR_SEPARATOR = "\\\\";
    private final String BACKUP_FILE_EXTENSION = ".bak";

    private final DateFormat dateOutputFormat;
    private final DateFormat dateInputFormat;

    private final CopyOption[] copyOptions;
    private String backupDestinationPath;
    private Boolean backupCreatedLastCopy = false;


    public SimpleFile() {

        this.exists = false;

        dateOutputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        dateInputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateInputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateOutputFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));


        copyOptions = new CopyOption[] {
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
    }


    public SimpleFile(String name, String pathNoName) throws IOException, ParseException {
        this();
        this.name = name;
        this.pathNoName = pathNoName;

        this.findAndCapture();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) throws Exception {
        this.name = name;
        if (this.pathNoName != null) {
            this.setFullPath(this.pathNoName + this.name);
            this.findAndCapture();
        }
    }

    public String getPathNoName() {
        return pathNoName;
    }

    public void setPathNoName(String pathNoName) throws Exception {


        if  (!pathNoName.substring(pathNoName.length() - 1).equals(DIR_SEPARATOR))
            pathNoName += DIR_SEPARATOR;


        this.pathNoName = pathNoName;

        if (this.name != null) {
           this.fullPath = this.pathNoName + this.name;
           this.findAndCapture();
        }
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) throws Exception {
        this.fullPath = fullPath;
        this.splitPathAndSet(this.fullPath);
        this.findAndCapture();
    }


    public Path getPathAsPathType() {
        return pathAsPathType;
    }

    public void setPathAsPathType(Path pathAsPathType) throws Exception {
        this.pathAsPathType = pathAsPathType;

        this.splitPathAndSet(this.pathAsPathType.toString());
    }

    public Boolean exists() {

        this.setExists();
        return exists;
    }

    private void setExists() {
        this.exists = Files.exists(this.pathAsPathType);
    }

    private void createPathType() {
        this.pathAsPathType = Paths.get(this.pathNoName, this.name);
    }

    private void splitPathAndSet(String path) throws Exception {

        String escapedDirSeparator = Pattern.quote(DIR_SEPARATOR);
        Pattern p = Pattern.compile("(.*" + escapedDirSeparator + ")([^" + escapedDirSeparator + "]+)$");
        Matcher m = p.matcher(path);
        if (!m.find())
            throw new Exception("Can't get the path figured out");

        this.pathNoName = m.group(1);
        this.name = m.group(2);

    }

    public String getLastModified() {
        return this.lastModified;
    }

    private void setLastModified() throws IOException, ParseException {

        Date date = dateInputFormat.parse(Files.getLastModifiedTime(this.pathAsPathType).toString());
        this.lastModified = dateOutputFormat.format(date);
    }

    private void findAndCapture() throws IOException, ParseException {
        this.createPathType();
        this.setExists();
        if (this.exists())
            this.setLastModified();

    }


    public void copy(String destinationPathNoName, Boolean createBackup) throws IOException {

        String fullDestinationPath = destinationPathNoName + this.name;

        if (createBackup)
            createBackup(destinationPathNoName);

        Files.copy(this.pathAsPathType, Paths.get(fullDestinationPath), copyOptions);
    }

    public Boolean getBackupCreatedLastCopy()
    {
        return this.backupCreatedLastCopy;
    }



    public void createBackup(String destinationPathNoName) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss" ).format(new Date());
        String fullDestinationPath = destinationPathNoName + this.name;

        //if the file already exists, create a backup copy
        if (Files.exists(Paths.get(fullDestinationPath))) {
            this.backupDestinationPath = fullDestinationPath + "_" + timeStamp + BACKUP_FILE_EXTENSION;
            Files.copy(Paths.get(fullDestinationPath), Paths.get(backupDestinationPath), this.copyOptions);

            this.backupCreatedLastCopy = true;
        } else
            this.backupCreatedLastCopy = false;

    }



    public String getBackupFullPath() {
        return this.backupDestinationPath;
    }



}
