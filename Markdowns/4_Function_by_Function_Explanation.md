# 4. Function-by-Function Explanation

This section provides an exhaustive analysis of every function and method in the project, explaining what it does, why it exists, how it works, and what would happen if it were modified or removed.

---

## SortAlgorithm Interface

### Method: `void sort(int[] array)`

**What it does**: Declares the contract that all sorting implementations must fulfill - sort the given array in-place.

**Input parameters**:
- `array` - The integer array to be sorted (modified in-place)

**Output/Return value**: `void` - No return value; array is modified in-place

**Why it exists**: Defines the common interface for all sorting algorithms, enabling polymorphism

**Why implemented this way**: 
- In-place sorting is memory-efficient (no need to return new array)
- `void` return is standard for in-place operations
- Simple signature makes it easy to implement

**What happens if removed**: Entire project fails to compile (all classes depend on this interface)

**What happens if simplified**: Cannot simplify further - already minimal

**What happens if made sequential**: Not applicable (interface has no implementation)

---

## SequentialMergeSort Class

### Method: `public void sort(int[] array)`

**What it does**: Public entry point for sorting; handles edge cases and initiates the recursive merge sort process.

**Input parameters**:
- `array` - The integer array to be sorted in-place

**Output/Return value**: `void` - Array is sorted in-place

**Why it exists**: Implements the `SortAlgorithm` interface and provides the public API

**Implementation**:
```java
public void sort(int[] array) {
    // Edge case 1: null or trivially small
    if (array == null || array.length <= 1) {
        System.out.println("array has no elements or only one element");
        return; 
    }

    // Edge case 2: already sorted (optimization)
    if (isSorted(array)) {
        System.out.println("array is already sorted");
        return; 
    }
    
    // Allocate temp buffer once
    int[] temp = new int[array.length];
    
    // Start recursive sort
    mergeSort(array, 0, array.length - 1, temp);
}
```

**Why implemented this way**:
1. **Edge case handling**: Prevents unnecessary work and potential errors
2. **Early exit optimization**: `isSorted()` check avoids O(n log n) work if already sorted
3. **Single temp allocation**: More efficient than allocating in each recursive call
4. **Informational messages**: Help users understand what happened (though could be removed for production)

**What happens if removed**: 
- Cannot use `SequentialMergeSort` as a `SortAlgorithm`
- No entry point for sorting
- Edge cases not handled (could cause errors)

**What happens if simplified**:
```java
// Minimal version (no edge cases, no optimization)
public void sort(int[] array) {
    int[] temp = new int[array.length];
    mergeSort(array, 0, array.length - 1, temp);
}
```
- **Impact**: Less robust (null pointer exceptions possible)
- **Benefit**: Simpler, faster for typical cases
- **Trade-off**: Worse for edge cases

**What happens if made sequential**: Already sequential (no change)

---

### Method: `private boolean isSorted(int[] array)`

**What it does**: Checks if the array is already sorted in non-decreasing order.

**Input parameters**:
- `array` - The array to check

**Output/Return value**: `boolean` - `true` if sorted, `false` otherwise

**Why it exists**: Optimization to skip sorting if array is already sorted

**Implementation**:
```java
private boolean isSorted(int[] array) {
    for (int i = 1; i < array.length; i++) {
        if (array[i - 1] > array[i]) {
            return false;  // Found inversion
        }
    }
    return true;  // No inversions found
}
```

**Why implemented this way**:
- **Single pass**: O(n) time complexity
- **Early exit**: Returns `false` as soon as inversion found
- **Simple logic**: Easy to understand and verify

**What happens if removed**:
- **Performance impact**: Already-sorted arrays would still go through full O(n log n) sort
- **Correctness**: No impact (still produces correct output)
- **Use case**: Matters for nearly-sorted data (common in some applications)

**What happens if simplified**: Already minimal (cannot simplify further)

**What happens if made sequential**: Already sequential (no change)

---

### Method: `private void mergeSort(int[] array, int left, int right, int[] temp)`

**What it does**: Recursively divides the array into halves, sorts each half, and merges them.

**Input parameters**:
- `array` - The array being sorted (modified in-place)
- `left` - Starting index of segment to sort (inclusive)
- `right` - Ending index of segment to sort (inclusive)
- `temp` - Temporary buffer for merging (same length as `array`)

**Output/Return value**: `void` - Segment `[left..right]` is sorted in-place

**Why it exists**: Core recursive divide-and-conquer logic of merge sort

