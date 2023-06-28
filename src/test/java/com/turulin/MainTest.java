package com.turulin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

class MainTest {

    @ParameterizedTest
    @CsvSource({"input1.txt,output1.txt", "input2.txt,output2.txt", "input3.txt,output3.txt"})
    public void file1(String inputFileName, String outputFileName) throws Exception {
        Map<String, String> actualMapOfSimple =
                Main.getMapOfSimple(MainTest.class.getClassLoader().getResource(inputFileName).toString());
        //Надо получить map из файла output, пока заглушка
        Map<String, String> expectedMapOfSimple = new HashMap<>();
        expectedMapOfSimple.put("манго", "киви");

        Assertions.assertEquals(expectedMapOfSimple, actualMapOfSimple);
    }

}