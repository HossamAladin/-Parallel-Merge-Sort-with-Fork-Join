# 3. Class-by-Class Explanation

This section provides a comprehensive analysis of each class in the project, explaining its purpose, design, interactions, and importance.

---

## 1. SortAlgorithm (Interface)

### Purpose and Responsibility

`SortAlgorithm` is an **interface** that defines the contract for all sorting algorithm implementations in the project. It declares a single method:

```java
void sort(int[] array);
```

**Responsibility**: Establish a common type and behavior for all sorting algorithms, enabling polymorphism and uniform treatment.

### Key Attributes

**None** - Interfaces in Java cannot have instance variables (only method signatures and constants).

### How It Interacts with Other Classes

**Implemented by**:
- `SequentialMergeSort`
- `ParallelMergeSort`
- `SortBenchmark.ArraysSortAlgorithm` (inner class)
- `SortBenchmark.ArraysParallelSortAlgorithm` (inner class)

**Used by**:
- `SortBenchmark` - stores algorithms as `SortAlgorithm[]` array
- `SortCorrectnessTests` - declares variables as `SortAlgorithm` type
- `SortGUI` - stores selected algorithm as `SortAlgorithm` type

**Interaction pattern**:
```java
// Declaration
SortAlgorithm algorithm;

// Assignment (runtime polymorphism)
algorithm = new ParallelMergeSort();

// Usage (same code works for any implementation)
algorithm.sort(myArray);
```

### Why This Design Was Chosen

**Design Pattern**: **Strategy Pattern**

**Rationale**:

1. **Polymorphism**: Allows treating different algorithms uniformly
   - Benchmark can iterate over an array of algorithms without knowing their concrete types
   - GUI can store and execute any algorithm using the same code path

2. **Extensibility**: New algorithms can be added without modifying existing code
   - Create new class implementing `SortAlgorithm`
   - Add to algorithm array in benchmark
   - No changes needed in benchmark logic

3. **Testability**: All algorithms can be tested using the same test harness
   - `SortCorrectnessTests` works for any `SortAlgorithm` implementation
   - Same correctness checks apply to all

4. **Separation of concerns**: Interface separates "what" from "how"
   - Interface defines what a sorting algorithm must do (sort an array)
   - Implementations define how they do it (sequential, parallel, etc.)

5. **Dependency Inversion Principle**: High-level code depends on abstraction, not concrete classes
   - Benchmark depends on `SortAlgorithm` interface, not specific implementations
   - Reduces coupling, increases flexibility

### What Happens If This Class Is Removed

**Compilation failures**:
- `SortBenchmark` cannot declare `SortAlgorithm[] algorithms`
- `SortCorrectnessTests` cannot declare `SortAlgorithm seq, par`
- `SortGUI` cannot declare `SortAlgorithm algorithm`

**Functional impact**:
- **No polymorphism**: Must use concrete types everywhere
- **Code duplication**: Need separate methods for each algorithm type
- **Reduced extensibility**: Adding new algorithms requires modifying existing code
- **Tight coupling**: Client code depends on specific implementations

**Example of resulting code without interface**:
```java
// Instead of:
for (SortAlgorithm alg : algorithms) {
    alg.sort(array);
}

// Would need:
sequentialSort.sort(array1);
parallelSort.sort(array2);
arraysSort.sort(array3);
// ... separate code for each
```

### What Happens If This Class Is Merged

**Cannot merge** - an interface has no implementation to merge with other classes.

**If replaced with abstract class**:
- Similar functionality possible
- Less flexible (classes can implement multiple interfaces but extend only one class)
- Would allow shared implementation code (but none exists here)

---

## 2. SequentialMergeSort

### Purpose and Responsibility

Implements the classic **sequential merge sort** algorithm using divide-and-conquer.

**Responsibility**: Sort integer arrays in non-decreasing order using a single thread, serving as the baseline for performance comparison.

### Key Attributes

**None** - This class has no instance variables. It's stateless, meaning each call to `sort()` is independent.

**Why stateless?**
- No need to maintain state between sort operations
- Thread-safe by default (no shared mutable state)
- Can be reused for multiple sort operations
- Simpler design

### Methods