**Implementation**:
```java
private void mergeSort(int[] array, int left, int right, int[] temp) {
    if (left < right) {  // Base case: left >= right means 0 or 1 element
        int mid = left + (right - left) / 2;  // Overflow-safe midpoint
        
        mergeSort(array, left, mid, temp);      // Sort left half
        mergeSort(array, mid + 1, right, temp); // Sort right half
        
        merge(array, left, mid, right, temp);   // Merge sorted halves
    }
}
```

**Why implemented this way**:
1. **Base case**: `left >= right` means segment has ≤1 element (already sorted)
2. **Overflow-safe midpoint**: `left + (right - left) / 2` prevents integer overflow
3. **Recursive structure**: Natural expression of divide-and-conquer
4. **Shared temp buffer**: Passed down recursion tree (no repeated allocation)

**What happens if removed**:
- No sorting occurs (only edge case handling in `sort()`)
- Algorithm is non-functional

**What happens if simplified**:
```java
// Using overflow-prone midpoint
int mid = (left + right) / 2;
```
- **Risk**: Integer overflow if `left` and `right` are both large
- **Example**: `left = 2^30`, `right = 2^30` → sum exceeds `Integer.MAX_VALUE`

**What happens if made sequential**: Already sequential (no change)

**Recursion tree example** (array size 8):
```
mergeSort(0, 7)
├── mergeSort(0, 3)
│   ├── mergeSort(0, 1)
│   │   ├── mergeSort(0, 0) [base case]
│   │   └── mergeSort(1, 1) [base case]
│   └── mergeSort(2, 3)
│       ├── mergeSort(2, 2) [base case]
│       └── mergeSort(3, 3) [base case]
└── mergeSort(4, 7)
    ├── mergeSort(4, 5)
    │   ├── mergeSort(4, 4) [base case]
    │   └── mergeSort(5, 5) [base case]
    └── mergeSort(6, 7)
        ├── mergeSort(6, 6) [base case]
        └── mergeSort(7, 7) [base case]
```

---

### Method: `private void merge(int[] array, int left, int mid, int right, int[] temp)`

**What it does**: Merges two sorted subarrays `[left..mid]` and `[mid+1..right]` into a single sorted subarray.

**Input parameters**:
- `array` - The array containing the two sorted subarrays (modified in-place)
- `left` - Start of first sorted subarray
- `mid` - End of first sorted subarray (start of second is `mid+1`)
- `right` - End of second sorted subarray
- `temp` - Temporary buffer for merging

**Output/Return value**: `void` - Segment `[left..right]` is merged and sorted in-place

**Why it exists**: Combines two sorted halves into one sorted segment (the "conquer" step)

**Implementation**:
```java
private void merge(int[] array, int left, int mid, int right, int[] temp) {
    // Step 1: Copy segment to temp
    System.arraycopy(array, left, temp, left, right - left + 1);

    // Step 2: Initialize pointers
    int i = left;      // Pointer in left half [left..mid]
    int j = mid + 1;   // Pointer in right half [mid+1..right]
    int k = left;      // Pointer in result array

    // Step 3: Merge while both halves have elements
    while (i <= mid && j <= right) {
        if (temp[i] <= temp[j]) {
            array[k++] = temp[i++];  // Take from left (stable)
        } else {
            array[k++] = temp[j++];  // Take from right
        }
    }

    // Step 4: Copy remaining elements from left half
    while (i <= mid) {
        array[k++] = temp[i++];
    }

    // Step 5: Copy remaining elements from right half
    while (j <= right) {
        array[k++] = temp[j++];
    }
}
```

**Why implemented this way**:

1. **System.arraycopy()**: Native method, faster than manual loop
2. **Temp buffer**: Prevents overwriting values still needed for comparison
3. **Two-pointer technique**: Classic merge algorithm
4. **Stable sort** (`<=` not `<`): Preserves relative order of equal elements
5. **Remaining element loops**: One half exhausted, copy rest from other

**Detailed walkthrough example**:
```
Initial state:
array = [3, 7, 1, 5]  (after sorting [3,7] and [1,5])
left=0, mid=1, right=3

Step 1: Copy to temp
temp = [3, 7, 1, 5]

Step 2: Initialize pointers
i=0 (points to 3), j=2 (points to 1), k=0

Step 3: Merge loop
Iteration 1: temp[0]=3, temp[2]=1 → 1<3 → array[0]=1, j=3, k=1
Iteration 2: temp[0]=3, temp[3]=5 → 3<5 → array[1]=3, i=1, k=2
Iteration 3: temp[1]=7, temp[3]=5 → 5<7 → array[2]=5, j=4, k=3
j>right, exit loop

Step 4: Copy remaining from left
array[3]=7, i=2, k=4
i>mid, exit loop

Step 5: Copy remaining from right
j>right, skip

Final: array = [1, 3, 5, 7]
```

