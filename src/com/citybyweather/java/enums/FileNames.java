package com.citybyweather.java.enums;


import java.util.ArrayList;
import java.util.List;

public enum FileNames {

    TNSNAMES("tnsnames.ora"), SQLNET("sqlnet.ora");

    private String name;

    FileNames(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }


    public static List<String> listAll() {

        List<String> fileNames = new ArrayList<>();

        for (FileNames fileName : FileNames.values())
            fileNames.add(fileName.toString());

        return fileNames;
    }



}
