package com.citybyweather.java.exceptions;

import java.io.FileNotFoundException;
import java.util.List;


public class FilesNotFoundCurrentDirException extends FileNotFoundException {

    public static final long serialVersionUID = 42L;
    public List<String> fileNames;

    public FilesNotFoundCurrentDirException(List<String> fileNames) {

        super("Files Not Found");
        this.fileNames = fileNames;
    }


    @Override
    public String getMessage() {

        String errorMessage = "";


        for (String fileName : this.fileNames)
            errorMessage += "- " + fileName + "\n";

        return errorMessage;
    }


}
