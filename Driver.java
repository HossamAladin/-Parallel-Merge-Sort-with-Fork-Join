package algorithms;
import java.util.Arrays;
public class Driver {
    public static void main (String[] args) {
            int[] testArray = {1,1};
    System.out.println("Before sorting: " + Arrays.toString(testArray));
    new SequentialMergeSort().sort(testArray);
    System.out.println("After sorting: " + Arrays.toString(testArray));
}
    }