**Why temp buffer is needed**:
Without temp, writing to `array[k]` would overwrite values we still need to read:
```
array = [3, 7, 1, 5]
If we write array[0] = 1, we lose the original value 3 that we still need!
```

**What happens if removed**:
- Cannot merge sorted halves
- Algorithm is non-functional

**What happens if simplified**:
```java
// Manual copy instead of System.arraycopy
for (int idx = left; idx <= right; idx++) {
    temp[idx] = array[idx];
}
```
- **Impact**: 2-3× slower for large arrays
- **Benefit**: More readable for beginners

**What happens if made sequential**: Already sequential (no change)

**What happens if `<=` changed to `<`**:
```java
if (temp[i] < temp[j]) {  // Unstable
```
- **Impact**: Sort becomes **unstable** (equal elements may swap)
- **Example**: `[3a, 1, 3b]` might become `[1, 3b, 3a]` (relative order of 3s changed)
- **Importance**: Stability matters for multi-key sorting

---

## ParallelMergeSort Class

### Constructor: `public ParallelMergeSort()`

**What it does**: Creates a `ParallelMergeSort` instance with default threshold (10,000).

**Input parameters**: None

**Output/Return value**: New `ParallelMergeSort` instance

**Why it exists**: Provides convenient default configuration

**Implementation**:
```java
public ParallelMergeSort() {
    this(10_000);  // Delegate to parameterized constructor
}
```

**Why implemented this way**: Constructor chaining reduces code duplication

**What happens if removed**: Users must always specify threshold (less convenient)

---

### Constructor: `public ParallelMergeSort(int threshold)`

**What it does**: Creates a `ParallelMergeSort` instance with specified threshold.

**Input parameters**:
- `threshold` - Minimum segment size to process in parallel (must be positive)

**Output/Return value**: New `ParallelMergeSort` instance

**Why it exists**: Allows customizing threshold for different hardware or workloads

**Implementation**:
```java
public ParallelMergeSort(int threshold) {
    if (threshold <= 0) {
        throw new IllegalArgumentException("Threshold must be positive");
    }
    this.threshold = threshold;
    this.pool = ForkJoinPool.commonPool();
}
```

**Why implemented this way**:
1. **Validation**: Ensures threshold is positive (prevents infinite recursion)
2. **commonPool()**: Uses shared JVM thread pool (more efficient than creating new pool)
3. **Final fields**: Immutable after construction (thread-safe)

**What happens if removed**: Cannot customize threshold (less flexible)

**What happens if validation removed**:
```java
// No validation
this.threshold = threshold;
```
- **Risk**: Negative or zero threshold causes infinite recursion
- **Example**: `threshold=0` → always recurse, never reach base case → stack overflow

---

### Method: `public void sort(int[] array)`

**What it does**: Public entry point for parallel sorting; creates root task and submits to thread pool.

**Input parameters**:
- `array` - The integer array to be sorted in-place

**Output/Return value**: `void` - Array is sorted in-place

**Why it exists**: Implements `SortAlgorithm` interface and initiates parallel execution

**Implementation**:
```java
@Override
public void sort(int[] array) {
    if (array == null || array.length <= 1) {
        return;  // Edge case
    }

    int[] temp = new int[array.length];  // Allocate temp buffer once
    MergeSortTask rootTask = new MergeSortTask(array, temp, 0, array.length - 1, threshold);
    pool.invoke(rootTask);  // Submit and wait for completion
}
```

**Why implemented this way**:
1. **Edge case handling**: Prevents errors for null/tiny arrays
2. **Single temp allocation**: Shared by all tasks (memory efficient)
3. **Root task creation**: Represents sorting entire array
4. **pool.invoke()**: Submits task and blocks until complete (synchronous execution)

**What happens if removed**: Cannot use `ParallelMergeSort` as a `SortAlgorithm`

**What happens if simplified**:
```java
// No edge case handling
int[] temp = new int[array.length];
pool.invoke(new MergeSortTask(array, temp, 0, array.length - 1, threshold));
```
- **Risk**: Null pointer exception if `array` is null
- **Benefit**: Slightly faster for typical cases

**What happens if made sequential**: 
```java
// Sequential version
sequentialMergeSort(array, temp, 0, array.length - 1);
```
- **Impact**: Loses all parallelism, becomes sequential merge sort
- **Performance**: 3-4× slower on large arrays

---

## ParallelMergeSort.MergeSortTask (Inner Class)

### Constructor: `MergeSortTask(int[] array, int[] temp, int left, int right, int threshold)`

