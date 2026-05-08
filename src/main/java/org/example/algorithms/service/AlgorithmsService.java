package org.example.algorithms.service;

import org.example.algorithms.exception.ValidationException;
import org.springframework.stereotype.Service;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AlgorithmsService {

    private static final int MAX_LIST_SIZE = 50;
    private static final int MIN_ELEMENT_VALUE = -100;
    private static final int MAX_ELEMENT_VALUE = 100;
    private static final Pattern LETTERS_ONLY = Pattern.compile("[a-zA-Z]+");
    private static final Pattern RANGE_TIME = Pattern.compile("^(1[0-2]|0?[1-9]):[0-5][0-9](AM|PM)-(1[0-2]|0?[1-9]):[0-5][0-9](AM|PM)$");
    private static final Pattern CONTAINS_LETTER = Pattern.compile("\\p{L}");
    private static final int MAX_TEXT_LENGTH = 10000;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
    private static final int MAX_RLE_TEXT_LENGTH = 1000;

    private static final Pattern PHONE_FORMAT = Pattern.compile("^(\\+48)?[0-9]{9}$");
    private static final Pattern PHONE_SEPARATORS = Pattern.compile("[-()\\s]");

    private static final int MAX_FACTORIAL_N = 5000;

    private static final int MAX_MAGIC_SQUARE_N = 100;

    public List<Integer> addSortedLists(List<Integer> list1, List<Integer> list2) {
        validateAddList("list1", list1);
        validateAddList("list2", list2);

        List<Integer> merged = new ArrayList<>(list1.size() + list2.size());
        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            if (list1.get(i) <= list2.get(j)) {
                merged.add(list1.get(i++));
            } else {
                merged.add(list2.get(j++));
            }
        }
        while (i < list1.size()) {
            merged.add(list1.get(i++));
        }
        while (j < list2.size()) {
            merged.add(list2.get(j++));
        }
        return merged;
    }

    // Best Time to Buy and Sell Stock
    // Given daily stock prices, returns the maximum profit from a single
    // buy-then-sell transaction (buy on day i, sell on day j > i).
    // Returns -1 if only a loss is possible
    public int maxProfit(Integer[] prices) {
        validatePrices("prices", prices);
        int maximumProfit = -1;
        int currentProfit;
        int minPriceSoFar = prices[0];

        for (int j = 0; j < prices.length - 1; j++) {
            if (prices[j] < minPriceSoFar) minPriceSoFar = prices[j];
            currentProfit = prices[j + 1] - minPriceSoFar;
            if (currentProfit > maximumProfit)  maximumProfit = currentProfit;

        }
        return maximumProfit;
    }


    // Run-Length Encoding (RLE) compression.
    // Compresses consecutive runs of the same character into character+count
    // pairs (e.g. "aaabbc" -> "a3b2c1").
    public String rleCompress(String text, boolean caseSensitive) {
        validateRleString("text", text);
        String inputText = text;
        if(!caseSensitive) inputText = text.toLowerCase();

        char[] decodedText = inputText.toCharArray();
        StringBuilder encodedText = new StringBuilder();


        int currentLetterCount = 1;

        for(int i = 0; i < decodedText.length - 1; i++) {
            if (decodedText[i] == decodedText[i+1]) {
                currentLetterCount++;
            }
            else {

                encodedText.append(currentLetterCount).append(decodedText[i]);
                currentLetterCount = 1;
            }
        }
        encodedText.append(currentLetterCount).append(decodedText[decodedText.length-1]);
        return encodedText.toString();
    }


    // Total minutes between two times on a 12-hour clock.
    // Input: a single string "H:MMam|pm-H:MMam|pm" (e.g. "9:00am-10:00am").
    // If the end time is on or before the start time, it wraps to the next day
    // (e.g. "1:00pm-11:00am" -> 1320 min = 22 h).
    public int minutesBetween(String timeRange) {
        validateNotNull("timeRange", timeRange);

        //It is important to use uppercase - Locale.ENGLISH only accepts AM/PM
        String timeRangeString = timeRange.toUpperCase().replace(" ", "");

        validateTimeRange("timeRange", timeRangeString);

        String[] timeRangeArray = timeRangeString.split("-");

        int minutesBegin = toMinutesOfDay(timeRangeArray[0]);
        int minutesEnd = toMinutesOfDay(timeRangeArray[1]);

        int minutesBetween = minutesEnd - minutesBegin;
        if(minutesBegin>minutesEnd) minutesBetween += 24 * 60;

        return minutesBetween;
    }

    private int toMinutesOfDay(String timeText) {
        LocalTime time = LocalTime.parse(timeText, TIME_FORMAT);
        return time.getHour() * 60 + time.getMinute();
    }


    /**
     * Returns the first word with the largest number of repeated letters.
     * <p>
     * Words are ranked by their letter-count distribution sorted in
     * descending order, compared lexicographically: the word with the
     * most frequent letter wins; ties are broken by the second-most
     * frequent letter, then the third, and so on. When distributions
     * are fully equal, the earliest word in the text wins.
     * <p>
     * Example: "greatest" (counts [2,2,1,1,1,1]) beats "ever"
     * (counts [2,1,1,1]) because they tie on the first position (2=2)
     * but differ on the second (2 > 1).
     *
     * @param text input text; words are separated by any non-letter
     *             characters (Unicode-aware)
     * @return the first word with the highest-ranked letter distribution,
     *         preserving original case.
     */
    public String mostRepeatedLetters(String text) {
        validateMostRepeatedLetters("text", text);
        String[] wordArray = text.split("\\P{L}+");

        String bestWord = null;
        int[] bestSortedCountArray = null;

        for (String word : wordArray) {
            if (word.isEmpty()) continue;
            HashMap<Character, Integer> characterMap = new HashMap<>();
            String currentWord = word.toLowerCase(Locale.ROOT);
            for (char c : currentWord.toCharArray()) {
                characterMap.merge(c, 1, Integer::sum);

            }

            int[] sortedCountArray = characterMap.values().stream().sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue).toArray();

            if(bestWord == null) {
                bestWord = word;
                bestSortedCountArray = sortedCountArray;
            }
            else {
                if (Arrays.compare(bestSortedCountArray, sortedCountArray) < 0) {
                    bestWord = word;
                    bestSortedCountArray = sortedCountArray;
                }
            }
        }
        return bestWord;
    }


    /**
     * Finds a pair of numbers whose sum equals the given number. None of the returned numbers may contain a 0
     * @param n; a number between 2 and 2,147,483,647
     * @return two numbers that do not contain the digit 0
     */
    public List<Integer> noZeroPair(Long n) {
        validateNoZeroPair("n", n);
        int target = n.intValue();
        int a = target/2;

        for(int i=1; i<=a; i++) {
            if (containsZeroDigit(i)) continue;
            int b = target - i;
            if (containsZeroDigit(b)) continue;
            return List.of(i, b);
        }
        throw new IllegalStateException("unreachable after validation");
    }

    private boolean containsZeroDigit(int number) {
        while(number >= 10) {
            if(number % 10 == 0) return true;
            number = number / 10;
        }
        return false;
    }

    public boolean isPhoneValid(String phone) {
        validateIsPhoneValid("phone", phone);
        String cleanPhone = PHONE_SEPARATORS.matcher(phone).replaceAll("");

        return PHONE_FORMAT.matcher(cleanPhone).matches();
    }

    public String factorial(Integer n) {
        validateFactorial("n", n);
        if (n <= 1) return "1";
        List<Integer> digits = new ArrayList<>();
        digits.add(1);
        for (int i = 2; i <= n; i++) {
            digits = multiply(digits, i);
        }
        StringBuilder result = new StringBuilder();
        for(int j = digits.size()-1; j >= 0; j--) {
            result.append(digits.get(j));
        }
        return result.toString();
    }


    private List<Integer> multiply(List<Integer> digits, int n) {
        int carry = 0;
        List<Integer> numbers = new ArrayList<>();
        for (int i=0; i<digits.size(); i++) {
            int product = digits.get(i) * n + carry;
            int remainder   = product % 10;
            numbers.add(remainder);
            carry = product / 10;

        }
        while(carry > 0) {
            int remainder   = carry % 10;
            numbers.add(remainder);
            carry = carry / 10;
        }

        return numbers;
    }

    public boolean isMagicSquare(Integer[][] matrix) {
        validateMagicSquare("matrix", matrix);
        int sum = 0;
        int currentSum;
        int diagonalSumLower = 0;
        int diagonalSumUpper = 0;
        int matrixLength = matrix.length;

        boolean[] seen = new boolean[matrixLength*matrixLength + 1];

        for(int row = 0; row < matrix.length; row++) {
            currentSum = 0;
            for(int col = 0; col < matrix[row].length; col++) {
                int value = matrix[row][col];

                if(value<1 || value>matrixLength*matrixLength) return false;
                // check if the value has already been used
                if (seen[value]) return false;
                // set the value in the seen array to true to avoid duplicates
                seen[value] = true;

                currentSum += value;
                if(row==col) diagonalSumLower += value;
                if(row==matrixLength-col-1) diagonalSumUpper += value;
            }
            if(sum == 0) sum = currentSum;
            if(sum!=currentSum) return false;
        }
        if(sum!=diagonalSumLower || sum!=diagonalSumUpper) return false;

        return true;
    }

    // VALIDATION
    private void validateAddList(String name, List<Integer> list) {
        validateNotNull(name, list);
        if (list.size() > MAX_LIST_SIZE) {
            throw new ValidationException(name + " exceeds maximum size of " + MAX_LIST_SIZE + " (got " + list.size() + ")");
        }
        for (int i = 0; i < list.size(); i++) {
            Integer curr = list.get(i);
            if (curr == null) {
                throw new ValidationException(name + " must not contain null elements");
            }
            if (curr < MIN_ELEMENT_VALUE || curr > MAX_ELEMENT_VALUE) {
                throw new ValidationException(name + " contains value out of range [" + MIN_ELEMENT_VALUE + ", " + MAX_ELEMENT_VALUE + "] at index " + i + " (got " + curr + ")");
            }
            if (i > 0) {
                Integer prev = list.get(i - 1);
                if (curr < prev) {
                    throw new ValidationException(name + " is not sorted in ascending order (violation at index " + i + ": " + prev + " > " + curr + ")");
                }
            }
        }
    }

    private void validatePrices(String name, Integer[] prices) {
        validateNotNull(name, prices);

        if (prices.length < 2) {
            throw new ValidationException(name + " must have at least 2 elements");
        }

        for (int i = 0; i < prices.length; i++) {
            Integer curr = prices[i];
            if (curr == null) {
                throw new ValidationException(name + " must not contain null elements");
            }
            if (curr < 0) {
                throw new ValidationException(name + " contains value less than 0 at index " + i + " (got " + curr + ")");
            }
        }
    }

    private void validateRleString(String name, String text) {
        validateNotNull(name, text);

        if (text.isEmpty()) {
            throw new ValidationException(name + " must be at least 1 character long");
        }

        if (text.length() > MAX_RLE_TEXT_LENGTH) {
            throw new ValidationException(name + " must be no longer than " + MAX_RLE_TEXT_LENGTH + " characters (got " + text.length() + ")");
        }


        if (!LETTERS_ONLY.matcher(text).matches()) {
            throw new ValidationException(name + " must contain only letters");
        }
    }

    private void validateNotNull(String name, Object value) {
        if (value == null) {
            throw new ValidationException(name + " must not be null");
        }
    }

    private void validateTimeRange(String name, String timeRange) {
        if (!RANGE_TIME.matcher(timeRange).matches()) {
            throw new ValidationException(name + " must follow the format h:mm(AM/PM)-h:mm(AM/PM)");
        }
    }

    private void validateMostRepeatedLetters(String name, String text) {
        validateNotNull(name, text);

        if (text.isEmpty()) {
            throw new ValidationException(name + " must be at least 1 character long");
        }

        if(text.length() > MAX_TEXT_LENGTH) {
            throw new ValidationException(name + " must be no longer than " + MAX_TEXT_LENGTH + " characters (got " + text.length() + ")");
        }

        if(!CONTAINS_LETTER.matcher(text).find()) {
            throw new ValidationException(name + " must contain at least one letter");
        }
    }

    private void validateNoZeroPair(String name, Long number) {
        validateNotNull(name, number);

        if (number <= 1) {
            throw new ValidationException(name + " must be greater than 1");
        }

        if (number > Integer.MAX_VALUE) {
            throw new ValidationException(name + " must be no greater than " + Integer.MAX_VALUE + " (got " + number + ")");
        }
    }

    private void validateIsPhoneValid(String name, String phone) {
        validateNotNull(name, phone);
    }

    private void validateFactorial(String name, Integer n) {
        validateNotNull(name, n);

        if (n < 0) {
            throw new ValidationException(name + " must be at least 0");
        }

        if (n>MAX_FACTORIAL_N) {
            throw new ValidationException(name + " must be no greater than " + MAX_FACTORIAL_N + " (got " + n + ")");
        }
    }

    private void validateMagicSquare(String name, Integer[][] matrix) {
        validateNotNull(name, matrix);

        int countRow = matrix.length;

        if (countRow == 0) {
            throw new ValidationException(name + " must not be empty");
        }

        if(countRow>MAX_MAGIC_SQUARE_N) {
            throw new ValidationException(name + " size must be no greater than " + MAX_MAGIC_SQUARE_N + " (got " + matrix.length + ")");
        }

        int countCol = matrix[0].length;

        if(countCol != countRow) {
            throw new ValidationException(name + " must have the same number of rows as columns");
        }

        for (int row = 0; row < matrix.length; row++) {
            if(matrix[row].length!=countCol) {
                throw new ValidationException(name + " must have " + countCol + " columns");
            }
            for (int col = 0; col < matrix[row].length; col++) {
                if(matrix[row][col]==null) {
                    throw new ValidationException(name + " must not contain null values in row : " + row + ", col : " + col);
                }
                if(matrix[row][col]<=0) {
                    throw new ValidationException(name + " must contain values greater than zero in row : " + row + ", col : " + col);
                }
            }
        }

    }
}
