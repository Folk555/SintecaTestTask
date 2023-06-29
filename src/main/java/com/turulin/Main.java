package com.turulin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    //Пути откуда брать и куда сохранять файлы input.txt и output.txt
    private static String pathToInputFile = "/home/aturulin/IdeaProjects/SintecaTestTask/src/main/resources/input.txt";
    private static String pathToOutputFile = "/home/aturulin/IdeaProjects/SintecaTestTask/src/main/resources/output.txt";

    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        findSimilar(pathToInputFile);
    }

    public static String findSimilar(String inputFileName) throws Exception {

        //возможно надо указать ресурсную папку
        var firstItemList = new ArrayList<String>();
        var secondItemList = new ArrayList<String>();
        try (var bufferedReader = new BufferedReader(new FileReader(inputFileName))) {
            var sizeFirstItemList = Integer.parseInt(bufferedReader.readLine());
            for (int i = 0; i < sizeFirstItemList; ++i) {
                firstItemList.add(bufferedReader.readLine());
            }
            int sizeSecondItemList = Integer.parseInt(bufferedReader.readLine());
            for (int i = 0; i < sizeSecondItemList; ++i) {
                secondItemList.add(bufferedReader.readLine());
            }
        }

        var mapMatchIndexesLists = getIndexMapOfSimple(firstItemList, secondItemList);

        //Тут хватило бы одной HashMap, но тогда будет нарушен порядок следования записей из примеров по условию задачи
        ArrayList<Map<String, String>> stringItemMatch = new ArrayList<>();
        HashSet<Integer> unusedIndexFromSecondList = new HashSet<>();
        for (int i = 0; i < secondItemList.size(); i++)
            unusedIndexFromSecondList.add(i);

        for (int i = 0; i < firstItemList.size(); i++) {
            String key = firstItemList.get(i);
            String value = mapMatchIndexesLists.containsKey(i) ? secondItemList.get(mapMatchIndexesLists.get(i)) : "?";
            stringItemMatch.add(Map.of(key, value));
            unusedIndexFromSecondList.remove(mapMatchIndexesLists.get(i));
        }

        if (!(unusedIndexFromSecondList.isEmpty()))
            unusedIndexFromSecondList.forEach(x -> stringItemMatch.add(Map.of("?", secondItemList.get(x))));

        System.out.println(stringItemMatch);

        return saveMapToFile(stringItemMatch);
    }

    private static Map<Integer, Integer> getIndexMapOfSimple(List<String> firstItemList, List<String> secondItemList) {
        HashMap<Integer, Integer> indexMatchMap = new HashMap<>();
        ArrayList<SimilarityElement> allSimilarity = new ArrayList<>();
        for (int indexFirstList = 0; indexFirstList < firstItemList.size(); indexFirstList++) {
            for (int indexSecondList = 0; indexSecondList < secondItemList.size(); indexSecondList++) {
                double similarity =
                        Levenshtein.calculate(firstItemList.get(indexFirstList), secondItemList.get(indexSecondList));
                allSimilarity.add(new SimilarityElement(indexFirstList, indexSecondList, similarity));
            }
        }
        allSimilarity.sort(Comparator.comparing(SimilarityElement::getSimilarity));
        allSimilarity.forEach(System.out::println); //debug

        //Логично что если один список больше, а второй меньше то будет один свободный элемент
        //поэтому искать нужно столько пар сколько элементов в меньшем списке.
        //setNotMatchedElements отслеживает те элементы меньшего списка для которых пары еще нет.
        Set<Integer> setNotMatchedElements = new HashSet<>();
        boolean isFirstListSmaller;
        if (firstItemList.size() < secondItemList.size())
            isFirstListSmaller = true;
        else isFirstListSmaller = false;
        for (int i = 0; i < Math.min(firstItemList.size(), secondItemList.size()); ++i)
            setNotMatchedElements.add(i);
        for (int i = allSimilarity.size() - 1; i >= 0; --i) {
            SimilarityElement similarityElement = allSimilarity.get(i);
            if (isFirstListSmaller) {
                if (!(setNotMatchedElements.contains(similarityElement.x))) continue;
                setNotMatchedElements.remove(similarityElement.x);
            } else {
                if (!(setNotMatchedElements.contains(similarityElement.y))) continue;
                setNotMatchedElements.remove(similarityElement.y);
            }
            indexMatchMap.put(similarityElement.x, similarityElement.y);
        }
        System.out.println(indexMatchMap); //debug

        return indexMatchMap;
    }

    private static class SimilarityElement {
        public int x;
        public int y;
        public double similarity;

        public SimilarityElement(int x, int y, double similarity) {
            this.x = x;
            this.y = y;
            this.similarity = similarity;
        }

        public double getSimilarity() {
            return similarity;
        }

        @Override
        public String toString() {
            return "x: " + x + "  y: " + y + "  similarity: " + similarity;
        }
    }

    private static String saveMapToFile(List<Map<String, String>> results) throws Exception {
        Path pathToResultFile = Path.of(pathToOutputFile);
        if (Files.notExists(pathToResultFile))
            Files.createFile(pathToResultFile);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToResultFile.toString()))) {
            for (Map record : results) {
                record.forEach((key, value) -> {
                    try {
                        bufferedWriter.write(key.toString());
                        bufferedWriter.write(":");
                        bufferedWriter.write(value.toString());
                        bufferedWriter.write("\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            }
        }

        return pathToResultFile.toString();
    }

    //Алгоритм Левенштейна
    private static class Levenshtein {

        //Этот метод будет выдавать не расст ояние Левенштейна м/д 2ух строк а степень их похожести,
        //где 1.0 полное совпадение
        public static double calculate(String x, String y) {
            int maxLength = Math.max(x.length(), y.length());
            int[][] dp = new int[x.length() + 1][y.length() + 1];

            for (int i = 0; i <= x.length(); i++) {
                for (int j = 0; j <= y.length(); j++) {
                    if (i == 0) {
                        dp[i][j] = j;
                    } else if (j == 0) {
                        dp[i][j] = i;
                    } else {
                        dp[i][j] = min(dp[i - 1][j - 1]
                                        + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1);
                    }
                }
            }
            return ((maxLength - dp[x.length()][y.length()]) / (double) maxLength);
        }

        public static int costOfSubstitution(char a, char b) {
            return a == b ? 0 : 1;
        }

        public static int min(int... numbers) {
            return Arrays.stream(numbers)
                    .min().orElse(Integer.MAX_VALUE);
        }
    }
}