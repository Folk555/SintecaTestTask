package com.turulin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class MainTest {

    @ParameterizedTest
    @CsvSource({"/home/aturulin/IdeaProjects/SintecaTestTask/src/test/resources/input1.txt,/home/aturulin/IdeaProjects/SintecaTestTask/src/test/resources/ouput1.txt",
            "/home/aturulin/IdeaProjects/SintecaTestTask/src/test/resources/input2.txt,/home/aturulin/IdeaProjects/SintecaTestTask/src/test/resources/ouput2.txt",
            "/home/aturulin/IdeaProjects/SintecaTestTask/src/test/resources/input3.txt,/home/aturulin/IdeaProjects/SintecaTestTask/src/test/resources/ouput3.txt"})
    public void file1(String inputFileName, String outputFileName) throws Exception {
        //File actualFile = new File(Main.findSimilar(inputFileName));
        Path act = Paths.get(Main.findSimilar(inputFileName));
        //File expectedFile = new File(outputFileName);
        Path exp = Paths.get(outputFileName);

        Assertions.assertEquals(-1L, Files.mismatch(exp, act));
    }

}