**What it does**: Creates a task representing sorting a segment `[left..right]` of the array.

**Input parameters**:
- `array` - The array being sorted
- `temp` - Temporary buffer for merging
- `left` - Start of segment (inclusive)
- `right` - End of segment (inclusive)
- `threshold` - Minimum size for parallel processing

**Output/Return value**: New `MergeSortTask` instance

**Why it exists**: Encapsulates the state needed for sorting a segment

**Implementation**:
```java
MergeSortTask(int[] array, int[] temp, int left, int right, int threshold) {
    this.array = array;
    this.temp = temp;
    this.left = left;
    this.right = right;
    this.threshold = threshold;
}
```

**Why implemented this way**: Simple field assignment (no validation needed, parent ensures valid values)

**What happens if removed**: Cannot create tasks (algorithm non-functional)

---

### Method: `protected void compute()`

**What it does**: Core parallel logic - decides whether to recurse in parallel or sort sequentially.

**Input parameters**: None (uses instance fields)

**Output/Return value**: `void` - Segment `[left..right]` is sorted in-place

**Why it exists**: Required by `RecursiveAction` - defines what the task does when executed

**Implementation**:
```java
@Override
protected void compute() {
    int length = right - left + 1;

    // Base case: segment too small for parallelism
    if (length <= threshold) {
        sequentialMergeSort(array, temp, left, right);
        return;
    }

    // Recursive case: split into parallel subtasks
    int mid = left + (right - left) / 2;
    MergeSortTask leftTask = new MergeSortTask(array, temp, left, mid, threshold);
    MergeSortTask rightTask = new MergeSortTask(array, temp, mid + 1, right, threshold);

    // Execute both tasks in parallel
    invokeAll(leftTask, rightTask);

    // Merge the sorted halves (sequential)
    merge(array, temp, left, mid, right);
}
```

**Why implemented this way**:

1. **Threshold check**: Prevents excessive task creation overhead
   - Below threshold: Sequential is faster (overhead > speedup)
   - Above threshold: Parallel is faster (speedup > overhead)

2. **Task creation**: Two subtasks for left and right halves
   - Independent work (no data dependencies)
   - Balanced load (roughly equal sizes)

3. **invokeAll()**: Executes both tasks in parallel
   - Current thread may execute one task
   - Other threads can steal the other task
   - Blocks until both complete

4. **Sequential merge**: After parallel sorting, merge sequentially
   - Merge is harder to parallelize efficiently
   - Sequential merge is fast enough (O(n) vs O(n log n) sort)

**Detailed execution flow**:
```
Thread 1: compute() for [0..999999]
  ├─ length = 1000000 > threshold (10000)
  ├─ Create leftTask [0..499999]
  ├─ Create rightTask [500000..999999]
  ├─ invokeAll(leftTask, rightTask)
  │   ├─ Thread 1: Execute leftTask
  │   │   ├─ compute() for [0..499999]
  │   │   ├─ Split into [0..249999] and [250000..499999]
  │   │   └─ ... (continues recursively)
  │   └─ Thread 2: Steal and execute rightTask
  │       ├─ compute() for [500000..999999]
  │       ├─ Split into [500000..749999] and [750000..999999]
  │       └─ ... (continues recursively)
  └─ merge([0..999999])
```

**What happens if removed**: Algorithm is non-functional (no execution logic)

**What happens if simplified**:
```java
// Always parallel (no threshold)
protected void compute() {
    if (left >= right) return;  // Base case: 0 or 1 element
    
    int mid = left + (right - left) / 2;
    invokeAll(
        new MergeSortTask(array, temp, left, mid, threshold),
        new MergeSortTask(array, temp, mid + 1, right, threshold)
    );
    merge(array, temp, left, mid, right);
}
```
- **Impact**: Creates tasks for every element (massive overhead)
- **Performance**: Much slower than sequential (overhead dominates)
- **Example**: 1M elements → ~1M tasks created (vs ~100 with threshold)

**What happens if made sequential**:
```java
// No parallelism
protected void compute() {
    sequentialMergeSort(array, temp, left, right);
}
```
- **Impact**: Loses all parallelism, becomes sequential
- **Performance**: 3-4× slower on large arrays

**Why invokeAll() instead of fork() + join()**:
```java
// Alternative (more verbose, same effect)
leftTask.fork();   // Push to work queue
rightTask.compute();  // Execute directly
leftTask.join();   // Wait for completion
```
- `invokeAll()` is cleaner and more idiomatic
- Framework optimizes execution order

---

### Method: `private static void sequentialMergeSort(int[] array, int[] temp, int left, int right)`

