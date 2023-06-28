package com.turulin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String args) {
        System.out.println("Hello world!");
    }

    public static String findSimilar(String inputFileName) throws URISyntaxException {

        return Paths.get(Main.class.getClassLoader().getResource("output.txt").toURI()).toString();
    }

    public static Map<String, String> getMapOfSimple(String filePath) throws Exception {
        return new HashMap<String, String>();
    }

    public static void saveMapToFile(Map<String, String> results) throws Exception {
        Path pathToResultFile = Paths.get(Main.class.getClassLoader().getResource("output.txt").toURI());
        if (Files.notExists(pathToResultFile))
            Files.createFile(pathToResultFile);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToResultFile.toString()))) {
            for (Map.Entry<String, String> entry : results.entrySet()) {
                bufferedWriter.write(entry.toString());
                bufferedWriter.write("\n");
            }

        }
    }
}