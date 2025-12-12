package algorithms;

import java.util.Arrays;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

/**
 * Lightweight correctness tests (no JUnit required).
 *
 * What this checks:
 * - Empty array and size-1 array do not throw and remain unchanged.
 * - All algorithms produce a sorted array.
 * - ParallelMergeSort produces EXACTLY the same output as SequentialMergeSort
 *   for the same input (important when duplicates exist).
 */
public class SortCorrectnessTests {

    private static final Random RAND = new Random(12345);

    public static void main(String[] args) {
        SortAlgorithm seq = new SequentialMergeSort();
        int threshold = 10_000;
        SortAlgorithm par = new ParallelMergeSort(threshold);

        System.out.println("=== SortCorrectnessTests ===");
        System.out.println("Parallel threshold: " + threshold);
        System.out.println();

        // Required explicit edge cases
        System.out.println("== 1) Edge cases ==");
        testEmptyArray(seq, par);
        System.out.println("PASS: empty array (length=0)");
        testSizeOneArray(seq, par);
        System.out.println("PASS: size-1 array (length=1)");
        System.out.println();

        // Additional small boundary cases
        System.out.println("== 2) Small specific cases ==");
        testSpecificCases(seq, par);
        System.out.println("PASS: specific cases (swap, already-sorted, reverse, duplicates, extremes)");
        System.out.println();

        // Explicit verification: parallel output == sequential output on same input
        System.out.println("== 3) Parallel output identical to Sequential output ==");
        testParallelEqualsSequentialOnManyRandomInputs(seq, par);
        System.out.println("PASS: random inputs (many sizes/trials) => parallel == sequential");
        testParallelEqualsSequentialOnReverseInputs(seq, par);
        System.out.println("PASS: reverse inputs (many sizes) => parallel == sequential");
        System.out.println();

        System.out.println("ALL CORRECTNESS TESTS PASSED");
    }

    private static void testEmptyArray(SortAlgorithm seq, SortAlgorithm par) {
        int[] empty = new int[0];
        int[] emptyCopy1 = Arrays.copyOf(empty, empty.length);
        int[] emptyCopy2 = Arrays.copyOf(empty, empty.length);

        runQuietly(() -> seq.sort(emptyCopy1));
        par.sort(emptyCopy2);

        assertTrue(emptyCopy1.length == 0, "Sequential: empty array length changed");
        assertTrue(emptyCopy2.length == 0, "Parallel: empty array length changed");
        assertTrue(Arrays.equals(emptyCopy1, emptyCopy2), "Empty array: parallel != sequential");
        assertTrue(SortBenchmark.isSorted(emptyCopy1), "Sequential: empty array not sorted (should be trivially sorted)");
        assertTrue(SortBenchmark.isSorted(emptyCopy2), "Parallel: empty array not sorted (should be trivially sorted)");
    }

    private static void testSizeOneArray(SortAlgorithm seq, SortAlgorithm par) {
        int[] one = new int[]{42};
        int[] oneCopy1 = Arrays.copyOf(one, one.length);
        int[] oneCopy2 = Arrays.copyOf(one, one.length);

        runQuietly(() -> seq.sort(oneCopy1));
        par.sort(oneCopy2);

        assertTrue(Arrays.equals(one, oneCopy1), "Sequential: size-1 array changed");
        assertTrue(Arrays.equals(one, oneCopy2), "Parallel: size-1 array changed");
        assertTrue(Arrays.equals(oneCopy1, oneCopy2), "Size-1 array: parallel != sequential");
        assertTrue(SortBenchmark.isSorted(oneCopy1), "Sequential: size-1 array not sorted");
        assertTrue(SortBenchmark.isSorted(oneCopy2), "Parallel: size-1 array not sorted");
    }

    private static void testSpecificCases(SortAlgorithm seq, SortAlgorithm par) {
        // size 2
        assertSameAsSequential(seq, par, new int[]{2, 1}, "size-2 swap");
        // already sorted
        assertSameAsSequential(seq, par, new int[]{1, 2, 3, 4, 5}, "already sorted");
        // reverse sorted
        assertSameAsSequential(seq, par, new int[]{5, 4, 3, 2, 1}, "reverse sorted");
        // duplicates (important for deterministic equality)
        assertSameAsSequential(seq, par, new int[]{3, 1, 2, 3, 2, 1, 3}, "duplicates");
        // negatives and extremes
        assertSameAsSequential(seq, par, new int[]{Integer.MAX_VALUE, 0, -1, Integer.MIN_VALUE, 7, -7}, "negatives/extremes");
    }

    private static void testParallelEqualsSequentialOnManyRandomInputs(SortAlgorithm seq, SortAlgorithm par) {
        int[] sizes = {0, 1, 2, 3, 10, 31, 128, 1000};

        for (int size : sizes) {
            for (int t = 0; t < 50; t++) {
                int[] input = new int[size];
                for (int i = 0; i < size; i++) {
                    // keep some duplicates common
                    input[i] = RAND.nextInt(200) - 100;
                }
                assertSameAsSequential(seq, par, input, "random(size=" + size + ", trial=" + t + ")");
            }
        }
    }

    private static void testParallelEqualsSequentialOnReverseInputs(SortAlgorithm seq, SortAlgorithm par) {
        int[] sizes = {0, 1, 2, 3, 10, 101, 1000};
        for (int size : sizes) {
            int[] input = SortBenchmark.generateReverseSortedArray(size);
            assertSameAsSequential(seq, par, input, "reverse(size=" + size + ")");
        }
    }

    private static void assertSameAsSequential(SortAlgorithm seq, SortAlgorithm par, int[] original, String label) {
        int[] a = Arrays.copyOf(original, original.length);
        int[] b = Arrays.copyOf(original, original.length);

        runQuietly(() -> seq.sort(a));
        par.sort(b);

        assertTrue(SortBenchmark.isSorted(a), "Sequential not sorted: " + label);
        assertTrue(SortBenchmark.isSorted(b), "Parallel not sorted: " + label);

        // Stronger check: identical output
        // (important when duplicates exist; also matches TA requirement)
        assertTrue(Arrays.equals(a, b), "Parallel != Sequential (different output): " + label);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * SequentialMergeSort currently prints informational messages for some edge cases.
     * For clean test output, we temporarily suppress System.out while executing the sequential sort.
     */
    private static void runQuietly(Runnable action) {
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            action.run();
        } finally {
            System.setOut(originalOut);
        }
    }
}