**What it does**: Standard recursive merge sort for small segments (below threshold).

**Input parameters**:
- `array` - The array being sorted
- `temp` - Temporary buffer for merging
- `left` - Start of segment (inclusive)
- `right` - End of segment (inclusive)

**Output/Return value**: `void` - Segment `[left..right]` is sorted in-place

**Why it exists**: Provides sequential fallback for small segments to avoid overhead

**Implementation**:
```java
private static void sequentialMergeSort(int[] array, int[] temp, int left, int right) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        sequentialMergeSort(array, temp, left, mid);
        sequentialMergeSort(array, temp, mid + 1, right);
        merge(array, temp, left, mid, right);
    }
}
```

**Why implemented this way**: Identical to `SequentialMergeSort.mergeSort()` (standard merge sort)

**Why static**: Doesn't need instance state, can be called from static inner class

**What happens if removed**: 
- Must always create parallel tasks (even for tiny segments)
- Performance degrades significantly (overhead dominates)

**What happens if simplified**: Already minimal (standard merge sort)

**What happens if made sequential**: Already sequential (no change)

---

### Method: `private static void merge(int[] array, int[] temp, int left, int mid, int right)`

**What it does**: Merges two sorted subarrays (identical to `SequentialMergeSort.merge()`).

**Input parameters**: Same as `SequentialMergeSort.merge()`

**Output/Return value**: `void` - Segment `[left..right]` is merged and sorted

**Why it exists**: Combines sorted halves (same as sequential version)

**Implementation**: Identical to `SequentialMergeSort.merge()` (see analysis above)

**Why static**: Doesn't need instance state, can be called from static inner class

**What happens if removed**: Cannot merge sorted halves (algorithm non-functional)

**What happens if simplified**: See `SequentialMergeSort.merge()` analysis

**What happens if made sequential**: Already sequential (merge is inherently sequential)

**Why merge is sequential**:
- Parallelizing merge is complex and often not worthwhile
- Merge is O(n), sort is O(n log n) - merge is small fraction of total time
- Parallel merge requires careful coordination (complex, error-prone)
- Sequential merge is fast enough in practice

---

## SortBenchmark Class

### Method: `public static void main(String[] args)`

**What it does**: Orchestrates the entire benchmark process - creates algorithms, generates test data, runs benchmarks, prints results.

**Input parameters**:
- `args` - Command-line arguments (currently unused)

**Output/Return value**: `void` - Prints results to console

**Why it exists**: Entry point for running benchmarks

**Implementation** (high-level flow):
```java
public static void main(String[] args) {
    // 1. Create algorithm instances
    SortAlgorithm arraysSort = new ArraysSortAlgorithm();
    SortAlgorithm arraysParallelSort = new ArraysParallelSortAlgorithm();
    String[] algorithmNames = {"SequentialMergeSort", "ParallelMergeSort", "Arrays.sort", "Arrays.parallelSort"};
    String[] patterns = {"Random", "Reverse"};

    // 2. Print header
    System.out.println("=== Sort Benchmark ===");
    System.out.println("Runs per case: " + RUNS_PER_CASE);

    // 3. For each size
    for (int size : SIZES) {
        int parallelThreshold = 10_000;
        SortAlgorithm seq = new SequentialMergeSort();
        SortAlgorithm par = new ParallelMergeSort(parallelThreshold);
        SortAlgorithm[] algorithms = {seq, par, arraysSort, arraysParallelSort};

        // 4. For each pattern
        for (String pattern : patterns) {
            int[] baseArray = pattern.equals("Random") 
                ? generateRandomArray(size)
                : generateReverseSortedArray(size);

            System.out.println("Size = " + size + ", Pattern = " + pattern + ", Parallel threshold = " + parallelThreshold);

            // 5. For each algorithm
            for (int i = 0; i < algorithms.length; i++) {
                long avgNanos = benchmarkAlgorithm(algorithms[i], baseArray, RUNS_PER_CASE);
                double avgMillis = avgNanos / 1_000_000.0;
                System.out.printf("%-20s : %.1f ms%n", algorithmNames[i], avgMillis);
            }
            System.out.println();
        }
    }
}
```

**Why implemented this way**:
1. **Nested loops**: Systematic coverage of all configurations
2. **Fresh algorithm instances**: Ensures no state pollution between tests
3. **Same base array**: Fair comparison (all algorithms sort identical data)
4. **Formatted output**: Readable results with aligned columns

**What happens if removed**: No automated benchmark (must manually test)

---

### Method: `public static long benchmarkAlgorithm(SortAlgorithm algorithm, int[] original, int runs)`

