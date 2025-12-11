package algorithms;

import java.util.Arrays;
import java.util.Random;

/**
 * Generates input arrays, runs different sort algorithms, and measures execution time.
 * <p>
 * Compares:
 * - SequentialMergeSort
 * - ParallelMergeSort (Fork/Join)
 * - Arrays.sort
 * - Arrays.parallelSort
 * <p>
 * It tests multiple sizes and input patterns (random, reverse-sorted) and reports
 * average runtimes.
 */
public class SortBenchmark {

    private static final int[] SIZES = {10_000};
    private static final int RUNS_PER_CASE = 5;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        SortAlgorithm seq = new SequentialMergeSort();
        SortAlgorithm par = new ParallelMergeSort(10_000);
        SortAlgorithm arraysSort = new ArraysSortAlgorithm();
        SortAlgorithm arraysParallelSort = new ArraysParallelSortAlgorithm();

        SortAlgorithm[] algorithms = {seq, par, arraysSort, arraysParallelSort};
        String[] algorithmNames = {"SequentialMergeSort", "ParallelMergeSort", "Arrays.sort", "Arrays.parallelSort"};

        String[] patterns = {"Random", "Reverse"};

        System.out.println("=== Sort Benchmark ===");
        System.out.println("Runs per case: " + RUNS_PER_CASE);
        System.out.println();

        for (int size : SIZES) {
            for (String pattern : patterns) {
                int[] baseArray;
                if ("Random".equals(pattern)) {
                    baseArray = generateRandomArray(size);
                } else {
                    baseArray = generateReverseSortedArray(size);
                }

                System.out.println("Size = " + size + ", Pattern = " + pattern);
                for (int i = 0; i < algorithms.length; i++) {
                    long avgNanos = benchmarkAlgorithm(algorithms[i], baseArray, RUNS_PER_CASE);
                    double avgMillis = avgNanos / 1_000_000.0;
                    System.out.printf("%-20s : %.1f ms%n", algorithmNames[i], avgMillis);
                }
                System.out.println();
            }
        }
    }

    /**
     * Benchmarks a single algorithm on the same input (cloned each run) and returns
     * the average time in nanoseconds.
     */
    public static long benchmarkAlgorithm(SortAlgorithm algorithm, int[] original, int runs) {
        long totalNanos = 0L;
        for (int r = 0; r < runs; r++) {
            int[] copy = Arrays.copyOf(original, original.length);
            long start = System.nanoTime();
            algorithm.sort(copy);
            long end = System.nanoTime();

            if (!isSorted(copy)) {
                throw new IllegalStateException("Array is not sorted correctly by " + algorithm.getClass().getSimpleName());
            }

            totalNanos += (end - start);
        }
        return totalNanos / runs;
    }

    /**
     * Generates a random int array of the given size.
     */
    public static int[] generateRandomArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = RANDOM.nextInt();
        }
        return array;
    }

    /**
     * Generates a reverse-sorted int array of the given size.
     */
    public static int[] generateReverseSortedArray(int size) {
        int[] array = generateRandomArray(size);
        Arrays.sort(array);
        // Reverse to get descending order
        for (int i = 0; i < array.length / 2; i++) {
            int tmp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = tmp;
        }
        return array;
    }

    /**
     * Utility to check if an array is sorted in non-decreasing order.
     */
    public static boolean isSorted(int[] array) {
        if (array == null || array.length <= 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Wrapper around Arrays.sort implementing SortAlgorithm, for comparison.
     */
    static class ArraysSortAlgorithm implements SortAlgorithm {
        @Override
        public void sort(int[] array) {
            Arrays.sort(array);
        }
    }

    /**
     * Wrapper around Arrays.parallelSort implementing SortAlgorithm, for comparison.
     */
    static class ArraysParallelSortAlgorithm implements SortAlgorithm {
        @Override
        public void sort(int[] array) {
            Arrays.parallelSort(array);
        }
    }
}