1. **`sort(int[] array)`** - Public interface method
2. **`isSorted(int[] array)`** - Helper to check if already sorted
3. **`mergeSort(int[] array, int left, int right, int[] temp)`** - Recursive divide-and-conquer
4. **`merge(int[] array, int left, int mid, int right, int[] temp)`** - Merge two sorted halves

### How It Interacts with Other Classes

**Implements**: `SortAlgorithm` interface

**Used by**:
- `SortBenchmark` - measures performance
- `SortCorrectnessTests` - uses as reference for correctness
- `SortGUI` - user can select and run it

**Interaction pattern**:
```java
// In SortBenchmark
SortAlgorithm seq = new SequentialMergeSort();
long time = benchmarkAlgorithm(seq, testArray, 5);

// In SortCorrectnessTests
SortAlgorithm seq = new SequentialMergeSort();
seq.sort(array);
// Verify parallel output matches sequential output
```

**Key interaction**: Acts as the **reference implementation** - parallel algorithms must produce identical output.

### Why This Design Was Chosen

**Design decisions**:

1. **Single temp array allocation**
   ```java
   int[] temp = new int[array.length];
   mergeSort(array, 0, array.length - 1, temp);
   ```
   - **Why**: Allocate once, reuse for all recursive calls
   - **Alternative**: Allocate in each recursive call (wasteful)
   - **Benefit**: Reduces memory allocation overhead

2. **isSorted() early exit**
   ```java
   if (isSorted(array)) {
       System.out.println("array is already sorted");
       return;
   }
   ```
   - **Why**: Optimization for already-sorted input
   - **Cost**: O(n) check before sorting
   - **Benefit**: Avoids O(n log n) work if already sorted
   - **Trade-off**: Adds overhead for unsorted arrays

3. **Stable sorting (using `<=` in merge)**
   ```java
   if (temp[i] <= temp[j]) {
       array[k++] = temp[i++];
   }
   ```
   - **Why**: Preserves relative order of equal elements
   - **Importance**: Required for multi-key sorting
   - **Alternative**: Using `<` would be unstable

4. **Overflow-safe midpoint calculation**
   ```java
   int mid = left + (right - left) / 2;
   ```
   - **Why**: Prevents integer overflow
   - **Alternative**: `(left + right) / 2` can overflow if both are large
   - **Example**: If `left = 2^30` and `right = 2^30`, sum exceeds `Integer.MAX_VALUE`

5. **System.arraycopy for efficiency**
   ```java
   System.arraycopy(array, left, temp, left, right - left + 1);
   ```
   - **Why**: Native method, faster than manual loop
   - **Alternative**: `for (int i = left; i <= right; i++) temp[i] = array[i];`
   - **Benefit**: ~2-3× faster for large arrays

### What Happens If This Class Is Removed

**Direct impact**:
- **No baseline**: Cannot measure parallel speedup (no reference point)
- **Correctness tests fail**: `SortCorrectnessTests` uses this as reference
- **Incomplete comparison**: Benchmarks only compare parallel algorithms
- **Small-array performance**: For small arrays, this is actually the fastest

**Workaround**:
- Could use `Arrays.sort` as baseline
- But less educational (not seeing custom implementation)
- And less fair (comparing custom parallel to optimized built-in sequential)

### What Happens If This Class Is Merged

**If merged with ParallelMergeSort**:

Could create a single class with a boolean flag:
```java
public class MergeSort implements SortAlgorithm {
    private boolean parallel;
    
    public MergeSort(boolean parallel) {
        this.parallel = parallel;
    }
}
```

**Pros**:
- Less code duplication (merge logic is identical)
- Single class to maintain

**Cons**:
- More complex (conditional logic throughout)
- Violates Single Responsibility Principle
- Harder to understand (two algorithms in one class)
- Less clear separation of concerns

**Verdict**: Current design (separate classes) is better for clarity and maintainability.

---

## 3. ParallelMergeSort

### Purpose and Responsibility

Implements **parallel merge sort** using Java's Fork/Join framework to distribute work across multiple CPU cores.

**Responsibility**: Sort integer arrays in non-decreasing order using multiple threads, demonstrating parallel algorithm design and achieving speedup on large arrays.