**What it does**: Runs an algorithm multiple times on the same input and returns average execution time.

**Input parameters**:
- `algorithm` - The sorting algorithm to benchmark
- `original` - The test array (not modified - cloned for each run)
- `runs` - Number of times to run (for averaging)

**Output/Return value**: `long` - Average execution time in nanoseconds

**Why it exists**: Provides fair, statistically valid performance measurement

**Implementation**:
```java
public static long benchmarkAlgorithm(SortAlgorithm algorithm, int[] original, int runs) {
    long totalNanos = 0L;
    
    for (int r = 0; r < runs; r++) {
        // Clone array (fairness - same input each run)
        int[] copy = Arrays.copyOf(original, original.length);
        
        // Measure execution time
        long start = System.nanoTime();
        algorithm.sort(copy);
        long end = System.nanoTime();

        // Validate correctness
        if (!isSorted(copy)) {
            throw new IllegalStateException("Array is not sorted correctly by " + algorithm.getClass().getSimpleName());
        }

        totalNanos += (end - start);
    }
    
    return totalNanos / runs;  // Average
}
```

**Why implemented this way**:
1. **Array cloning**: Ensures identical input for each run (fairness)
2. **System.nanoTime()**: High-precision timing (nanosecond resolution)
3. **Correctness check**: Catches bugs early (performance meaningless if wrong)
4. **Multiple runs**: Averages out JVM warmup, GC, OS scheduling noise
5. **Return nanoseconds**: Caller converts to milliseconds (preserves precision)

**What happens if removed**: Cannot measure performance (core functionality lost)

**What happens if simplified**:
```java
// Single run, no cloning
public static long benchmarkAlgorithm(SortAlgorithm algorithm, int[] array, int runs) {
    long start = System.nanoTime();
    algorithm.sort(array);
    long end = System.nanoTime();
    return end - start;
}
```
- **Problems**:
  1. No averaging (results unreliable due to noise)
  2. No cloning (subsequent algorithms sort already-sorted array - unfair)
  3. Ignores `runs` parameter (misleading API)

**What happens if made sequential**: Already sequential (benchmarking is inherently sequential)

---

### Method: `public static int[] generateRandomArray(int size)`

**What it does**: Creates an array of specified size filled with random integers.

**Input parameters**:
- `size` - Number of elements in array

**Output/Return value**: `int[]` - Array of random integers

**Why it exists**: Provides standardized random test data

**Implementation**:
```java
public static int[] generateRandomArray(int size) {
    int[] array = new int[size];
    for (int i = 0; i < size; i++) {
        array[i] = RANDOM.nextInt();  // Full integer range
    }
    return array;
}
```

**Why implemented this way**:
1. **RANDOM.nextInt()**: Full integer range (including negatives)
2. **Simple loop**: Straightforward, easy to understand
3. **No seed**: Different data each run (tests variety of inputs)

**What happens if removed**: Cannot generate random test data (major functionality lost)

**What happens if simplified**:
```java
// Limited range
array[i] = RANDOM.nextInt(1000);  // Only 0-999
```
- **Impact**: Less diverse data (may not expose all algorithm behaviors)
- **Benefit**: Easier to debug (smaller numbers)

---

### Method: `public static int[] generateReverseSortedArray(int size)`

**What it does**: Creates an array of specified size in descending order.

**Input parameters**:
- `size` - Number of elements in array

**Output/Return value**: `int[]` - Array in descending order

**Why it exists**: Provides worst-case test data for some algorithms

**Implementation**:
```java
public static int[] generateReverseSortedArray(int size) {
    int[] array = generateRandomArray(size);  // Random data
    Arrays.sort(array);                       // Sort ascending
    
    // Reverse to get descending
    for (int i = 0; i < array.length / 2; i++) {
        int tmp = array[i];
        array[i] = array[array.length - 1 - i];
        array[array.length - 1 - i] = tmp;
    }
    return array;
}
```

**Why implemented this way**:
1. **Start with random**: Ensures diverse values (not just 0, 1, 2, ...)
2. **Sort then reverse**: Guarantees strictly descending order
3. **In-place reversal**: Memory efficient (no extra array)

**Alternative implementation**:
```java
// Simpler but less diverse
for (int i = 0; i < size; i++) {
    array[i] = size - i;  // [size, size-1, ..., 2, 1]
}
```
- **Problem**: Predictable pattern (less realistic test)

**What happens if removed**: Cannot test reverse-sorted input (incomplete testing)

---

### Method: `public static boolean isSorted(int[] array)`

**What it does**: Checks if array is sorted in non-decreasing order.

**Input parameters**:
- `array` - Array to check

