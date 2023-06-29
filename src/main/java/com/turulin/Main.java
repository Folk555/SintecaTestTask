package com.turulin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    //Пути откуда брать и куда сохранять файлы input.txt и output.txt
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

        var mapFirstSecondItemListMatching = getMapOfSimple(firstItemList, secondItemList);

        return saveMapToFile(new HashMap<>()).toString();
    }

    public static Map<String, String> getMapOfSimple(List<String> firstItemList, List<String> secondItemList) {
        //Содержимое элемента List это индекс элемента secondItemList, который больше всего подходит к текущему
        // элементу из firstItemList
        ArrayList allSimilarity = new ArrayList<SimilarityElement>();
        //Декартово произведение всех комбинаций из первого и второго списка + степень их похожести
        var cartesianProduct = new double[firstItemList.size()][secondItemList.size()];
        for (int indexFirstList = 0; indexFirstList < firstItemList.size(); indexFirstList++) {
            for (int indexSecondList = 0; indexSecondList < secondItemList.size(); indexSecondList++) {
                double similarity =
                        Levenshtein.calculate(firstItemList.get(indexFirstList), secondItemList.get(indexSecondList));
                cartesianProduct[indexFirstList][indexSecondList] = similarity;
                allSimilarity.add(new SimilarityElement(indexFirstList, indexSecondList, similarity));
            }
        }

        allSimilarity.sort(Comparator.comparing(SimilarityElement::getSimilarity));
        allSimilarity.forEach(System.out::println);

        return new HashMap<String, String>();
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
            return "x: "+x+"  y: "+y+"  similarity: "+similarity;
        }
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