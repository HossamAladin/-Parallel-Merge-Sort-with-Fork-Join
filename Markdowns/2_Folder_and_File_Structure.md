# 2. Folder and File Structure

## Overview

The project consists of **7 Java source files**, **3 documentation files**, and **1 configuration file**. All Java files belong to the `algorithms` package. This section provides a comprehensive analysis of each file's purpose, role, and importance.

---

## Java Source Files

### 1. SortAlgorithm.java

**File Type**: Interface  
**Lines of Code**: 5  
**Package**: `algorithms`

#### Role

Defines the contract that all sorting algorithm implementations must follow. It declares a single method: `void sort(int[] array)`.

#### Why It Exists

**Design Pattern**: This is the **Strategy Pattern** - it allows different sorting algorithms to be used interchangeably without changing client code.

**Benefits**:
- **Polymorphism**: The benchmark and GUI can treat all algorithms uniformly
- **Extensibility**: New sorting algorithms can be added without modifying existing code
- **Testability**: All algorithms can be tested using the same test harness
- **Separation of concerns**: Algorithm implementation is separated from algorithm usage

**Example**:
```java
SortAlgorithm algorithm = new ParallelMergeSort();
algorithm.sort(myArray);  // Works for any implementation
```

#### What Would Break If Removed

**Everything would break**. This interface is the foundation of the entire project:

1. **SortBenchmark** would fail to compile - it uses `SortAlgorithm` as the type for algorithm arrays
2. **SortGUI** would fail to compile - it uses `SortAlgorithm` to store the selected algorithm
3. **All algorithm classes** would lose their common type, requiring separate code paths for each
4. **Extensibility** would be lost - adding new algorithms would require modifying multiple files

**Alternative**: Without the interface, you would need separate methods like:
```java
benchmarkSequential(SequentialMergeSort seq, int[] array)
benchmarkParallel(ParallelMergeSort par, int[] array)
benchmarkArraysSort(ArraysSortAlgorithm as, int[] array)
// ... code duplication for each algorithm
```

#### What Would Change If Removed

If you removed the interface and used concrete classes directly:
- **Code duplication**: Benchmark and GUI would need separate code paths for each algorithm
- **Tight coupling**: Client code would depend on specific implementations
- **Maintenance burden**: Adding a new algorithm would require changes in multiple places
- **Loss of abstraction**: The concept of "a sorting algorithm" would not exist as a type

---

### 2. SequentialMergeSort.java

**File Type**: Class (implements `SortAlgorithm`)  
**Lines of Code**: 62  
**Package**: `algorithms`

#### Role

Implements the classic sequential merge sort algorithm. This is the **baseline** against which parallel performance is measured.

#### Why It Exists

**Primary purposes**:

1. **Performance baseline**: Provides a reference point to measure parallel speedup
2. **Correctness reference**: The parallel version must produce identical output to this version
3. **Educational value**: Demonstrates the classic divide-and-conquer algorithm
4. **Small-array optimization**: For small datasets, this is actually faster than parallel versions

#### Key Responsibilities

1. **Edge case handling**:
   - Returns immediately for null, empty, or single-element arrays
   - Checks if array is already sorted (optimization)

2. **Recursive divide-and-conquer**:
   - Splits array into halves
   - Recursively sorts each half
   - Merges sorted halves

3. **In-place merging**:
   - Uses a temporary buffer to avoid overwriting data
   - Implements stable sorting (preserves order of equal elements)

#### Why This Implementation

**Design decisions**:

1. **Single temp array**: Allocated once and reused for all recursive calls (memory efficient)
2. **isSorted() check**: Early exit optimization for already-sorted arrays
3. **Stable sort**: Uses `<=` in merge to preserve relative order of equal elements
4. **Overflow-safe midpoint**: Calculates `mid = left + (right - left) / 2` to avoid integer overflow

#### What Would Break If Removed

1. **No performance baseline**: Cannot measure parallel speedup without sequential reference
2. **Correctness tests fail**: `SortCorrectnessTests` compares parallel output to sequential output
3. **Benchmarks incomplete**: Cannot compare parallel vs sequential performance
4. **Small-array performance**: For arrays < 50,000 elements, this is the fastest algorithm