### Key Attributes

```java
private final int threshold;
private final ForkJoinPool pool;
```

**`threshold`**:
- **Type**: `int`
- **Purpose**: Minimum segment size to process in parallel
- **Default**: 10,000
- **Why needed**: Prevents excessive task creation overhead
- **Impact**: Too small → overhead dominates; too large → insufficient parallelism

**`pool`**:
- **Type**: `ForkJoinPool`
- **Purpose**: Thread pool that executes parallel tasks
- **Value**: `ForkJoinPool.commonPool()` - shared JVM-wide pool
- **Why shared**: More efficient than creating new threads
- **Size**: Typically `Runtime.getRuntime().availableProcessors()` threads

### Inner Class: MergeSortTask

```java
private static class MergeSortTask extends RecursiveAction {
    private final int[] array;
    private final int[] temp;
    private final int left;
    private final int right;
    private final int threshold;
}
```

**Why static inner class?**
- Doesn't need reference to outer `ParallelMergeSort` instance
- More memory efficient (no implicit outer reference)
- Clearer that it only depends on passed parameters

**Extends RecursiveAction**:
- Part of Fork/Join framework
- For tasks that don't return values (void)
- Provides `compute()` method to override
- Provides `invokeAll()` for parallel execution

### How It Interacts with Other Classes

**Implements**: `SortAlgorithm` interface

**Uses**:
- `ForkJoinPool` - Java's parallel task execution framework
- `RecursiveAction` - Base class for Fork/Join tasks

**Used by**:
- `SortBenchmark` - measures parallel performance
- `SortCorrectnessTests` - validates parallel correctness
- `SortGUI` - user can select and run it

**Interaction with Fork/Join framework**:
```java
// Create root task
MergeSortTask rootTask = new MergeSortTask(array, temp, 0, array.length - 1, threshold);

// Submit to pool and wait for completion
pool.invoke(rootTask);

// Inside compute():
invokeAll(leftTask, rightTask);  // Parallel execution
```

**Interaction pattern**:
1. `ParallelMergeSort.sort()` creates root task
2. Root task splits into left and right subtasks
3. Subtasks are executed by worker threads from pool
4. Each subtask recursively splits until threshold reached
5. Small segments sorted sequentially
6. Results merged back up the recursion tree

### Why This Design Was Chosen

**Design decisions**:

1. **Fork/Join framework over manual threads**
   - **Why**: Work-stealing scheduler optimizes load balancing
   - **Alternative**: Manual thread creation and management
   - **Benefit**: Automatic thread pool sizing, efficient task distribution
   - **Simplicity**: Framework handles synchronization and coordination

2. **Threshold-based cutoff**
   ```java
   if (length <= threshold) {
       sequentialMergeSort(array, temp, left, right);
       return;
   }
   ```
   - **Why**: Avoid overhead for small tasks
   - **Overhead sources**: Task object creation, scheduling, synchronization
   - **Break-even point**: ~10,000 elements (empirically determined)
   - **Below threshold**: Sequential is faster
   - **Above threshold**: Parallel speedup exceeds overhead

3. **invokeAll() for parallel execution**
   ```java
   invokeAll(leftTask, rightTask);
   ```
   - **Why**: Executes both tasks in parallel
   - **Behavior**: Current thread may execute one task, others can steal the other
   - **Synchronization**: Implicitly waits for both to complete
   - **Alternative**: `fork()` + `join()` (more verbose, same effect)

4. **Shared temp array**
   ```java
   int[] temp = new int[array.length];  // Allocated once
   MergeSortTask rootTask = new MergeSortTask(array, temp, ...);
   ```
   - **Why**: Memory efficiency (one allocation instead of many)
   - **Safety**: Each task only modifies its `[left..right]` segment (non-overlapping)
   - **No locks needed**: Non-overlapping ranges prevent race conditions

5. **Static merge and sequentialMergeSort methods**
   ```java
   private static void merge(...)
   private static void sequentialMergeSort(...)
   ```
   - **Why**: Don't need instance state
   - **Benefit**: Can be called from static inner class
   - **Clarity**: Emphasizes they're pure functions (no side effects on instance)

