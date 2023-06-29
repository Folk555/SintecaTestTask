package com.turulin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Перед запуском укажите путь к файлу input и output в переменные pathToInputFile и pathToOutputFile соответственно.
 *
 * @author Turulin
 */
public class Main {
    //Пути откуда брать и куда сохранять файлы input.txt и output.txt
    private static String pathToInputFile = "/home/aturulin/IdeaProjects/SintecaTestTask/src/main/resources/input.txt";
    private static String pathToOutputFile = "/home/aturulin/IdeaProjects/SintecaTestTask/src/main/resources/output.txt";

    public static void main(String[] args) throws Exception {
        findSimilar(pathToInputFile);
    }

    /**
     * Это основной метод. К сожалению через Stream я не знаю как сделать, да и по мне это не возможно, так как, чтобы
     * сопоставить строки, нужно сначала узнать их все, т.е. записать куда-то.
     * <p>
     * Логика программы:
     * Вначале мы читаем списки из файла и записываем в 2 разных ArrayList. Далее получаем мапу индексов тех элементов
     * которые совпали. Количество совпадений будет равно размеру самого малого списка. После создаем еще одну мапу,
     * но на этот раз не с парами индексов, а с соответствующими им строками из списков. Также добавляем в мапу те
     * строки которые остались без пары. Наконец сохраняем мапу в файл.
     * <p>
     * У самого малого списка не будет строк без пары из большего списка, так с точки зрения алгоритма все строки будут
     * сопоставлены, просто одни будут похожи друг на друга больше, другие меньше. Например, если будет 2 списка со
     * словами "слон"(в первом списке), "java"(во втором списке) они будут в паре.
     *
     * @param inputFileName - Путь к файлу
     * @return путь к сохраненному файлу, где лежет сопоставленные строки из разных списков
     * @throws Exception
     */
    public static String findSimilar(String inputFileName) throws Exception {
        //читаем списки из файла
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
        var mapMatchIndexesLists = getIndexMapOfMatch(firstItemList, secondItemList);

        //Тут хватило бы одной HashMap, но тогда будет нарушен порядок следования записей из примеров по условию задачи
        //stringItemMatch это конечная структура данных, вне хранятся непосредственно те записи, которая будет записываться в файл
        ArrayList<Map<String, String>> stringItemMatch = new ArrayList<>();
        //Если 2ой список будет больше первого, то unusedIndexFromSecondList отследит какие строки остались без пары
        HashSet<Integer> unusedIndexFromSecondList = new HashSet<>();
        for (int i = 0; i < secondItemList.size(); i++)
            unusedIndexFromSecondList.add(i);
        for (int i = 0; i < firstItemList.size(); i++) {
            String key = firstItemList.get(i);
            String value = mapMatchIndexesLists.containsKey(i) ? secondItemList.get(mapMatchIndexesLists.get(i)) : "?";
            stringItemMatch.add(Map.of(key, value));
            unusedIndexFromSecondList.remove(mapMatchIndexesLists.get(i));
        }
        //учитываем те строки из 2ого списка которые остались без пары
        if (!(unusedIndexFromSecondList.isEmpty()))
            unusedIndexFromSecondList.forEach(x -> stringItemMatch.add(Map.of(secondItemList.get(x), "?")));
        //System.out.println(stringItemMatch); //debug

        return saveMapToFile(stringItemMatch);
    }

    /**
     * Тут реализован сам алгоритм сопоставления пар. Берем запись первого листа и сравниваем ее с каждой записью
     * второго листа, результат записываем в третий лист, где хранятся данные "что с чем сравнили и какая похожесть".
     * Сравниваются на похожесть не целиком записи, а каждое их слово, после чего вычисляется среднеарифметическая
     * похожесть. Сделано это потому что не все тесткейсы проходили если сравнивать записи целиком.
     * <p>
     * Далее согласно отсортированному листу с похожестью формируются пары начиная с тех у кого похожесть наибольшая.
     *
     * @param firstItemList  - первый лист с записями для сопоставления с записями из второго листа
     * @param secondItemList - второй лист с записями для сопоставления с записями из первого листа
     * @return - мапа с индексами записей двух листов записи которых наиболее похожи.
     */
    private static Map<Integer, Integer> getIndexMapOfMatch(List<String> firstItemList, List<String> secondItemList) {
        //Заготовка для мапы которая будет хранить индексы соответствующих друг другу записей
        HashMap<Integer, Integer> indexMatchMap = new HashMap<>();
        //Вычисляем похожесть друг на друга абсолютно всех записей одного листа с другим (декартово произведение)
        ArrayList<SimilarityElement> allSimilarity = new ArrayList<>();
        for (int indexFirstList = 0; indexFirstList < firstItemList.size(); indexFirstList++) {
            for (int indexSecondList = 0; indexSecondList < secondItemList.size(); indexSecondList++) {
                //вычисляем среднее расстояние Левенштейна по всем словам записи одного листа относительно всех слов записи другого листа
                AtomicReference<Double> similarityCartesianSum = new AtomicReference<>((double) 0);
                String[] elementWordFirstList = firstItemList.get(indexFirstList).split(" ");
                String[] elementWordSecondList = secondItemList.get(indexSecondList).split(" ");
                Arrays.stream(elementWordFirstList).forEach(currentWordFirstList -> {
                    Arrays.stream(elementWordSecondList).forEach(currentWordSecondList -> {
                        double calculatedWordsDist = Levenshtein.calculate(currentWordFirstList, currentWordSecondList);
                        similarityCartesianSum.updateAndGet(v -> ((double) (v + calculatedWordsDist)));
//                        System.out.println("currentWordFirstList: "+currentWordFirstList+"\tcurrentWordSecondList: "+currentWordSecondList +
//                                "\tcalculatedWordsDist: "+calculatedWordsDist); //debug
                    });
                });
                double similarity = similarityCartesianSum.get() / (elementWordFirstList.length + elementWordSecondList.length);
                allSimilarity.add(new SimilarityElement(indexFirstList, indexSecondList, similarity));
            }
        }
        allSimilarity.sort(Comparator.comparing(SimilarityElement::getSimilarity));
        //allSimilarity.forEach(System.out::println); //debug

        //Составляем пары из отфильтрованного списка начинаем с тех у кого максиммльная похожесть.
        //Логично что если один список больше, а второй меньше то будет один свободный элемент
        //поэтому искать нужно строго столько пар, сколько элементов в меньшем списке.
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
        //System.out.println(indexMatchMap); //debug

        return indexMatchMap;
    }

    /**
     * Служебный класс чтобы было проще хранить и обрабатывать значения индексов сравниваемых записей и
     * степень их похожести.
     */
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

    /**
     * Алгоритм Лефенштейна. Алгоритм вычисляет "дистанцию редактирования" для двух строк. Иными словами вычисляет
     * сколько нужно совершить действий (вставка, удаление, замена буквы), чтобы одна строка стала похожа на другую.
     */
    private static class Levenshtein {

        /**
         * Внимание, этот метод возвращает не расстояние Левенштейна м/д 2ух строк, а степень их похожести.
         * Т.е. 1.0 строки идентичны, 0.0 нет ни одного совпадающего символа.
         */
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