#### What Would Change If Removed

If you removed this class:
- **Benchmarks** would only compare parallel algorithms (less meaningful)
- **Correctness tests** would need a different reference (e.g., Arrays.sort)
- **Educational value** would decrease (no clear sequential vs parallel comparison)
- **Project completeness** would suffer (missing the fundamental algorithm)

---

### 3. ParallelMergeSort.java

**File Type**: Class (implements `SortAlgorithm`)  
**Lines of Code**: 132  
**Package**: `algorithms`

#### Role

Implements parallel merge sort using Java's Fork/Join framework. This is the **core contribution** of the project - demonstrating how to parallelize a divide-and-conquer algorithm.

#### Why It Exists

**Primary purposes**:

1. **Demonstrate parallelism**: Shows how to use Fork/Join framework for parallel sorting
2. **Performance improvement**: Achieves 3-4× speedup on large arrays compared to sequential
3. **Educational value**: Illustrates parallel algorithm design, task decomposition, and threshold selection
4. **Practical application**: Provides a usable parallel sorting implementation

#### Key Components

**Outer class (ParallelMergeSort)**:
- Manages the threshold parameter
- Holds reference to ForkJoinPool
- Provides the public `sort()` method
- Creates the root task and invokes it

**Inner class (MergeSortTask)**:
- Extends `RecursiveAction` (Fork/Join task with no return value)
- Represents sorting a subarray `[left..right]`
- Implements the parallel divide-and-conquer logic
- Contains static helper methods for sequential sorting and merging

#### Why This Design

**Design decisions**:

1. **Threshold parameter**: Controls when to switch from parallel to sequential
   - Too small → excessive task overhead
   - Too large → insufficient parallelism
   - Default 10,000 provides good balance

2. **ForkJoinPool.commonPool()**: Uses shared thread pool instead of creating new threads
   - More efficient (thread reuse)
   - Automatically sized to CPU core count
   - Shared across all parallel operations in JVM

3. **RecursiveAction**: Chosen over RecursiveTask because sorting is in-place (no return value needed)

4. **invokeAll()**: Executes both subtasks in parallel
   - Current thread may execute one task
   - Other threads can "steal" the other task
   - Blocks until both complete

5. **Static inner class**: MergeSortTask doesn't need reference to outer class instance (more memory efficient)

6. **Shared temp array**: All tasks share one temporary buffer (memory efficient, safe because tasks have non-overlapping ranges)

#### What Would Break If Removed

1. **No parallel implementation**: The entire point of the project is lost
2. **No speedup demonstration**: Cannot show performance improvement from parallelism
3. **Benchmarks incomplete**: Only sequential and built-in algorithms remain
4. **Educational value lost**: No demonstration of Fork/Join framework usage

#### What Would Change If Removed

If you removed this class:
- **Project becomes trivial**: Just comparing sequential to Java built-ins
- **No custom parallelism**: No demonstration of parallel programming skills
- **Benchmarks less interesting**: Only comparing to black-box Java implementations
- **Learning objectives unmet**: Cannot study parallel algorithm design

---

### 4. SortBenchmark.java

**File Type**: Class (executable with `main()`)  
**Lines of Code**: 147  
**Package**: `algorithms`

#### Role

Provides automated performance benchmarking of all four sorting algorithms across multiple array sizes and input patterns.

#### Why It Exists

**Primary purposes**:

1. **Performance measurement**: Quantifies execution time for each algorithm
2. **Fair comparison**: Ensures all algorithms sort identical input arrays
3. **Statistical validity**: Runs multiple trials and averages results
4. **Correctness validation**: Verifies each algorithm produces sorted output
5. **Data generation**: Provides standardized test inputs (random, reverse-sorted)

#### Key Responsibilities

1. **Test configuration**:
   - Array sizes: 100,000, 500,000, 1,000,000
   - Patterns: Random, Reverse
   - Runs per case: 5 (for averaging)

2. **Algorithm instantiation**:
   - Creates instances of all four algorithms
   - Configures parallel threshold (10,000 for large arrays)