6. **Configurable threshold**
   ```java
   public ParallelMergeSort(int threshold) { ... }
   ```
   - **Why**: Different hardware may have different optimal thresholds
   - **Flexibility**: GUI allows experimenting with different values
   - **Default**: Provides reasonable default (10,000) for typical systems

### What Happens If This Class Is Removed

**Direct impact**:
- **No custom parallelism**: Project loses its core contribution
- **No speedup demonstration**: Cannot show parallel performance improvement
- **Reduced educational value**: No demonstration of Fork/Join framework
- **Incomplete comparison**: Only sequential and built-in algorithms remain

**What's lost**:
- Understanding of parallel algorithm design
- Demonstration of task decomposition
- Illustration of threshold selection importance
- Proof that parallelism provides real speedup

**Remaining algorithms**:
- `SequentialMergeSort` - baseline
- `Arrays.sort` - sequential built-in
- `Arrays.parallelSort` - parallel built-in (black box)

**Why this matters**: Without custom parallel implementation, project becomes trivial comparison of sequential to built-ins, with no demonstration of parallel programming skills.

### What Happens If This Class Is Merged

**If merged with SequentialMergeSort** (as discussed above):
- Could use flag to switch between parallel and sequential
- Would reduce code duplication
- But would increase complexity and reduce clarity

**If merged into a single method**:
```java
public void sort(int[] array) {
    if (array.length < threshold) {
        sequentialSort(array);
    } else {
        parallelSort(array);
    }
}
```

**Problems**:
- Loses fine-grained control (threshold applies only at top level)
- Cannot recursively switch between parallel and sequential
- Less efficient (all-or-nothing approach)

**Verdict**: Current design (separate class with recursive threshold checks) is optimal.

---

## 4. SortBenchmark

### Purpose and Responsibility

Provides automated performance benchmarking infrastructure for comparing all sorting algorithms.

**Responsibility**: Generate test data, measure execution time, validate correctness, and report results in a standardized format.

### Key Attributes

```java
private static final int[] SIZES = {100_000, 500_000, 1_000_000};
private static final int RUNS_PER_CASE = 5;
private static final Random RANDOM = new Random();
```

**`SIZES`**:
- **Purpose**: Array sizes to benchmark
- **Values**: 100k, 500k, 1M
- **Why these**: Show performance scaling from medium to large
- **Rationale**: Small sizes (10k) don't show parallel benefits; very large (10M+) take too long

**`RUNS_PER_CASE`**:
- **Purpose**: Number of times to run each test
- **Value**: 5
- **Why**: Average out JVM warmup, garbage collection, OS scheduling variations
- **Trade-off**: More runs = more accurate but slower

**`RANDOM`**:
- **Purpose**: Generate random test data
- **Why static**: Shared across all test cases
- **Seed**: Not fixed (different data each run)

### Methods

**Main method**:
- Orchestrates entire benchmark
- Creates algorithm instances
- Iterates over sizes and patterns
- Prints results

**`benchmarkAlgorithm(SortAlgorithm algorithm, int[] original, int runs)`**:
- Runs algorithm multiple times on same input
- Clones array for each run (fairness)
- Measures time with `System.nanoTime()`
- Validates output is sorted
- Returns average time in nanoseconds

**`generateRandomArray(int size)`**:
- Creates array with random integers
- Uses `RANDOM.nextInt()` for full integer range

**`generateReverseSortedArray(int size)`**:
- Creates descending-order array
- First generates random, sorts ascending, then reverses

**`isSorted(int[] array)`**:
- Validates array is in non-decreasing order
- Returns `true` if sorted, `false` otherwise

### Inner Classes

**`ArraysSortAlgorithm`**:
- Wraps `Arrays.sort()` to implement `SortAlgorithm`
- Allows including built-in in benchmark

**`ArraysParallelSortAlgorithm`**:
- Wraps `Arrays.parallelSort()` to implement `SortAlgorithm`
- Allows including built-in parallel in benchmark

### How It Interacts with Other Classes

**Uses**:
- `SortAlgorithm` - interface for all algorithms
- `SequentialMergeSort` - custom sequential implementation
- `ParallelMergeSort` - custom parallel implementation
- `Arrays.sort` - via wrapper class
- `Arrays.parallelSort` - via wrapper class