**Output/Return value**: `boolean` - `true` if sorted, `false` otherwise

**Why it exists**: Validates correctness of sorting algorithms

**Implementation**: Identical to `SequentialMergeSort.isSorted()` (see analysis above)

**What happens if removed**: Cannot validate correctness (bugs might go undetected)

---

### Inner Class: `ArraysSortAlgorithm`

**What it does**: Wraps `Arrays.sort()` to implement `SortAlgorithm` interface.

**Why it exists**: Allows including Java's built-in sort in benchmark

**Implementation**:
```java
static class ArraysSortAlgorithm implements SortAlgorithm {
    @Override
    public void sort(int[] array) {
        Arrays.sort(array);
    }
}
```

**Why implemented this way**: Adapter pattern - adapts incompatible interface

**What happens if removed**: Cannot benchmark `Arrays.sort()` (incomplete comparison)

---

### Inner Class: `ArraysParallelSortAlgorithm`

**What it does**: Wraps `Arrays.parallelSort()` to implement `SortAlgorithm` interface.

**Why it exists**: Allows including Java's built-in parallel sort in benchmark

**Implementation**:
```java
static class ArraysParallelSortAlgorithm implements SortAlgorithm {
    @Override
    public void sort(int[] array) {
        Arrays.parallelSort(array);
    }
}
```

**Why implemented this way**: Adapter pattern - adapts incompatible interface

**What happens if removed**: Cannot benchmark `Arrays.parallelSort()` (incomplete comparison)

---

## SortCorrectnessTests Class

### Method: `public static void main(String[] args)`

**What it does**: Runs all correctness tests and reports results.

**Input parameters**:
- `args` - Command-line arguments (unused)

**Output/Return value**: `void` - Prints test results

**Why it exists**: Entry point for running correctness tests

**Implementation** (high-level):
```java
public static void main(String[] args) {
    SortAlgorithm seq = new SequentialMergeSort();
    SortAlgorithm par = new ParallelMergeSort(10_000);

    System.out.println("=== SortCorrectnessTests ===");

    // 1. Edge cases
    testEmptyArray(seq, par);
    testSizeOneArray(seq, par);

    // 2. Small specific cases
    testSpecificCases(seq, par);

    // 3. Parallel equals sequential
    testParallelEqualsSequentialOnManyRandomInputs(seq, par);
    testParallelEqualsSequentialOnReverseInputs(seq, par);

    System.out.println("ALL CORRECTNESS TESTS PASSED");
}
```

**What happens if removed**: No automated correctness testing

---

### Method: `private static void testEmptyArray(SortAlgorithm seq, SortAlgorithm par)`

**What it does**: Tests that both algorithms handle empty arrays correctly.

**Input parameters**:
- `seq` - Sequential algorithm
- `par` - Parallel algorithm

**Output/Return value**: `void` - Throws exception if test fails

**Why it exists**: Edge case validation

**Implementation**:
```java
private static void testEmptyArray(SortAlgorithm seq, SortAlgorithm par) {
    int[] empty = new int[0];
    int[] emptyCopy1 = Arrays.copyOf(empty, empty.length);
    int[] emptyCopy2 = Arrays.copyOf(empty, empty.length);

    runQuietly(() -> seq.sort(emptyCopy1));
    par.sort(emptyCopy2);

    assertTrue(emptyCopy1.length == 0, "Sequential: empty array length changed");
    assertTrue(emptyCopy2.length == 0, "Parallel: empty array length changed");
    assertTrue(Arrays.equals(emptyCopy1, emptyCopy2), "Empty array: parallel != sequential");
    assertTrue(SortBenchmark.isSorted(emptyCopy1), "Sequential: empty array not sorted");
    assertTrue(SortBenchmark.isSorted(emptyCopy2), "Parallel: empty array not sorted");
}
```

**Why implemented this way**: Comprehensive checks (length, equality, sorted)

**What happens if removed**: Empty array bugs might go undetected

---

### Method: `private static void assertSameAsSequential(...)`

**What it does**: Runs both algorithms on same input and verifies identical output.

**Input parameters**:
- `seq` - Sequential algorithm
- `par` - Parallel algorithm
- `original` - Test array
- `label` - Description for error messages

**Output/Return value**: `void` - Throws exception if outputs differ

**Why it exists**: Core correctness check (parallel must match sequential)

