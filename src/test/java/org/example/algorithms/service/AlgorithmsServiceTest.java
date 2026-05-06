package org.example.algorithms.service;

import org.example.algorithms.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlgorithmsServiceTest {

    // Pure POJO under test - no Spring context required for unit tests.
    private final AlgorithmsService service = new AlgorithmsService();

    @Nested
    @DisplayName("addSortedLists - happy path")
    class HappyPathAddSortedLists {

        @Test
        @DisplayName("merges two non-empty sorted lists into one sorted list")
        void mergesTwoNonEmptySortedLists() {
            // given: two pre-sorted lists with overlapping ranges
            List<Integer> list1 = List.of(-100, -70, -5, 1, 2, 4, 10);
            List<Integer> list2 = List.of(-88, -13, -5, -1, 1, 3, 4);

            // when
            List<Integer> result = service.addSortedLists(list1, list2);

            // then: merged list contains all elements from both inputs in ascending order
            assertThat(result)
                    .containsExactly(-100, -88, -70, -13, -5, -5, -1, 1, 1, 2, 3, 4, 4, 10)
                    .hasSize(list1.size() + list2.size());
        }

        @Test
        @DisplayName("returns copy of list2 when list1 is empty")
        void returnsList2WhenList1Empty() {
            List<Integer> list2 = List.of(-5, 0, 7);

            List<Integer> result = service.addSortedLists(List.of(), list2);

            assertThat(result).containsExactlyElementsOf(list2);
        }

        @Test
        @DisplayName("returns copy of list1 when list2 is empty")
        void returnsList1WhenList2Empty() {
            List<Integer> list1 = List.of(-5, 0, 7);

            List<Integer> result = service.addSortedLists(list1, List.of());

            assertThat(result).containsExactlyElementsOf(list1);
        }

        @Test
        @DisplayName("returns empty list when both inputs are empty")
        void returnsEmptyWhenBothEmpty() {
            List<Integer> result = service.addSortedLists(List.of(), List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("preserves duplicates present in both input lists")
        void preservesDuplicatesAcrossLists() {
            // given: the value 2 appears in both lists
            List<Integer> list1 = List.of(1, 2, 2, 3);
            List<Integer> list2 = List.of(2, 2, 4);

            List<Integer> result = service.addSortedLists(list1, list2);

            // then: every duplicate is kept in the merged output
            assertThat(result).containsExactly(1, 2, 2, 2, 2, 3, 4);
        }

        @Test
        @DisplayName("accepts boundary values -100 and 100 (inclusive range)")
        void acceptsBoundaryValues() {
            // given: lists containing the inclusive boundaries of the allowed range
            List<Integer> list1 = List.of(-100, 0, 100);
            List<Integer> list2 = List.of(-100, 50, 100);

            List<Integer> result = service.addSortedLists(list1, list2);

            assertThat(result).containsExactly(-100, -100, 0, 50, 100, 100);
        }

        @Test
        @DisplayName("accepts maximum-size lists (50 elements each)")
        void acceptsMaxSizeLists() {
            // given: two sorted lists of exactly 50 elements, each within the allowed range
            List<Integer> list1 = new ArrayList<>();
            List<Integer> list2 = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                list1.add(-50 + i); // -50..-1
                list2.add(i);       // 0..49
            }

            List<Integer> result = service.addSortedLists(list1, list2);

            // then: result has all 100 elements and is sorted
            assertThat(result).hasSize(100).isSorted();
        }

        @Test
        @DisplayName("accepts single-element lists")
        void acceptsSingleElementLists() {
            List<Integer> result = service.addSortedLists(List.of(5), List.of(-3));

            assertThat(result).containsExactly(-3, 5);
        }

        @Test
        @DisplayName("does not mutate the input lists")
        void doesNotMutateInputs() {
            // given: mutable inputs so mutation would be observable
            List<Integer> list1 = new ArrayList<>(List.of(1, 3, 5));
            List<Integer> list2 = new ArrayList<>(List.of(2, 4, 6));
            List<Integer> snapshot1 = List.copyOf(list1);
            List<Integer> snapshot2 = List.copyOf(list2);

            service.addSortedLists(list1, list2);

            // then: both inputs remain equal to their pre-call snapshots
            assertThat(list1).isEqualTo(snapshot1);
            assertThat(list2).isEqualTo(snapshot2);
        }
    }

    @Nested
    @DisplayName("addSortedLists - null validation")
    class NullValidationAddSortedLists {

        @Test
        @DisplayName("throws when list1 is null")
        void throwsWhenList1Null() {
            assertThatThrownBy(() -> service.addSortedLists(null, List.of(1, 2)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list1 must not be null");
        }

        @Test
        @DisplayName("throws when list2 is null")
        void throwsWhenList2Null() {
            assertThatThrownBy(() -> service.addSortedLists(List.of(1, 2), null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list2 must not be null");
        }

        @Test
        @DisplayName("throws when list1 contains a null element")
        void throwsWhenList1ContainsNullElement() {
            // Arrays.asList allows nulls; List.of would reject them at construction.
            List<Integer> list1 = Arrays.asList(1, null, 3);

            assertThatThrownBy(() -> service.addSortedLists(list1, List.of()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list1 must not contain null elements");
        }
    }

    @Nested
    @DisplayName("addSortedLists - size validation")
    class SizeValidationAddSortedLists {

        @Test
        @DisplayName("throws when list1 has 51 elements")
        void throwsWhenList1ExceedsMaxSize() {
            // given: a sorted, in-range list that is just one element over the limit
            List<Integer> list1 = new ArrayList<>();
            for (int i = 0; i < 51; i++) {
                list1.add(-50 + i); // stays within [-100, 100], sorted
            }

            assertThatThrownBy(() -> service.addSortedLists(list1, List.of()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list1 exceeds maximum size of 50 (got 51)");
        }

        @Test
        @DisplayName("throws when list2 has 73 elements and reports the actual size")
        void throwsWhenList2ExceedsMaxSize() {
            // given: 73 sorted, in-range elements - verifies the "(got N)" suffix is dynamic
            List<Integer> list2 = new ArrayList<>();
            for (int i = 0; i < 73; i++) {
                list2.add(-36 + i); // -36..36, all within [-100, 100], sorted
            }

            assertThatThrownBy(() -> service.addSortedLists(List.of(), list2))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list2 exceeds maximum size of 50 (got 73)");
        }
    }

    @Nested
    @DisplayName("addSortedLists - order validation")
    class OrderValidationAddSortedLists {

        @Test
        @DisplayName("throws when list1 is not sorted ascending and reports the violation")
        void throwsWhenList1NotSorted() {
            // given: list1 breaks the ordering at index 2 (5 > 3)
            assertThatThrownBy(() -> service.addSortedLists(List.of(1, 5, 3), List.of()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list1 is not sorted in ascending order (violation at index 2: 5 > 3)");
        }

        @Test
        @DisplayName("throws when list2 is not sorted ascending and reports the violation")
        void throwsWhenList2NotSorted() {
            assertThatThrownBy(() -> service.addSortedLists(List.of(), List.of(10, 2)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list2 is not sorted in ascending order (violation at index 1: 10 > 2)");
        }
    }

    @Nested
    @DisplayName("addSortedLists - range validation")
    class RangeValidationAddSortedLists {

        @Test
        @DisplayName("throws when list1 contains a value above the upper bound")
        void throwsWhenList1AboveUpperBound() {
            // given: 150 is above the inclusive upper bound of 100
            assertThatThrownBy(() -> service.addSortedLists(List.of(1, 50, 150), List.of()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list1 contains value out of range [-100, 100] at index 2 (got 150)");
        }

        @Test
        @DisplayName("throws when list2 contains a value below the lower bound")
        void throwsWhenList2BelowLowerBound() {
            // given: -200 is below the inclusive lower bound of -100
            assertThatThrownBy(() -> service.addSortedLists(List.of(), List.of(-200, -50)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list2 contains value out of range [-100, 100] at index 0 (got -200)");
        }

        @Test
        @DisplayName("throws when a boundary is exceeded by one (101 is out of range)")
        void throwsJustAboveUpperBound() {
            // given: 101 is one past the upper bound - verifies the bound is inclusive
            assertThatThrownBy(() -> service.addSortedLists(List.of(101), List.of()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("list1 contains value out of range [-100, 100] at index 0 (got 101)");
        }
    }

    @Nested
    @DisplayName("maxProfit - happy path")
    class HappyPathMaxProfit  {

        @ParameterizedTest
        @MethodSource("profitCases")
        void findsMaxProfitFromArray(Integer[] prices, int expected) {
            assertThat(service.maxProfit(prices)).isEqualTo(expected);
        }

        static Stream<Arguments> profitCases() {
            return Stream.of(
                    Arguments.of(new Integer[]{7, 1, 9, 4, 3, 6}, 8),
                    Arguments.of(new Integer[]{1, 2, 3, 4, 5, 6}, 5),
                    Arguments.of(new Integer[]{1, 7}, 6)
            );
        }


        @Test
        @DisplayName("returns 0 if there is neither a profit nor a loss")
        void returnsZeroWhenNoProfitAndNoLoss() {
            Integer[] prices = {7, 7, 5, 1};

            // when
            int result = service.maxProfit(prices);

            // then: is equal 0
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("returns -1 if only loss is possible")
        void returnsMinusOneWhenOnlyLoss() {
            Integer[] prices = {10, 7, 6, 4, 2, 1};

            // when
            int result = service.maxProfit(prices);

            // then: is equal -1
            assertThat(result).isEqualTo(-1);
        }

        @Test
        @DisplayName("does not mutate the input array")
        void doesNotMutateInputArray() {
            Integer[] prices = {7, 1, 5, 3, 6, 4};
            Integer[] snapshot = prices.clone();

            service.maxProfit(prices);

            assertThat(prices).containsExactly(snapshot);
        }
    }

    @Nested
    @DisplayName("maxProfit - null validation")
    class NullValidationMaxProfit {

        @Test
        @DisplayName("throws when array is null")
        void throwsWhenArrayIsNull() {
            Integer[] prices = null;
            assertThatThrownBy(() -> service.maxProfit(prices))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("prices must not be null");
        }

        @Test
        @DisplayName("throws when array contains a null value")
        void throwsWhenArrayContainsNullValue() {
            Integer[] prices = {5, 3, null, 7};
            assertThatThrownBy(() -> service.maxProfit(prices))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("prices must not contain null elements");
        }
    }

    @Nested
    @DisplayName("maxProfit - size validation")
    class SizeValidationMaxProfit {

        @ParameterizedTest
        @MethodSource("lessThanTwoCases")
        @DisplayName("throws when array has less than 2 elements")
        void throwsWhenArrayHasLessThanTwoElements(Integer[] prices) {
            assertThatThrownBy(() -> service.maxProfit(prices))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("prices must have at least 2 elements");
        }

        static Stream<Arguments> lessThanTwoCases() {
            return Stream.of(
                    Arguments.of((Object) new Integer[]{7}),
                    Arguments.of((Object) new Integer[]{})
            );
        }
    }

    @Nested
    @DisplayName("maxProfit - range validation")
    class RangeValidationMaxProfit {

        @Test
        @DisplayName("throws when array contains a negative value")
        void throwsWhenArrayContainsNegativeValue() {
            Integer[] prices = {5, 3, -7, 5};
            assertThatThrownBy(() -> service.maxProfit(prices))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("prices contains value less than 0 at index 2 (got -7)");
        }
    }

    @Nested
    @DisplayName("rleCompress - happy path")
    class HappyPathRleCompress {

        @ParameterizedTest
        @MethodSource("rleCases")
        @DisplayName("Compression using the RLE algorithm")
        void compressionUsingRleAlgorithm(String text, Boolean caseSensitive,  String expected) {
            assertThat(service.rleCompress(text, caseSensitive)).isEqualTo(expected);
        }

        static Stream<Arguments> rleCases() {
            return Stream.of(
                    Arguments.of("a", true,  "1a"),
                    Arguments.of("A", true,  "1A"),
                    Arguments.of("aaaAAbbbbBc", true,  "3a2A4b1B1c"),
                    Arguments.of("a", false,  "1a"),
                    Arguments.of("A", false,  "1a"),
                    Arguments.of("aaaAAbbbbBc", false,  "5a5b1c"),
                    Arguments.of("a".repeat(1000), true,  "1000a")
            );
        }

    }

    @Nested
    @DisplayName("rleCompress - null validation")
    class NullValidationRleCompress {

        @Test
        @DisplayName("throws when string is null")
        void throwsWhenStringIsNull() {
            String text = null;
            assertThatThrownBy(() -> service.rleCompress(text, true))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must not be null");
        }
    }

    @Nested
    @DisplayName("rleCompress - size validation")
    class SizeValidationRleCompress {

        @Test
        @DisplayName("throws when string is empty")
        void throwsWhenStringIsEmpty() {
            String text = "";
            assertThatThrownBy(() -> service.rleCompress(text, true))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must be at least 1 character long");
        }

        @Test
        @DisplayName("throws when string is longer than 1000")
        void throwsWhenStringIsTooLong() {
            String text = "a".repeat(1001);
            assertThatThrownBy(() -> service.rleCompress(text, true))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must be no longer than 1000 characters (got 1001)");
        }
    }

    @Nested
    @DisplayName("rleCompress - character validation")
    class AlphaValidationRleCompress {

        @ParameterizedTest
        @MethodSource("rleCases")
        @DisplayName("throws when string is not alphabetic")
        void throwsWhenStringIsNotAlpha(String text) {
            assertThatThrownBy(() -> service.rleCompress(text, true))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must contain only letters");
        }


        static Stream<Arguments> rleCases() {
            return Stream.of(
                    Arguments.of("aaaAAbbbb12Bc"),
                    Arguments.of("aaaAAbbbb12Bc1"),
                    Arguments.of("aaaAAbbbb#@Bc"),
                    Arguments.of("aaaAAbbbbBc!"),
                    Arguments.of("aaaAAbbbb Bc"),
                    Arguments.of("aaa-bbb"),           // dash
                    Arguments.of("żółw")
            );
        }
    }

    @Nested
    @DisplayName("MinutesBetween - happy path")
    class HappyPathMinutesBetween {

        @ParameterizedTest
        @MethodSource("minutesBetweenCases")
        @DisplayName("Calculate the number of minutes in a given time period")
        void minutesBetweenTimeRange(String timeRange, int expected) {
            assertThat(service.minutesBetween(timeRange)).isEqualTo(expected);
        }

        static Stream<Arguments> minutesBetweenCases() {
            return Stream.of(
                    Arguments.of("9:00am-10:00am", 60),
                    Arguments.of("09:00am-10:00am", 60),
                    Arguments.of("1:00pm - 11:00am", 1320),
                    Arguments.of("12:30PM-12:00AM", 690),
                    Arguments.of("12:00Am-12:00Am", 0),
                    Arguments.of("09:00am-10:00am", 60),
                    Arguments.of("11:13am-11:14am", 1),
                    Arguments.of("12:13am-12:13am", 0)
            );
        }
    }

    @Nested
    @DisplayName("MinutesBetween - range format validation")
    class RangeFormatValidationMinutesBetween {

        @ParameterizedTest
        @MethodSource("minutesBetweenCases")
        @DisplayName("throws when range time format is note valid")
        void throwsWhenRangeFormatIsnotValid(String timeRange) {
            assertThatThrownBy(() -> service.minutesBetween(timeRange))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("timeRange must follow the format h:mm(AM/PM)-h:mm(AM/PM)");
        }


        static Stream<Arguments> minutesBetweenCases() {
            return Stream.of(
                    Arguments.of("21:15-23:15"),
                    Arguments.of("00:00AM-00:15AM"),
                    Arguments.of("text"),
                    Arguments.of(""),
                    Arguments.of("11:11AM 11:15AM"),
                    Arguments.of("aaa-bbb"),
                    Arguments.of("13:12AM-13:17AM"),
                    Arguments.of("10AM-11AM")
            );
        }
    }

    @Nested
    @DisplayName("MinutesBetween - null validation")
    class NullValidationMinutesBetween {

        @Test
        @DisplayName("throws when timeRange is null")
        void throwsWhenTimeRangeIsNull() {
            String timeRange = null;
            assertThatThrownBy(() -> service.minutesBetween(timeRange))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("timeRange must not be null");
        }
    }

    @Nested
    @DisplayName("MostRepeatedLetters - happy path")
    class HappyPathMostRepeatedLetters {

        @ParameterizedTest
        @MethodSource("mostRepeatedLettersCases")
        @DisplayName("finds first word with most repeated letters")
        void mostRepeatedLettersInText(String text, String expected) {
            assertThat(service.mostRepeatedLetters(text)).isEqualTo(expected);
        }

        static Stream<Arguments> mostRepeatedLettersCases() {
            return Stream.of(
                    Arguments.of("Today, is the greatest day ever!", "greatest"),
                    Arguments.of("Hello apple pie", "Hello"),
                    Arguments.of("AAAaaa bbbbbb", "AAAaaa"),
                    Arguments.of("c", "c"),
                    Arguments.of("aabbcc aabbccdd", "aabbccdd"),
                    Arguments.of(",,,greatest,,,", "greatest"),
                    Arguments.of("a\tbb\nccc", "ccc"),
                    Arguments.of("Śrubokręt i klucz", "Śrubokręt"),
                    Arguments.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi convallis urna quis turpis aliquet, eget luctus odio facilisis. Sed a tellus quis mauris cursus cursus nec id mi. Proin nisl augue, sodales id arcu sed, faucibus tincidunt nunc. Cras eleifend elementum dignissim. Maecenas at quam vel dui mollis rutrum quis nec enim. Aenean vel malesuada justo, eu ullamcorper massa. Curabitur a gravida ligula. Integer ac erat semper, egestas diam vel, scelerisque ante. Maecenas sagittis nunc in accumsan varius. Proin purus metus, eleifend quis arcu ultrices, auctor lacinia mauris. Donec nisl eros, ultrices in diam ac, consequat viverra mi. Phasellus aliquam eu tortor non dictum. Donec ultrices id mauris eu facilisis. Vivamus nisi libero, aliquet sit amet turpis nec, fringilla imperdiet nisl. Duis et metus ut massa pharetra lobortis. Vestibulum placerat orci eget vehicula ultrices. Donec quis diam fermentum, gravida nibh et, ultricies lorem. Sed at tincidunt risus. Maecenas accumsan nisi dui. Phasellus tincidunt vehicula tortor, vel pulvinar tellus rutrum sit amet. Sed tincidunt ultricies ex, eget iaculis justo facilisis tristique. Aenean eget neque et massa eleifend dapibus non et diam. Nullam quis eleifend ex. In augue nibh, faucibus at consectetur et, congue nec ante. Pellentesque dictum nibh metus, quis fermentum augue sagittis id. Nulla condimentum finibus erat pharetra dapibus. Curabitur ut luctus turpis, vel mattis nibh. Sed purus augue, rutrum non sapien vitae, vulputate semper ligula. Phasellus vitae mi tortor. In ut urna nisl.", "Pellentesque"),
                    Arguments.of("Kukułka (zwyczajna), kukułka pospolita – nazwy ludowe: gżegżółka, reg. zazula lub zozula (Cuculus canorus) – gatunek średniego ptaka wędrownego z podrodziny kukułek (Cuculinae) w rodzinie kukułkowatych (Cuculidae). Jedyny w Europie Środkowej pasożyt lęgowy.", "kukułkowatych")
            );
        }
    }

    @Nested
    @DisplayName("MostRepeatedLetters - null validation")
    class NullValidationMostRepeatedLetters {

        @Test
        @DisplayName("throws when text is null")
        void throwsWhenTextIsNull() {
            String text = null;
            assertThatThrownBy(() -> service.mostRepeatedLetters(text))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must not be null");
        }
    }

    @Nested
    @DisplayName("MostRepeatedLetters - format validation")
    class FormatValidationMostRepeatedLetters {

        @ParameterizedTest
        @MethodSource("mostRepeatedLettersCases")
        @DisplayName("throws when text format is not valid")
        void throwsWhenTextFormatIsNotValid(String text) {
            assertThatThrownBy(() -> service.mostRepeatedLetters(text))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must contain at least one letter");
        }


        static Stream<Arguments> mostRepeatedLettersCases() {
            return Stream.of(
                    Arguments.of("12345678"),
                    Arguments.of(" "),
                    Arguments.of("!@#$%^&*()"),
                    Arguments.of("\t\n")
            );
        }
    }

    @Nested
    @DisplayName("MostRepeatedLetters - size validation")
    class SizeValidationMostRepeatedLetters {

        @Test
        @DisplayName("throws when text is empty")
        void throwsWhenTextIsEmpty() {
            String text = "";
            assertThatThrownBy(() -> service.mostRepeatedLetters(text))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must be at least 1 character long");
        }

        @Test
        @DisplayName("throws when string is longer than 10000")
        void throwsWhenStringIsTooLong() {
            String text = "a".repeat(10001);
            assertThatThrownBy(() -> service.mostRepeatedLetters(text))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("text must be no longer than 10000 characters (got 10001)");
        }
    }

    @Nested
    @DisplayName("noZeroPair - happy path")
    class HappyPathNoZeroPair {

        @ParameterizedTest
        @MethodSource("noZeroPairCases")
        @DisplayName("finds a pair of numbers whose sum equals the given number")
        void noZeroPairFromNumber(Long n, List<Integer> expected) {
            assertThat(service.noZeroPair(n)).containsExactlyElementsOf(expected);
        }

        static Stream<Arguments> noZeroPairCases() {
            return Stream.of(
                    Arguments.of(2L, List.of(1,1)),
                    Arguments.of(2147483647L, List.of(1,2147483646)),
                    Arguments.of(11L, List.of(2,9)),
                    Arguments.of(1010L, List.of(11,999)),
                    Arguments.of(2000000647L, List.of(648,1999999999))
                    );
        }
    }

    @Nested
    @DisplayName("NoZeroPair  - null validation")
    class NullValidationNoZeroPair {

        @Test
        @DisplayName("throws when n is null")
        void throwsWhenNIsNull() {
            Long aLong = null;
            assertThatThrownBy(() -> service.noZeroPair(aLong))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("n must not be null");
        }
    }

    @Nested
    @DisplayName("NoZeroPair - size validation")
    class SizeValidationNoZeroPair {

        @ParameterizedTest
        @MethodSource("noZeroPairTooSmallCases")
        @DisplayName("throws when n is less than 2")
        void throwsWhenNIsTooSmall(Long aLong) {
            assertThatThrownBy(() -> service.noZeroPair(aLong))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("n must be greater than 1");
        }

        static Stream<Arguments> noZeroPairTooSmallCases() {
            return Stream.of(
                    Arguments.of(1L),
                    Arguments.of(0L),
                    Arguments.of(-5L)
            );
        }

        @Test
        @DisplayName("throws when n is greater than 2147483647")
        void throwsWhenNIsTooBig() {
            Long aLong = 2147483648L;
            assertThatThrownBy(() -> service.noZeroPair(aLong))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("n must be no greater than 2147483647 (got 2147483648)");
        }
    }

    @Nested
    @DisplayName("isPhoneValid - happy path")
    class HappyPathIsPhoneValid {

        @ParameterizedTest
        @MethodSource("isPhoneValidCases")
        @DisplayName("Check that the phone number is correct")
        void isPhoneValid(String phone, boolean expected) {
            assertThat(service.isPhoneValid(phone)).isEqualTo(expected);
        }

        static Stream<Arguments> isPhoneValidCases() {
            return Stream.of(
                    Arguments.of("123456789", true),
                    Arguments.of("500600700", true),
                    Arguments.of("500 600 700", true),
                    Arguments.of("12 34 56 789", true),
                    Arguments.of("500-600-700", true),
                    Arguments.of("12-34-56-789", true),
                    Arguments.of("(22) 123 45 67", true),
                    Arguments.of("(12) 345-67-89", true),
                    Arguments.of("(61)1234567", true),
                    Arguments.of("(58) 987 65 43", true),
                    Arguments.of("+48123456789", true),
                    Arguments.of("+48 500 600 700", true),
                    Arguments.of("+48-500-600-700", true),
                    Arguments.of("+48 (22) 123 45 67", true),
                    Arguments.of("+48(12)3456789", true),
                    Arguments.of("+48 22 123 45 67", true),
                    Arguments.of(" 123 456 789 ", true),
                    Arguments.of("123\t456\t789", true),
                    Arguments.of(" +48 500 600 700 ", true),
                    Arguments.of("( 22 ) 123 45 67", true)
            );
        }
    }

    @Nested
    @DisplayName("isPhoneValid - invalidate format")
    class InvalidFormatIsPhoneValid {

        @ParameterizedTest
        @MethodSource("isPhoneValidInvalidateCases")
        @DisplayName("Check that the phone number is incorrect")
        void isPhoneInvalid(String phone, boolean expected) {
            assertThat(service.isPhoneValid(phone)).isEqualTo(expected);
        }

        static Stream<Arguments> isPhoneValidInvalidateCases() {
            return Stream.of(
                    Arguments.of("1234567890", false),
                    Arguments.of("+972 500600700", false),
                    Arguments.of("500 600 700 123", false),
                    Arguments.of("12 34 565 789", false),
                    Arguments.of("500*600*700", false),
                    Arguments.of("12@34!56?789", false),
                    Arguments.of("abcdefghi", false),
                    Arguments.of("(12) 345_67_89", false),
                    Arguments.of("", false),
                    Arguments.of("12345678", false),
                    Arguments.of("+4812345678", false),
                    Arguments.of("+48 12 34 56 78", false),
                    Arguments.of("123", false),
                    Arguments.of("+48 1", false)
            );
        }
    }

    @Nested
    @DisplayName("isPhoneValid  - null validation")
    class NullValidationIsPhoneValid {

        @Test
        @DisplayName("throws when phone is null")
        void throwsWhenPhoneIsNull() {
            String phone = null;
            assertThatThrownBy(() -> service.isPhoneValid(phone))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("phone must not be null");
        }
    }
}
