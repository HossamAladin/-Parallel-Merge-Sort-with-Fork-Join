package algorithms;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Parallel merge sort implementation using Java's Fork/Join framework.
 * <p>
 * The algorithm recursively splits the array segment into halves, sorts each half
 * in parallel, and then merges the sorted halves. For small segments (size below
 * a configurable threshold) it falls back to a sequential merge sort to avoid
 * excessive task overhead.
 */
public class ParallelMergeSort implements SortAlgorithm {

    private final int threshold;
    private final ForkJoinPool pool;

    /**
     * Creates a ParallelMergeSort with a default threshold.
     */
    public ParallelMergeSort() {
        this(10_000); // reasonable default threshold for int[]
    }

    /**
     * Creates a ParallelMergeSort with a custom threshold.
     *
     * @param threshold minimum segment size to process in parallel
     */
    public ParallelMergeSort(int threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }
        this.threshold = threshold;
        this.pool = ForkJoinPool.commonPool();
    }

    @Override
    public void sort(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }

        int[] temp = new int[array.length];
        MergeSortTask rootTask = new MergeSortTask(array, temp, 0, array.length - 1, threshold);
        pool.invoke(rootTask);
    }

    /**
     * RecursiveAction task representing a merge sort on a subrange of the array.
     */
    private static class MergeSortTask extends RecursiveAction {

        private final int[] array;
        private final int[] temp;
        private final int left;
        private final int right;
        private final int threshold;

        MergeSortTask(int[] array, int[] temp, int left, int right, int threshold) {
            this.array = array;
            this.temp = temp;
            this.left = left;
            this.right = right;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int length = right - left + 1;

            // For small segments, use sequential merge sort to reduce overhead
            if (length <= threshold) {
                sequentialMergeSort(array, temp, left, right);
                return;
            }

            int mid = left + (right - left) / 2;
            MergeSortTask leftTask = new MergeSortTask(array, temp, left, mid, threshold);
            MergeSortTask rightTask = new MergeSortTask(array, temp, mid + 1, right, threshold);

            // Sort halves in parallel
            invokeAll(leftTask, rightTask);

            // Then merge the sorted halves
            merge(array, temp, left, mid, right);
        }

        /**
         * Standard recursive merge sort used for small segments.
         */
        private static void sequentialMergeSort(int[] array, int[] temp, int left, int right) {
            if (left < right) {
                int mid = left + (right - left) / 2;
                sequentialMergeSort(array, temp, left, mid);
                sequentialMergeSort(array, temp, mid + 1, right);
                merge(array, temp, left, mid, right);
            }
        }

        /**
         * Merges two sorted subarrays: [left..mid] and [mid+1..right] in-place using a temp buffer.
         */
        private static void merge(int[] array, int[] temp, int left, int mid, int right) {
            System.arraycopy(array, left, temp, left, right - left + 1);

            int i = left;      // pointer in left half
            int j = mid + 1;   // pointer in right half
            int k = left;      // pointer in merged array

            while (i <= mid && j <= right) {
                if (temp[i] <= temp[j]) {
                    array[k++] = temp[i++];
                } else {
                    array[k++] = temp[j++];
                }
            }

            while (i <= mid) {
                array[k++] = temp[i++];
            }

            while (j <= right) {
                array[k++] = temp[j++];
            }
        }
    }
}


