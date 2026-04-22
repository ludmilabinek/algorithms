package org.example.algorithms.service;

import org.example.algorithms.exception.ValidationException;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AlgorithmsService {

    private static final int MAX_LIST_SIZE = 50;
    private static final int MIN_ELEMENT_VALUE = -100;
    private static final int MAX_ELEMENT_VALUE = 100;
    private static final Pattern LETTERS_ONLY = Pattern.compile("[a-zA-Z]+");
    private static final int MAX_RLE_TEXT_LENGTH = 1000;

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


    // VALIDATION
    private void validateAddList(String name, List<Integer> list) {
        if (list == null) {
            throw new ValidationException(name + " must not be null");
        }
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
        if (prices == null) {
            throw new ValidationException(name + " must not be null");
        }

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
        if (text == null) {
            throw new ValidationException(name + " must not be null");
        }

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
}