**Used by**:
- `SortGUI` - calls utility methods (`generateRandomArray`, `isSorted`, `benchmarkAlgorithm`)

**Interaction pattern**:
```java
// For each size and pattern:
for (int size : SIZES) {
    for (String pattern : patterns) {
        int[] baseArray = generateRandomArray(size);
        
        for (SortAlgorithm alg : algorithms) {
            long avgTime = benchmarkAlgorithm(alg, baseArray, RUNS_PER_CASE);
            System.out.printf("%-20s : %.1f ms%n", name, avgTime / 1_000_000.0);
        }
    }
}
```

### Why This Design Was Chosen

**Design decisions**:

1. **Multiple sizes**
   - **Why**: Show how performance scales
   - **Observation**: Parallel overhead matters at small sizes, speedup increases with size

2. **Multiple patterns**
   - **Why**: Different algorithms perform differently on different inputs
   - **Random**: General case, no exploitable patterns
   - **Reverse**: Worst case for some algorithms, best for others (with pattern detection)

3. **Multiple runs and averaging**
   - **Why**: Reduce impact of noise (JVM warmup, GC, OS scheduling)
   - **First run**: Often slower (JIT compilation, cold caches)
   - **Subsequent runs**: More representative of steady-state performance

4. **Array cloning for fairness**
   ```java
   int[] copy = Arrays.copyOf(original, original.length);
   ```
   - **Why**: All algorithms sort identical data
   - **Alternative**: Each algorithm sorts different data (unfair)
   - **Importance**: Ensures comparison is valid

5. **Correctness validation**
   ```java
   if (!isSorted(copy)) {
       throw new IllegalStateException("Array is not sorted correctly");
   }
   ```
   - **Why**: Catch bugs early
   - **Importance**: Performance means nothing if output is wrong

6. **High-precision timing**
   ```java
   long start = System.nanoTime();
   algorithm.sort(copy);
   long end = System.nanoTime();
   ```
   - **Why**: Nanosecond precision (vs millisecond for `currentTimeMillis()`)
   - **Importance**: Some operations take < 1 ms

7. **Wrapper classes for built-ins**
   - **Why**: Allows treating built-ins uniformly with custom implementations
   - **Benefit**: Same benchmark code works for all
   - **Pattern**: Adapter pattern

### What Happens If This Class Is Removed

**Direct impact**:
- **No automated benchmarking**: Must manually time each algorithm
- **No performance data**: Cannot generate results tables
- **No statistical validity**: Single runs unreliable
- **No standardized comparison**: Different tests might use different inputs

**Workarounds**:
- Manually write timing code for each test case
- Manually run and record results
- Manually average multiple runs

**Why this matters**: Benchmarking is tedious and error-prone without automation. This class makes it systematic and reproducible.

### What Happens If This Class Is Merged

**If merged with SortCorrectnessTests**:

Could create a single `SortTests` class with both correctness and performance tests.

**Pros**:
- Single file for all testing
- Reduced file count

**Cons**:
- Violates Single Responsibility Principle (one class doing two things)
- Harder to run tests independently
- Mixes concerns (correctness vs performance)

**Verdict**: Current design (separate classes) is better for separation of concerns.

---

## 5. SortCorrectnessTests

### Purpose and Responsibility

Provides comprehensive correctness validation for all sorting algorithms, with emphasis on verifying parallel implementation produces identical results to sequential.

**Responsibility**: Test edge cases, validate sorted output, ensure parallel equals sequential, prevent regressions.

### Key Attributes

```java
private static final Random RAND = new Random(12345);
```

**`RAND`**:
- **Purpose**: Generate random test data
- **Seed**: Fixed (12345) for reproducibility
- **Why fixed seed**: Tests are deterministic (same results every run)
- **Importance**: Reproducible tests are easier to debug

### Methods

**Main method**:
- Orchestrates all tests
- Prints progress and results
- Exits with success message if all pass

**`testEmptyArray()`**:
- Tests length-0 arrays
- Verifies no exceptions thrown
- Validates output equals input

**`testSizeOneArray()`**:
- Tests length-1 arrays
- Verifies no exceptions thrown
- Validates output equals input

