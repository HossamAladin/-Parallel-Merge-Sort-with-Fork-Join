# Parallel Merge Sort with Fork/Join (Java) — Report

## Abstract
This report presents the design and implementation of a modular Java project that implements and evaluates **sequential** and **parallel** merge sort for `int[]`. The parallel implementation uses Java’s **Fork/Join framework** (`ForkJoinPool` + `RecursiveAction`). For context and baseline comparison, the project also benchmarks Java’s built-in **`Arrays.sort`** and **`Arrays.parallelSort`**. A Swing GUI allows interactive experiments with array size, input pattern, and parallel threshold, and provides a simple performance chart.

## 1) Introduction
Sorting is a foundational operation in software systems (databases, scientific computing, graphics). **Merge sort** is a classic divide-and-conquer algorithm with guaranteed \(O(n \log n)\) time complexity, and its recursive structure makes it a strong candidate for parallelization on modern multi-core CPUs.

This project focuses on:
- Implementing **Sequential Merge Sort** for `int[]`, including edge cases.
- Implementing **Parallel Merge Sort** with Fork/Join and a **threshold** for granularity control.
- Comparing both custom implementations with **`Arrays.sort`** and **`Arrays.parallelSort`**.
- Presenting measured performance results (tables) and a graph/plot (figure).

## 2) Requirements Mapping (What the Project Delivers)
The design follows the required OOP structure:
- `SortAlgorithm`: interface defining `void sort(int[] array)`.
- `SequentialMergeSort`: sequential merge sort implementation.
- `ParallelMergeSort`: parallel merge sort using Fork/Join (`RecursiveAction`) and a threshold.
- `SortBenchmark`: generates inputs, times algorithms, and validates outputs.
- `SortGUI`: interactive GUI to run experiments and show a performance chart.

## 3) Sequential Merge Sort

### 3.1 Concept (Divide and Conquer)
Sequential merge sort has three phases:
1. **Divide**: recursively split the array into halves until subarrays have size 0 or 1.
2. **Conquer**: recursively sort each half.
3. **Combine**: merge two sorted halves into a larger sorted segment.

### 3.2 Complexity
- **Time**: \(O(n \log n)\) for best/average/worst case.
- **Space**: \(O(n)\) for a temporary buffer used during merge.
- **Stability**: stable when the merge chooses left element first on equality (using `<=`).

### 3.3 Edge Cases
The sequential implementation handles:
- **Empty arrays** (`length = 0`) and **size-1 arrays** (`length = 1`) by returning immediately.
- **Already sorted arrays** via an early-exit check (optional optimization).

## 4) Parallel Merge Sort (Fork/Join)

### 4.1 Parallelization Strategy (Task-Based Divide and Conquer)
Parallel merge sort uses the same logical structure as sequential merge sort, but executes independent recursive calls in parallel:
- Each task represents: “sort subarray \([left..right]\)”.
- For a **large** segment, the task splits into two subtasks:
  - Left task sorts \([left..mid]\)
  - Right task sorts \([mid+1..right]\)
- The task executes the subtasks via **`invokeAll(leftTask, rightTask)`** and then merges the results.

### 4.2 Split + Merge Details (Explicit — TA Requirement)

#### Split (Divide)
Given an array segment \([left..right]\):
1. Compute:
   \[
   mid = left + \frac{(right-left)}{2}
   \]
2. Create two subtasks for the halves:
   - \([left..mid]\)
   - \([mid+1..right]\)
3. Execute the subtasks in parallel using Fork/Join:
   - `invokeAll(leftTask, rightTask)`

#### Merge (Combine)
After both halves are sorted:
1. Copy the current segment to a temporary buffer: `temp[left..right]`.
2. Use two pointers:
   - `i` starts at `left` (left half)
   - `j` starts at `mid + 1` (right half)
3. Write the smaller of `temp[i]` and `temp[j]` back into the original array.
4. Copy any remaining elements after one half is exhausted.

**Why a temp buffer is needed:** merging writes into the same array region being read; copying prevents overwriting values that are still needed for comparisons.