**Implementation**:
```java
private static void assertSameAsSequential(SortAlgorithm seq, SortAlgorithm par, int[] original, String label) {
    int[] a = Arrays.copyOf(original, original.length);
    int[] b = Arrays.copyOf(original, original.length);

    runQuietly(() -> seq.sort(a));
    par.sort(b);

    assertTrue(SortBenchmark.isSorted(a), "Sequential not sorted: " + label);
    assertTrue(SortBenchmark.isSorted(b), "Parallel not sorted: " + label);
    assertTrue(Arrays.equals(a, b), "Parallel != Sequential: " + label);
}
```

**Why implemented this way**:
1. **Clone input**: Both algorithms sort identical data
2. **Check both sorted**: Ensures both produce sorted output
3. **Check equality**: Ensures outputs are identical (not just both sorted)
4. **Label**: Helps identify which test failed

**What happens if removed**: Cannot verify parallel correctness

---

### Method: `private static void runQuietly(Runnable action)`

**What it does**: Executes code while suppressing `System.out` output.

**Input parameters**:
- `action` - Code to execute

**Output/Return value**: `void`

**Why it exists**: Suppresses `SequentialMergeSort`'s informational messages during tests

**Implementation**:
```java
private static void runQuietly(Runnable action) {
    PrintStream originalOut = System.out;
    try {
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        action.run();
    } finally {
        System.setOut(originalOut);
    }
}
```

**Why implemented this way**: Temporarily redirects output, restores in finally block

**What happens if removed**: Test output cluttered with informational messages

---

## SortGUI Class

### Method: `public static void main(String[] args)`

**What it does**: Launches the GUI on the Event Dispatch Thread.

**Input parameters**:
- `args` - Command-line arguments (unused)

**Output/Return value**: `void` - Shows GUI window

**Why it exists**: Entry point for GUI application

**Implementation**:
```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(SortGUI::createAndShowGUI);
}
```

**Why implemented this way**: 
- `SwingUtilities.invokeLater()`: Ensures GUI created on Event Dispatch Thread (Swing requirement)
- Method reference: Clean, concise syntax

**What happens if removed**: Cannot launch GUI

---

### Method: `private static void createAndShowGUI()`

**What it does**: Creates and displays the main GUI window with all components.

**Input parameters**: None

**Output/Return value**: `void` - Shows GUI window

**Why it exists**: Constructs the GUI

**Implementation** (high-level):
```java
private static void createAndShowGUI() {
    // 1. Create frame
    JFrame frame = new JFrame("Parallel Merge Sort Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 700);

    // 2. Create control panel with inputs
    JPanel controlPanel = new JPanel(new GridBagLayout());
    // ... add algorithm combo, size field, pattern combo, threshold field, run button

    // 3. Create output components
    JTextArea outputArea = new JTextArea();
    PerformanceChartPanel chartPanel = new PerformanceChartPanel();
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Log", new JScrollPane(outputArea));
    tabbedPane.addTab("Performance Chart", chartPanel);

    // 4. Add action listener to run button
    runButton.addActionListener((ActionEvent e) -> {
        // Read inputs, validate, generate array, run algorithm, display results
    });

    // 5. Layout and show
    frame.add(controlPanel, BorderLayout.NORTH);
    frame.add(tabbedPane, BorderLayout.CENTER);
    frame.setVisible(true);
}
```

**Why implemented this way**: Standard Swing GUI construction pattern

**What happens if removed**: No GUI (only method that creates it)

---

### Inner Class: `PerformanceChartPanel`

**What it does**: Custom panel that plots time vs size for all runs.

**Key methods**:

**`void addPoint(int size, double timeMs, String algorithm)`**:
- Adds data point to chart
- Triggers repaint

**`protected void paintComponent(Graphics g)`**:
- Draws axes, ticks, labels, legend
- Plots data points color-coded by algorithm

**Why it exists**: Visualizes performance trends

**What happens if removed**: No performance chart (only log output)

---

## Summary

This section has provided exhaustive analysis of every function in the project, covering:

- **What each function does** (purpose and behavior)
- **Input parameters** (meaning, not just types)
- **Output/return values** (what they represent)
- **Why each function exists** (design rationale)
- **Why implemented this way** (design decisions)
- **What happens if removed** (impact on functionality)
- **What happens if simplified** (trade-offs)
- **What happens if made sequential** (parallelism impact)

Key insights:

1. **Edge case handling** is critical for robustness
2. **Threshold selection** is key to parallel performance
3. **Array cloning** ensures fair benchmarking
4. **Multiple runs** provide statistical validity
5. **Correctness validation** catches bugs early
6. **invokeAll()** simplifies parallel coordination
7. **Shared temp buffer** improves memory efficiency
8. **Stable sorting** matters for multi-key sorting
9. **Overflow-safe arithmetic** prevents subtle bugs
10. **Separation of concerns** improves maintainability