**`testSpecificCases()`**:
- Tests small specific cases (size 2-7)
- Covers: swap, sorted, reverse, duplicates, negatives, extremes

**`testParallelEqualsSequentialOnManyRandomInputs()`**:
- Tests many random inputs (sizes 0-1000, 50 trials each)
- Validates parallel output exactly matches sequential

**`testParallelEqualsSequentialOnReverseInputs()`**:
- Tests reverse-sorted inputs (sizes 0-1000)
- Validates parallel output exactly matches sequential

**`assertSameAsSequential()`**:
- Helper that runs both sequential and parallel on same input
- Validates both produce sorted output
- Validates outputs are identical (not just both sorted)

**`assertTrue()`**:
- Custom assertion (no JUnit dependency)
- Throws `AssertionError` if condition false

**`runQuietly()`**:
- Suppresses `System.out` during execution
- Used to silence `SequentialMergeSort`'s informational messages

### How It Interacts with Other Classes

**Uses**:
- `SortAlgorithm` - interface for algorithms
- `SequentialMergeSort` - reference implementation
- `ParallelMergeSort` - implementation under test
- `SortBenchmark.isSorted()` - validates sorted output
- `SortBenchmark.generateReverseSortedArray()` - generates test data

**Interaction pattern**:
```java
// For each test case:
int[] a = Arrays.copyOf(original, original.length);
int[] b = Arrays.copyOf(original, original.length);

seq.sort(a);  // Sequential
par.sort(b);  // Parallel

assertTrue(isSorted(a), "Sequential not sorted");
assertTrue(isSorted(b), "Parallel not sorted");
assertTrue(Arrays.equals(a, b), "Parallel != Sequential");
```

### Why This Design Was Chosen

**Design decisions**:

1. **No external dependencies (no JUnit)**
   - **Why**: Simpler deployment (no library dependencies)
   - **Trade-off**: Must implement own assertions
   - **Benefit**: Runs anywhere Java is installed

2. **Fixed random seed**
   ```java
   private static final Random RAND = new Random(12345);
   ```
   - **Why**: Reproducible tests
   - **Benefit**: Same test data every run
   - **Debugging**: Can reproduce failures

3. **Comprehensive size coverage**
   ```java
   int[] sizes = {0, 1, 2, 3, 10, 31, 128, 1000};
   ```
   - **Why**: Edge cases (0, 1, 2) and various sizes
   - **31, 128**: Power-of-2 adjacent (boundary cases for binary algorithms)
   - **1000**: Large enough to test parallelism

4. **Many trials per size**
   ```java
   for (int t = 0; t < 50; t++) { ... }
   ```
   - **Why**: Increase confidence in correctness
   - **Rationale**: Parallel algorithms have non-deterministic thread scheduling
   - **Goal**: Catch rare race conditions

5. **Strict equality check**
   ```java
   assertTrue(Arrays.equals(a, b), "Parallel != Sequential");
   ```
   - **Why**: Not just "both sorted" but "identical output"
   - **Importance**: Stability matters (equal elements must maintain order)
   - **Rationale**: Parallel must be functionally equivalent to sequential

6. **Quiet execution**
   ```java
   runQuietly(() -> seq.sort(a));
   ```
   - **Why**: `SequentialMergeSort` prints messages for edge cases
   - **Problem**: Clutters test output
   - **Solution**: Temporarily redirect `System.out` to null

7. **Explicit test categories**
   - Edge cases (empty, size-1)
   - Small specific cases (swap, sorted, reverse, duplicates)
   - Random inputs (many sizes and trials)
   - Reverse inputs (structured pattern)
   - **Why**: Organized, clear what's being tested

### What Happens If This Class Is Removed

**Direct impact**:
- **No correctness guarantee**: Bugs might go undetected
- **No edge case validation**: Empty/small arrays might not work
- **No regression testing**: Code changes might introduce bugs
- **Reduced confidence**: Cannot prove algorithms are correct

**Specific risks**:
- Parallel implementation might have race conditions
- Edge cases might throw exceptions
- Duplicates might not be handled correctly
- Stability might be violated

**Discovery of bugs**:
- Would happen during benchmarking (harder to diagnose)
- Or worse, in production (catastrophic)