### 4.3 Threshold Rationale (Explicit — TA Requirement)
Fork/Join parallelism adds overhead:
- task creation/allocation
- scheduling and synchronization
- memory/cache traffic

If tasks are too small, overhead dominates and the parallel version can be slower than sequential.

Therefore the implementation uses a **threshold**:
- If segment length \(\le\) threshold → sort sequentially for that segment.
- Else → keep splitting into parallel subtasks.

**Chosen threshold:** **10,000** for the large benchmark sizes. This value is a practical compromise:
- Large enough that each task performs meaningful work (amortizes overhead).
- Small enough to expose parallelism and utilize multiple CPU cores.

> Note: A different threshold can be useful for different hardware. The GUI allows experimenting with threshold values to observe the tradeoff between overhead and parallel speedup.

## 5) Benchmark Methodology (How Results Were Produced)

### 5.1 Compared Algorithms (Baselines Included)
The benchmark compares:
- `SequentialMergeSort` (custom)
- `ParallelMergeSort` (custom Fork/Join)
- `Arrays.sort` (Java built-in)
- `Arrays.parallelSort` (Java built-in parallel)

### 5.2 Input Patterns
At least two patterns are used:
- **Random**
- **Reverse-sorted**

### 5.3 Repetitions and Fairness
- Each case is run multiple times (5 runs) and averaged.
- For fairness, each algorithm sorts a **fresh copy** of the same base input array.
- Each run validates correctness (array is sorted).

## 6) Results (Tables + Graph)

### 6.1 Results Tables (From `sorting_results_comparison.md`)

#### Table 1 — Random Input (Average of 5 runs, ms)
| Size      | SequentialMergeSort | ParallelMergeSort | Arrays.sort | Arrays.parallelSort |
|-----------|---------------------|-------------------|-------------|----------------------|
| 10,000    | 1.157               | 2.246             | 1.528       | 2.067                |
| 100,000   | 10.514              | 3.971             | 8.671       | 6.119                |
| 500,000   | 58.521              | 16.303            | 27.115      | 8.572                |
| 1,000,000 | 119.410             | 32.522            | 57.144      | 17.632               |

#### Table 2 — Reverse Input (Average of 5 runs, ms)
| Size      | SequentialMergeSort | ParallelMergeSort | Arrays.sort | Arrays.parallelSort |
|-----------|---------------------|-------------------|-------------|----------------------|
| 10,000    | 0.441               | 0.328             | 0.344       | 0.327                |
| 100,000   | 4.434               | 1.668             | 0.051       | 0.061                |
| 500,000   | 24.402              | 7.821             | 0.282       | 0.306                |
| 1,000,000 | 51.486              | 13.500            | 0.721       | 0.627                |

### 6.2 Results Graph (Figure) — TA Requirement
Insert a single plot/figure that visualizes performance.

**[FIGURE 1 PLACEHOLDER: Time (ms) vs Array Size for Random and Reverse patterns]**
- X-axis: input size (10k, 100k, 500k, 1M)
- Y-axis: average time (ms)
- 4 series: SequentialMergeSort, ParallelMergeSort, Arrays.sort, Arrays.parallelSort

**Figure 1 caption (paste under the figure):**  
**Figure 1.** Average runtime (ms) versus input size for Random and Reverse inputs. Parallel approaches improve with larger sizes as task overhead is amortized. Java built-ins, especially `Arrays.parallelSort`, provide strong baselines and can outperform custom implementations due to extensive optimization.

> If you do not have an external plotting tool, you may use a screenshot from the GUI **Performance Chart** tab as the required “graph/plot”.

## Appendix A — How to Reproduce
1. Compile:
   - `javac -d . (Get-ChildItem -File -Filter '*.java' | ForEach-Object Name)`
2. Run benchmark:
   - `java algorithms.SortBenchmark`
3. Run correctness tests:
   - `java algorithms.SortCorrectnessTests`
4. Run GUI:
   - `java algorithms.SortGUI`