3. **Benchmarking**:
   - Clones input array for each run (fairness)
   - Measures time using `System.nanoTime()` (high precision)
   - Validates output is sorted (correctness)
   - Averages multiple runs (statistical validity)

4. **Data generation**:
   - `generateRandomArray()`: Creates arrays with random integers
   - `generateReverseSortedArray()`: Creates descending-order arrays
   - `isSorted()`: Validates array is in non-decreasing order

5. **Result reporting**:
   - Prints results in organized format
   - Shows size, pattern, threshold, and time for each algorithm

#### Why This Implementation

**Design decisions**:

1. **Multiple sizes**: Shows how performance scales with data size
2. **Multiple patterns**: Tests different algorithm behaviors (random vs structured)
3. **Multiple runs**: Reduces impact of JVM warmup, garbage collection, OS scheduling
4. **Array cloning**: Ensures fair comparison (all algorithms sort identical data)
5. **Correctness checks**: Throws exception if output is not sorted (catches bugs early)
6. **Wrapper classes**: `ArraysSortAlgorithm` and `ArraysParallelSortAlgorithm` adapt Java built-ins to `SortAlgorithm` interface

#### What Would Break If Removed

1. **No automated testing**: Would need manual testing for each configuration
2. **No performance data**: Cannot generate the results tables in the report
3. **No statistical validity**: Single runs are unreliable due to JVM/OS noise
4. **No correctness validation**: Bugs might go undetected

#### What Would Change If Removed

If you removed this class:
- **Manual benchmarking**: Would need to write timing code for each test case
- **No standardized results**: Different test runs might use different inputs
- **Reduced credibility**: No systematic performance comparison
- **More work**: Would need to manually run and record results for each configuration

---

### 5. SortCorrectnessTests.java

**File Type**: Class (executable with `main()`)  
**Lines of Code**: 158  
**Package**: `algorithms`

#### Role

Provides comprehensive correctness testing for all sorting algorithms, with special focus on verifying that the parallel implementation produces identical results to the sequential version.

#### Why It Exists

**Primary purposes**:

1. **Edge case validation**: Tests empty arrays, single-element arrays, small arrays
2. **Parallel correctness**: Verifies parallel output exactly matches sequential output
3. **Duplicate handling**: Ensures stable sorting (equal elements maintain order)
4. **Extreme value testing**: Tests with `Integer.MIN_VALUE` and `Integer.MAX_VALUE`
5. **Pattern testing**: Tests various input patterns (sorted, reverse, random)
6. **Regression prevention**: Catches bugs introduced by code changes

#### Key Test Categories

**1. Edge cases**:
- Empty array (length = 0)
- Single-element array (length = 1)
- Two-element array requiring swap

**2. Small specific cases**:
- Already sorted arrays
- Reverse-sorted arrays
- Arrays with duplicates
- Arrays with negative numbers
- Arrays with extreme values (MIN_VALUE, MAX_VALUE)

**3. Parallel equals sequential**:
- Many random inputs (sizes 0-1000, 50 trials each)
- Reverse-sorted inputs (sizes 0-1000)
- Validates identical output (not just both sorted)

#### Why This Implementation

**Design decisions**:

1. **No external dependencies**: Uses custom assertions instead of JUnit (simpler deployment)
2. **Deterministic random seed**: `new Random(12345)` ensures reproducible tests
3. **Quiet execution**: Suppresses SequentialMergeSort's informational messages during tests
4. **Strict equality check**: Uses `Arrays.equals()` to verify parallel output exactly matches sequential
5. **Comprehensive coverage**: Tests many sizes and patterns to catch edge cases

#### What Would Break If Removed

1. **No correctness guarantee**: Bugs in parallel implementation might go undetected
2. **No edge case validation**: Empty/small arrays might not be handled correctly
3. **No regression testing**: Code changes might introduce bugs without detection
4. **Reduced confidence**: Cannot prove parallel implementation is correct

#### What Would Change If Removed

If you removed this class:
- **Manual testing**: Would need to manually verify correctness for each case
- **Bug risk**: Parallel implementation bugs might reach production
- **Reduced credibility**: No proof that algorithms are correct
- **Debugging difficulty**: Bugs would be discovered during benchmarking (harder to diagnose)