### What Happens If This Class Is Merged

**If merged with SortBenchmark**:
- Could have single test class with both correctness and performance
- See analysis in SortBenchmark section above

---

## 6. SortGUI

### Purpose and Responsibility

Provides interactive graphical user interface for experimenting with sorting algorithms and visualizing performance.

**Responsibility**: Allow users to select algorithms, configure parameters, run sorts, view results, and visualize performance trends.

### Key Attributes

**GUI components** (created in `createAndShowGUI()`):
- `JFrame` - main window
- `JComboBox` - algorithm and pattern selection
- `JTextField` - size and threshold input
- `JButton` - run sort
- `JTextArea` - log output
- `JTabbedPane` - tabs for log and chart
- `PerformanceChartPanel` - custom chart component

### Inner Class: PerformanceChartPanel

```java
private static class PerformanceChartPanel extends JPanel {
    private final List<DataPoint> points = new ArrayList<>();
    
    void addPoint(int size, double timeMs, String algorithm) { ... }
    
    @Override
    protected void paintComponent(Graphics g) { ... }
}
```

**Purpose**: Custom chart component that plots time vs size

**Key features**:
- Accumulates data points from all runs
- Color-codes by algorithm
- Draws axes, ticks, labels, legend
- Scales automatically to fit data

### How It Interacts with Other Classes

**Uses**:
- `SortAlgorithm` - interface for selected algorithm
- `SequentialMergeSort` - user can select
- `ParallelMergeSort` - user can select
- `SortBenchmark.ArraysSortAlgorithm` - user can select
- `SortBenchmark.ArraysParallelSortAlgorithm` - user can select
- `SortBenchmark.generateRandomArray()` - generates test data
- `SortBenchmark.generateReverseSortedArray()` - generates test data
- `SortBenchmark.isSorted()` - validates output

**Interaction pattern**:
```java
// User clicks "Run Sort"
ActionListener:
    1. Read user inputs (algorithm, size, pattern, threshold)
    2. Validate inputs
    3. Generate test array
    4. Create algorithm instance
    5. Time execution
    6. Validate output
    7. Display results in log
    8. Add point to chart
```

### Why This Design Was Chosen

**Design decisions**:

1. **Swing framework**
   - **Why**: Standard Java GUI toolkit (no external dependencies)
   - **Alternative**: JavaFX (more modern but requires separate module)
   - **Benefit**: Works on any Java installation

2. **Tabbed interface**
   - **Why**: Separates log from chart (cleaner UI)
   - **Alternative**: Split pane (more complex)
   - **Benefit**: User can focus on one view at a time

3. **GridBagLayout for control panel**
   - **Why**: Flexible layout for form-like controls
   - **Alternative**: GridLayout (less flexible), BorderLayout (too simple)
   - **Benefit**: Professional-looking aligned controls

4. **Custom chart panel**
   - **Why**: No external charting library needed
   - **Alternative**: JFreeChart (powerful but heavy dependency)
   - **Trade-off**: Simpler but less features
   - **Benefit**: Lightweight, sufficient for this project

5. **Input validation**
   ```java
   try {
       size = Integer.parseInt(sizeField.getText().trim());
       if (size <= 0) throw new NumberFormatException();
   } catch (NumberFormatException ex) {
       JOptionPane.showMessageDialog(frame, "Please enter a positive integer", ...);
       return;
   }
   ```
   - **Why**: Prevent invalid inputs
   - **User experience**: Clear error messages
   - **Robustness**: Prevents crashes

6. **Array preview (first 20 elements)**
   ```java
   int previewLength = Math.min(20, arrayToSort.length);
   int[] beforePreview = Arrays.copyOf(original, previewLength);
   ```
   - **Why**: Show enough to see patterns without clutter
   - **20 elements**: Good balance (not too few, not too many)
   - **Benefit**: User can verify sorting worked

7. **Accumulating chart**
   - **Why**: Shows trends across multiple runs
   - **Benefit**: Can compare different configurations
   - **Alternative**: Clear chart each run (loses history)

