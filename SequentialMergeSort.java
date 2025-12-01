package algorithms;
public class SequentialMergeSort implements SortAlgorithm {

    private boolean isSorted(int[] array) { //to check array sorted or not
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void sort(int[] array) {
        
        if (array == null || array.length <= 1) { //edge case
            System.out.println("array has no elements or only one element");
            return; 
        }

        if (isSorted(array)) {//edge case
            System.out.println("array is already sorted");
            return; 
        }
        int[] temp = new int[array.length];
        mergeSort(array, 0, array.length - 1, temp);
    }

    private void mergeSort(int[] array, int left, int right, int[] temp) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(array, left, mid, temp);
            mergeSort(array, mid + 1, right, temp);
            merge(array, left, mid, right, temp);
        }
    }

    private void merge(int[] array, int left, int mid, int right, int[] temp) {
        System.arraycopy(array, left, temp, left, right - left + 1);

        int i = left;     
        int j = mid + 1; 
        int k = left;  

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