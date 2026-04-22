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
}