8. **Color-coded algorithms**
   ```java
   private Color colorForAlgorithm(String algorithm) {
       if ("Sequential Merge Sort".equals(algorithm)) return new Color(0x1f77b4);
       if ("Parallel Merge Sort".equals(algorithm)) return new Color(0xff7f0e);
       // ...
   }
   ```
   - **Why**: Easy to distinguish algorithms in chart
   - **Colors**: Chosen for good contrast and visibility

### What Happens If This Class Is Removed

**Direct impact**:
- **No interactive exploration**: Users must edit code and recompile
- **No visual feedback**: Cannot see performance chart
- **Reduced accessibility**: Non-programmers cannot use project
- **Harder threshold tuning**: Must edit code to test different thresholds

**Remaining functionality**:
- `SortBenchmark` still works (command-line)
- `SortCorrectnessTests` still works (command-line)
- All algorithms still work

**Why this matters**: GUI makes project more accessible and impressive, but isn't essential for core functionality.

### What Happens If This Class Is Merged

**If merged with SortBenchmark**:

Could add GUI code to `SortBenchmark` with a flag:
```java
public static void main(String[] args) {
    if (args.length > 0 && args[0].equals("--gui")) {
        launchGUI();
    } else {
        runBenchmark();
    }
}
```

**Pros**:
- Single entry point
- Reduced file count

**Cons**:
- Violates Single Responsibility Principle
- Mixes GUI and benchmark logic
- Harder to maintain
- Larger file (348 + 147 = 495 lines)

**Verdict**: Current design (separate classes) is better for separation of concerns.

---

## Summary Table

| Class | Type | Primary Responsibility | Key Design Pattern | Criticality |
|-------|------|----------------------|-------------------|-------------|
| SortAlgorithm | Interface | Define sorting contract | Strategy | ✓✓✓ Critical |
| SequentialMergeSort | Concrete | Baseline sequential sort | Divide-and-conquer | ✓✓✓ Critical |
| ParallelMergeSort | Concrete | Parallel sort with Fork/Join | Divide-and-conquer + Task parallelism | ✓✓✓ Critical |
| SortBenchmark | Utility | Automated performance testing | - | ✓✓ Important |
| SortCorrectnessTests | Utility | Automated correctness validation | - | ✓✓ Important |
| SortGUI | Application | Interactive user interface | MVC (loosely) | ✓ Useful |

---

## Class Interaction Diagram

```
SortAlgorithm (interface)
    ↑ implements
    ├── SequentialMergeSort
    ├── ParallelMergeSort
    ├── SortBenchmark.ArraysSortAlgorithm
    └── SortBenchmark.ArraysParallelSortAlgorithm

SortBenchmark
    ├── uses → SortAlgorithm (polymorphism)
    ├── creates → SequentialMergeSort
    ├── creates → ParallelMergeSort
    └── provides utilities → SortGUI

SortCorrectnessTests
    ├── uses → SortAlgorithm (polymorphism)
    ├── creates → SequentialMergeSort
    ├── creates → ParallelMergeSort
    └── uses utilities → SortBenchmark

SortGUI
    ├── uses → SortAlgorithm (polymorphism)
    ├── creates → SequentialMergeSort
    ├── creates → ParallelMergeSort
    ├── creates → SortBenchmark.ArraysSortAlgorithm
    ├── creates → SortBenchmark.ArraysParallelSortAlgorithm
    └── uses utilities → SortBenchmark

ParallelMergeSort
    ├── uses → ForkJoinPool (Java framework)
    └── inner class → MergeSortTask extends RecursiveAction
```

---

## Design Principles Demonstrated

1. **Interface-based design**: `SortAlgorithm` enables polymorphism
2. **Single Responsibility**: Each class has one clear purpose
3. **Separation of Concerns**: Algorithm, testing, benchmarking, and UI are separate
4. **Open/Closed Principle**: New algorithms can be added without modifying existing code
5. **Dependency Inversion**: High-level code depends on abstractions (interface), not concrete classes
6. **Composition over Inheritance**: `ParallelMergeSort` uses `ForkJoinPool` rather than extending it
7. **Strategy Pattern**: Different sorting strategies implement same interface
8. **Adapter Pattern**: Wrapper classes adapt Java built-ins to `SortAlgorithm` interface