---

### 6. SortGUI.java

**File Type**: Class (executable with `main()`, Swing GUI)  
**Lines of Code**: 348  
**Package**: `algorithms`

#### Role

Provides an interactive graphical user interface for experimenting with sorting algorithms and visualizing performance.

#### Why It Exists

**Primary purposes**:

1. **Interactive experimentation**: Users can try different configurations without recompiling
2. **Visual feedback**: Shows before/after array previews and performance chart
3. **Educational tool**: Helps understand algorithm behavior and performance characteristics
4. **Threshold tuning**: Allows experimenting with different parallel thresholds
5. **User-friendly**: Non-programmers can explore the algorithms

#### Key Components

**Control panel**:
- Algorithm selection (dropdown)
- Array size input (text field)
- Pattern selection (random/reverse)
- Threshold input (for parallel algorithm)
- Run button

**Log tab**:
- Shows algorithm name, pattern, size, time, sorted status
- Displays first 20 elements before and after sorting
- Scrollable history of all runs

**Performance chart tab**:
- Plots time (ms) vs array size (n)
- Color-coded by algorithm
- Labeled axes with tick marks
- Legend showing algorithm colors
- Accumulates data from all runs

#### Why This Implementation

**Design decisions**:

1. **Swing framework**: Standard Java GUI toolkit (no external dependencies)
2. **Tabbed interface**: Separates log output from chart (cleaner UI)
3. **GridBagLayout**: Flexible control panel layout
4. **Custom chart panel**: Simple time-vs-size plot without external charting libraries
5. **Input validation**: Checks for positive integers, shows error dialogs
6. **Array preview**: Shows first 20 elements (enough to see patterns without clutter)
7. **Accumulating chart**: Keeps all data points from session (shows trends)

#### What Would Break If Removed

1. **No interactive exploration**: Users must recompile to change parameters
2. **No visual feedback**: Cannot see performance trends graphically
3. **Reduced accessibility**: Non-programmers cannot use the project
4. **Harder threshold tuning**: Must edit code and recompile to test different thresholds

#### What Would Change If Removed

If you removed this class:
- **Command-line only**: Must use SortBenchmark (less user-friendly)
- **No visualization**: Cannot see performance chart
- **Less educational**: Harder to explore algorithm behavior interactively
- **Reduced appeal**: Project is less impressive without GUI

---

## Documentation Files

### 7. README.md

**File Type**: Markdown documentation  
**Lines of Code**: 79

#### Role

Provides quick-start instructions for compiling, running, and understanding the project.

#### Why It Exists

**Primary purposes**:

1. **Onboarding**: New users can get started quickly
2. **Compilation instructions**: Shows how to compile all Java files
3. **Execution instructions**: Shows how to run each component
4. **Project overview**: Brief description of what the project does
5. **Git guidance**: Explains what not to commit

#### What Would Break If Removed

Nothing would break technically, but:
- **Usability suffers**: Users wouldn't know how to compile or run the project
- **Onboarding friction**: New users would struggle to get started
- **Professional appearance**: Projects without READMEs look incomplete

---

### 8. AlgorithmBreakdown.md

**File Type**: Markdown documentation  
**Lines of Code**: 709

#### Role

Provides comprehensive technical documentation of all algorithms, including implementation details, performance analysis, and TA question preparation.

#### Why It Exists

**Primary purposes**:

1. **Deep technical reference**: Explains how each algorithm works
2. **Performance analysis**: Analyzes benchmark results in detail
3. **Design decisions**: Explains why choices were made
4. **TA preparation**: Provides answers to likely discussion questions
5. **Educational resource**: Teaches parallel algorithm design

#### What Would Break If Removed

Nothing breaks technically, but:
- **Understanding suffers**: Users wouldn't understand design decisions
- **Reduced educational value**: Less learning from the project
- **Harder TA discussions**: No reference for answering questions

---

### 9. Report.md

**File Type**: Markdown documentation (formal report)  
**Lines of Code**: 157

#### Role

Provides formal academic report documenting the project, methodology, and results.

#### Why It Exists

**Primary purposes**:

1. **Academic documentation**: Formal write-up for course submission
2. **Methodology description**: Explains how benchmarks were conducted
3. **Results presentation**: Shows performance tables and graphs
4. **Reproducibility**: Provides instructions to reproduce results

#### What Would Break If Removed

Nothing breaks technically, but:
- **No formal documentation**: Project lacks academic write-up
- **Results not documented**: Performance data not formally presented
- **Reduced credibility**: No systematic documentation of findings

---

### 10. sorting_results_comparison.md

**File Type**: Markdown documentation (results data)  
**Lines of Code**: 207

#### Role

Contains detailed benchmark results including individual run times and averages.

#### Why It Exists

**Primary purposes**:

1. **Raw data**: Preserves all benchmark measurements
2. **Statistical transparency**: Shows individual runs, not just averages
3. **Variance analysis**: Allows examining run-to-run variation
4. **Result verification**: Others can verify calculations

#### What Would Break If Removed

Nothing breaks technically, but:
- **No result record**: Would need to re-run benchmarks
- **No transparency**: Cannot verify result accuracy
- **Reduced credibility**: No detailed data to support claims

---

## Configuration Files

### 11. .gitignore

**File Type**: Git configuration  
**Lines of Code**: ~10

#### Role

Tells Git which files to ignore (not commit to version control).

#### Why It Exists

**Primary purposes**:

1. **Exclude build artifacts**: `.class` files, `algorithms/` folder
2. **Exclude IDE files**: `.idea/`, `.vscode/`, etc.
3. **Exclude OS files**: `.DS_Store`, `Thumbs.db`
4. **Keep repository clean**: Only source files, not generated files

#### What Would Break If Removed

Nothing breaks technically, but:
- **Repository pollution**: Build artifacts would be committed
- **Merge conflicts**: Different users' compiled files would conflict
- **Repository size**: Unnecessary files increase clone time

---

## Summary Table

| File | Type | LOC | Role | Critical? |
|------|------|-----|------|-----------|
| SortAlgorithm.java | Interface | 5 | Common contract | ✓✓✓ |
| SequentialMergeSort.java | Class | 62 | Baseline algorithm | ✓✓✓ |
| ParallelMergeSort.java | Class | 132 | Core parallel algorithm | ✓✓✓ |
| SortBenchmark.java | Class | 147 | Performance testing | ✓✓ |
| SortCorrectnessTests.java | Class | 158 | Correctness validation | ✓✓ |
| SortGUI.java | Class | 348 | Interactive interface | ✓ |
| README.md | Docs | 79 | Quick start guide | ✓ |
| AlgorithmBreakdown.md | Docs | 709 | Technical reference | ✓ |
| Report.md | Docs | 157 | Formal report | ✓ |
| sorting_results_comparison.md | Docs | 207 | Benchmark data | ✓ |
| .gitignore | Config | ~10 | Git configuration | ✓ |

**Legend**:
- ✓✓✓ = Critical (project fails without it)
- ✓✓ = Important (major functionality lost without it)
- ✓ = Useful (enhances project but not essential)

---

## File Dependencies

```
SortAlgorithm.java (interface)
    ├── SequentialMergeSort.java (implements)
    ├── ParallelMergeSort.java (implements)
    ├── SortBenchmark.java (uses)
    │   ├── ArraysSortAlgorithm (inner class, implements)
    │   └── ArraysParallelSortAlgorithm (inner class, implements)
    ├── SortCorrectnessTests.java (uses)
    └── SortGUI.java (uses)

SortBenchmark.java
    └── Used by: SortGUI.java (calls utility methods)

Documentation files (independent, no code dependencies)
```

---

## Architectural Principles

The file structure demonstrates several software engineering principles:

1. **Interface-based design**: `SortAlgorithm` interface enables polymorphism
2. **Separation of concerns**: Each class has a single, well-defined responsibility
3. **Testability**: Separate classes for benchmarking and correctness testing
4. **Extensibility**: New algorithms can be added without modifying existing code
5. **Reusability**: Utility methods in `SortBenchmark` are reused by `SortGUI`
6. **Documentation**: Comprehensive docs for users, developers, and academic submission
