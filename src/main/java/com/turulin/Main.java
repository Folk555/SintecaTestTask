package com.turulin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static String pathToInputFile = "/home/aturulin/IdeaProjects/SintecaTestTask/src/main/resources/input.txt";
    private static String pathToOutputFile = "/home/aturulin/IdeaProjects/SintecaTestTask/src/main/resources/output.txt";
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        findSimilar("input.txt");
    }

    public static String findSimilar(String inputFileName) throws Exception {

        //возможно надо указать ресурсную папку
        var firstItemList = new ArrayList<String>();
        var secondItemList = new ArrayList<String>();
        try (var bufferedReader = new BufferedReader(new FileReader(pathToInputFile))) {
            var sizeFirstItemList = Integer.parseInt(bufferedReader.readLine());
            for (int i = 0; i < sizeFirstItemList; ++i) {
                firstItemList.add(bufferedReader.readLine());
            }
            int sizeSecondItemList = Integer.parseInt(bufferedReader.readLine());
            for (int i = 0; i < sizeSecondItemList; ++i) {
                secondItemList.add(bufferedReader.readLine());
            }
        }

        //var mapFirstSecondItemListMatching = getMapOfSimple();

        return saveMapToFile(new HashMap<>()).toString();
    }

    public static Map<String, String> getMapOfSimple(String filePath) throws Exception {
        return new HashMap<String, String>();
    }

    public static Path saveMapToFile(Map<String, String> results) throws Exception {
        Path pathToResultFile = Path.of(pathToOutputFile);
        if (Files.notExists(pathToResultFile))
            Files.createFile(pathToResultFile);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToResultFile.toString()))) {
            for (Map.Entry<String, String> entry : results.entrySet()) {
                bufferedWriter.write(entry.toString());
                bufferedWriter.write("\n");
            }
        }

        return pathToResultFile;
    }
}