# 7. Execution and Data Flow

This section provides a step-by-step walkthrough of program execution from start to finish, explaining data movement between threads/processes and where/why synchronization happens.

---

## Overview

The project has three main execution paths:

1. **SortBenchmark** - Automated performance testing
2. **SortCorrectnessTests** - Automated correctness validation
3. **SortGUI** - Interactive user interface

This section focuses on the **parallel merge sort execution**, which is the core of the project.

---

## Execution Path 1: Parallel Merge Sort (Detailed Walkthrough)

### Step 1: Program Start

**Entry point**: User calls `ParallelMergeSort.sort(array)`

```java
ParallelMergeSort sorter = new ParallelMergeSort(10_000);
int[] array = {5, 2, 8, 1, 9, 3, 7, 4, 6};
sorter.sort(array);
```

**What happens**:
1. Constructor creates `ParallelMergeSort` instance
2. Stores threshold (10,000)
3. Gets reference to `ForkJoinPool.commonPool()`

**State**:
- `array`: Unsorted input array
- `threshold`: 10,000
- `pool`: Reference to shared thread pool (8 threads on 8-core system)

---

### Step 2: Sort Method Entry

**Code**:
```java
public void sort(int[] array) {
    if (array == null || array.length <= 1) {
        return;  // Edge case
    }

    int[] temp = new int[array.length];  // Allocate temp buffer
    MergeSortTask rootTask = new MergeSortTask(array, temp, 0, array.length - 1, threshold);
    pool.invoke(rootTask);  // Submit and wait
}
```

**What happens**:
1. Check edge cases (null, empty, single element)
2. Allocate temporary buffer (same size as array)
3. Create root task representing entire array
4. Submit task to pool and block until complete

**Data state**:
- `array`: `[5, 2, 8, 1, 9, 3, 7, 4, 6]` (unsorted)
- `temp`: `[0, 0, 0, 0, 0, 0, 0, 0, 0]` (allocated but unused)
- `rootTask`: Represents sorting `[0..8]`

**Thread state**:
- Main thread: Blocked in `pool.invoke()`, waiting for task completion
- Worker threads: Ready to execute tasks

---

### Step 3: Root Task Execution

**Code**:
```java
protected void compute() {
    int length = right - left + 1;  // 9 elements

    if (length <= threshold) {  // 9 <= 10,000? Yes
        sequentialMergeSort(array, temp, left, right);
        return;
    }
    // ... (not reached for this small example)
}
```

**What happens**:
1. Calculate segment length: `8 - 0 + 1 = 9`
2. Check threshold: `9 <= 10,000` → true
3. Execute sequential merge sort on `[0..8]`

**Why sequential**:
- Array too small (9 elements < 10,000 threshold)
- Overhead of parallelism would exceed benefit
- Sequential is faster for small arrays

**Thread state**:
- Worker thread 1: Executes root task sequentially
- Other worker threads: Idle (no work to steal)

**Data flow**:
- Input: `array = [5, 2, 8, 1, 9, 3, 7, 4, 6]`
- Output: `array = [1, 2, 3, 4, 5, 6, 7, 8, 9]` (sorted)

---

### Step 4: Sequential Merge Sort (Within Parallel Task)

**Code**:
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

**Execution trace** (for `[5, 2, 8, 1, 9, 3, 7, 4, 6]`):

```
sequentialMergeSort([0..8])
├─ mid = 4
├─ sequentialMergeSort([0..4])  // Sort [5, 2, 8, 1, 9]
│   ├─ mid = 2
│   ├─ sequentialMergeSort([0..2])  // Sort [5, 2, 8]
│   │   ├─ mid = 1
│   │   ├─ sequentialMergeSort([0..1])  // Sort [5, 2]
│   │   │   ├─ mid = 0
│   │   │   ├─ sequentialMergeSort([0..0])  // [5] - base case
│   │   │   ├─ sequentialMergeSort([1..1])  // [2] - base case
│   │   │   └─ merge([0..1])  // [2, 5]
│   │   ├─ sequentialMergeSort([2..2])  // [8] - base case
│   │   └─ merge([0..2])  // [2, 5, 8]
│   ├─ sequentialMergeSort([3..4])  // Sort [1, 9]
│   │   ├─ mid = 3
│   │   ├─ sequentialMergeSort([3..3])  // [1] - base case
│   │   ├─ sequentialMergeSort([4..4])  // [9] - base case
│   │   └─ merge([3..4])  // [1, 9]
│   └─ merge([0..4])  // [1, 2, 5, 8, 9]
├─ sequentialMergeSort([5..8])  // Sort [3, 7, 4, 6]
│   ├─ mid = 6
│   ├─ sequentialMergeSort([5..6])  // Sort [3, 7]
│   │   ├─ mid = 5
│   │   ├─ sequentialMergeSort([5..5])  // [3] - base case
│   │   ├─ sequentialMergeSort([6..6])  // [7] - base case
│   │   └─ merge([5..6])  // [3, 7]
│   ├─ sequentialMergeSort([7..8])  // Sort [4, 6]
│   │   ├─ mid = 7
│   │   ├─ sequentialMergeSort([7..7])  // [4] - base case
│   │   ├─ sequentialMergeSort([8..8])  // [6] - base case
│   │   └─ merge([7..8])  // [4, 6]
│   └─ merge([5..8])  // [3, 4, 6, 7]
└─ merge([0..8])  // [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

**Data transformations**:
```
Initial:  [5, 2, 8, 1, 9, 3, 7, 4, 6]
After [0..1]: [2, 5, 8, 1, 9, 3, 7, 4, 6]
After [0..2]: [2, 5, 8, 1, 9, 3, 7, 4, 6]
After [3..4]: [2, 5, 8, 1, 9, 3, 7, 4, 6]
After [0..4]: [1, 2, 5, 8, 9, 3, 7, 4, 6]
After [5..6]: [1, 2, 5, 8, 9, 3, 7, 4, 6]
After [7..8]: [1, 2, 5, 8, 9, 3, 7, 4, 6]
After [5..8]: [1, 2, 5, 8, 9, 3, 4, 6, 7]
After [0..8]: [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

---

### Step 5: Task Completion and Return

**What happens**:
1. Sequential merge sort completes
2. Root task's `compute()` returns
3. `pool.invoke()` unblocks
4. Main thread resumes execution

**Final state**:
- `array`: `[1, 2, 3, 4, 5, 6, 7, 8, 9]` (sorted)
- `temp`: `[...]` (used during merges, no longer needed)
- Main thread: Continues execution after `sort()` call

---

## Execution Path 2: Parallel Merge Sort (Large Array)

Now let's trace execution for a **large array** (1,000,000 elements) where parallelism actually occurs.

### Step 1: Program Start (Large Array)

```java
ParallelMergeSort sorter = new ParallelMergeSort(10_000);
int[] array = new int[1_000_000];  // 1M elements
// ... fill with random data ...
sorter.sort(array);
```

**State**:
- `array`: 1M unsorted elements
- `threshold`: 10,000
- `pool`: 8 worker threads (on 8-core system)

---

### Step 2: Root Task Creation and Submission

```java
int[] temp = new int[1_000_000];  // 1M element buffer
MergeSortTask rootTask = new MergeSortTask(array, temp, 0, 999_999, 10_000);
pool.invoke(rootTask);
```

**What happens**:
1. Allocate 1M element temp buffer
2. Create root task for `[0..999999]`
3. Submit to pool
4. Main thread blocks

**Thread state**:
- Main thread: Blocked
- Worker thread 1: Picks up root task, starts executing

---

### Step 3: Root Task Splits (Level 1)

**Code execution** (Worker thread 1):
```java
protected void compute() {
    int length = 999_999 - 0 + 1;  // 1,000,000

    if (length <= 10_000) {  // 1,000,000 <= 10,000? No
        // Not reached
    }

    int mid = 0 + (999_999 - 0) / 2;  // 499,999
    MergeSortTask leftTask = new MergeSortTask(array, temp, 0, 499_999, 10_000);
    MergeSortTask rightTask = new MergeSortTask(array, temp, 500_000, 999_999, 10_000);

    invokeAll(leftTask, rightTask);  // Execute in parallel
    
    // ... (waits here until both complete)
}
```

**What happens**:
1. Check threshold: `1,000,000 > 10,000` → split
2. Calculate midpoint: `499,999`
3. Create left task: `[0..499999]` (500K elements)
4. Create right task: `[500000..999999]` (500K elements)
5. Call `invokeAll()` → submits both tasks

**Thread state**:
- Worker thread 1: Blocked in `invokeAll()`, waiting for both tasks
- Worker thread 2: Steals `rightTask`, starts executing
- Worker thread 1: Executes `leftTask` directly (after submitting)

**Data partitioning**:
```
Array: [0...................499999|500000...................999999]
       └─ Left task (500K)        └─ Right task (500K)
```

---

### Step 4: Parallel Execution (Level 2)

**Worker thread 1** executes left task `[0..499999]`:
```java
int length = 499_999 - 0 + 1;  // 500,000
if (length <= 10_000) {  // No
    // Split again
}
int mid = 0 + (499_999 - 0) / 2;  // 249,999
MergeSortTask leftTask = new MergeSortTask(array, temp, 0, 249_999, 10_000);
MergeSortTask rightTask = new MergeSortTask(array, temp, 250_000, 499_999, 10_000);
invokeAll(leftTask, rightTask);
```

**Worker thread 2** executes right task `[500000..999999]`:
```java
int length = 999_999 - 500_000 + 1;  // 500,000
if (length <= 10_000) {  // No
    // Split again
}
int mid = 500_000 + (999_999 - 500_000) / 2;  // 749,999
MergeSortTask leftTask = new MergeSortTask(array, temp, 500_000, 749_999, 10_000);
MergeSortTask rightTask = new MergeSortTask(array, temp, 750_000, 999_999, 10_000);
invokeAll(leftTask, rightTask);
```

**Thread state** (now 4 tasks active):
- Worker thread 1: Blocked, waiting for `[0..249999]` and `[250000..499999]`
- Worker thread 2: Blocked, waiting for `[500000..749999]` and `[750000..999999]`
- Worker thread 3: Steals `[250000..499999]`, starts executing
- Worker thread 4: Steals `[750000..999999]`, starts executing
- Worker thread 1: Executes `[0..249999]` directly
- Worker thread 2: Executes `[500000..749999]` directly

**Data partitioning**:
```
Array: [0........249999|250000........499999|500000........749999|750000........999999]
       └─ T1 (250K)    └─ T3 (250K)         └─ T2 (250K)         └─ T4 (250K)
```

---

### Step 5: Continued Splitting (Levels 3-6)

**Process continues recursively**:
- Each task splits into two smaller tasks
- More worker threads join (up to 8 threads on 8-core system)
- Splitting continues until segments reach threshold (10,000)

**Level 3**: 8 tasks (125K elements each)
**Level 4**: 16 tasks (62.5K elements each)
**Level 5**: 32 tasks (31.25K elements each)
**Level 6**: 64 tasks (15.625K elements each)
**Level 7**: 128 tasks (7.8K elements each) → **Below threshold, switch to sequential**

**Final task distribution**:
- ~100 leaf tasks (each ~10K elements)
- All tasks execute sequentially (below threshold)
- Up to 8 tasks run in parallel at any time (limited by 8 cores)

---

### Step 6: Sequential Sorting (Leaf Tasks)

**What happens**:
- Each leaf task sorts its ~10K element segment sequentially
- Uses standard recursive merge sort
- No further parallelism (below threshold)

**Example** (one leaf task):
```java
sequentialMergeSort(array, temp, 0, 9_999);  // Sort first 10K elements
```

**Thread state**:
- All 8 worker threads busy sorting different segments
- Work-stealing keeps all threads busy
- Idle threads steal tasks from busy threads' queues

**Data flow**:
- Each thread reads/writes its own segment
- No conflicts (segments don't overlap)
- No synchronization needed

---

### Step 7: Merging (Bottom-Up)

**After leaf tasks complete**, merging begins bottom-up:

**Level 7 → Level 6**: Merge pairs of 7.8K segments into 15.625K segments
```java
// Worker thread 1 (after sorting [0..7812] and [7813..15624]):
merge(array, temp, 0, 7812, 15624);  // Merge into [0..15624]
```

**Level 6 → Level 5**: Merge pairs of 15.625K segments into 31.25K segments
**Level 5 → Level 4**: Merge pairs of 31.25K segments into 62.5K segments
**Level 4 → Level 3**: Merge pairs of 62.5K segments into 125K segments
**Level 3 → Level 2**: Merge pairs of 125K segments into 250K segments
**Level 2 → Level 1**: Merge pairs of 250K segments into 500K segments
**Level 1 → Level 0**: Merge pairs of 500K segments into 1M segment (final merge)

**Synchronization**:
- Each merge waits for both child tasks to complete
- `invokeAll()` provides implicit barrier
- Parent task resumes after both children done

---

### Step 8: Final Merge and Completion

**Final merge** (Worker thread 1):
```java
// After both [0..499999] and [500000..999999] are sorted:
merge(array, temp, 0, 499_999, 999_999);  // Merge entire array
```

**What happens**:
1. Copy entire array to temp buffer
2. Merge two sorted halves (500K elements each)
3. Write merged result back to array
4. Root task's `compute()` returns
5. `pool.invoke()` unblocks
6. Main thread resumes

**Final state**:
- `array`: 1M elements, fully sorted
- All worker threads: Return to idle state
- Main thread: Continues execution

---

## Data Movement Between Threads

### Shared Data Structures

**1. Array (shared, read-write)**
- **Type**: `int[] array`
- **Access pattern**: Each thread reads/writes its own segment
- **Synchronization**: None needed (non-overlapping segments)
- **Visibility**: Java Memory Model ensures visibility after `invokeAll()`

**2. Temp buffer (shared, read-write)**
- **Type**: `int[] temp`
- **Access pattern**: Each thread reads/writes its own segment during merge
- **Synchronization**: None needed (non-overlapping segments)
- **Visibility**: Automatic (happens-before relationship)

**3. Task objects (shared, read-only after creation)**
- **Type**: `MergeSortTask`
- **Fields**: `array`, `temp`, `left`, `right`, `threshold` (all final)
- **Access pattern**: Read-only after construction
- **Synchronization**: None needed (immutable)

### Data Flow Example (4-core system, 40K elements)

**Initial state**:
```
Array: [unsorted 40K elements]
Temp:  [empty 40K elements]
```

**Level 1** (1 task):
```
Thread 1: Sort [0..39999]
  ├─ Split into [0..19999] and [20000..39999]
  └─ Submit both tasks
```

**Level 2** (2 tasks, parallel):
```
Thread 1: Sort [0..19999]
  ├─ Split into [0..9999] and [10000..19999]
  └─ Submit both tasks
  
Thread 2: Sort [20000..39999]
  ├─ Split into [20000..29999] and [30000..39999]
  └─ Submit both tasks
```

**Level 3** (4 tasks, parallel):
```
Thread 1: Sort [0..9999] sequentially (below threshold)
Thread 2: Sort [10000..19999] sequentially
Thread 3: Sort [20000..29999] sequentially
Thread 4: Sort [30000..39999] sequentially
```

**Data after Level 3**:
```
Array: [sorted 0..9999][sorted 10000..19999][sorted 20000..29999][sorted 30000..39999]
```

**Level 2 merging**:
```
Thread 1: Merge [0..9999] and [10000..19999] → [0..19999]
  ├─ Copy [0..19999] to temp
  ├─ Merge from temp back to array
  
Thread 2: Merge [20000..29999] and [30000..39999] → [20000..39999]
  ├─ Copy [20000..39999] to temp
  ├─ Merge from temp back to array
```

**Data after Level 2**:
```
Array: [sorted 0..19999][sorted 20000..39999]
```

**Level 1 merging**:
```
Thread 1: Merge [0..19999] and [20000..39999] → [0..39999]
  ├─ Copy [0..39999] to temp
  ├─ Merge from temp back to array
```

**Final data**:
```
Array: [sorted 0..39999]
```

---

## Synchronization Points

### Synchronization Point 1: invokeAll()

**Location**: Every task split

**Purpose**: Wait for both child tasks to complete before merging

**Code**:
```java
invokeAll(leftTask, rightTask);  // Barrier: Wait for both
merge(array, temp, left, mid, right);  // Safe: Both halves sorted
```

**What happens**:
1. Current thread submits both tasks
2. Current thread may execute one task directly
3. Current thread blocks until both tasks complete
4. After both complete, current thread resumes
5. Merge proceeds (safe because both halves sorted)

**Why needed**:
- Merge depends on both halves being sorted
- Must wait for both before merging
- Prevents race condition (reading unsorted data)

### Synchronization Point 2: pool.invoke()

**Location**: Root task submission

**Purpose**: Wait for entire sort to complete

**Code**:
```java
pool.invoke(rootTask);  // Blocks until rootTask completes
// Array is sorted here
```

**What happens**:
1. Main thread submits root task
2. Main thread blocks
3. Worker threads execute task tree
4. After all tasks complete, main thread unblocks
5. Main thread continues execution

**Why needed**:
- Caller expects array to be sorted when `sort()` returns
- Must wait for all parallel work to complete
- Ensures correctness (array fully sorted)

### Synchronization Point 3: Happens-Before Relationships

**Java Memory Model guarantees**:
- Changes made by child tasks visible to parent after `invokeAll()` returns
- No explicit synchronization (locks, volatile) needed
- Automatic memory visibility

**Example**:
```java
// Child task (Thread 2)
array[100] = 42;  // Write

// Parent task (Thread 1)
invokeAll(leftTask, rightTask);  // Happens-before edge
int value = array[100];  // Read (guaranteed to see 42)
```

---

## Where and Why Synchronization Happens

### 1. Task Submission (invokeAll)

**Where**: Every task split in `compute()`

**Why**: Must wait for both child tasks before merging

**Mechanism**: `invokeAll()` blocks until both tasks complete

**Cost**: Minimal (efficient implementation in ForkJoinPool)

### 2. Root Task Completion (pool.invoke)

**Where**: Main thread in `sort()` method

**Why**: Caller expects sorted array when method returns

**Mechanism**: `pool.invoke()` blocks until root task completes

**Cost**: Minimal (just waiting, no active work)

### 3. Memory Visibility (Implicit)

**Where**: All task boundaries

**Why**: Child task writes must be visible to parent

**Mechanism**: Java Memory Model happens-before guarantees

**Cost**: None (automatic, no explicit synchronization)

### 4. Work-Stealing (Internal)

**Where**: ForkJoinPool internal queues

**Why**: Threads steal tasks from each other

**Mechanism**: Atomic operations on work queues

**Cost**: Minimal (optimized by JVM)

---

## No Synchronization Needed

### Why No Locks?

**Reason 1: Disjoint access**
- Each task operates on non-overlapping array segments
- No two tasks modify the same array indices
- No conflicts possible

**Example**:
```
Thread 1: Modifies array[0..499999]
Thread 2: Modifies array[500000..999999]
No overlap → No conflict → No lock needed
```

**Reason 2: No shared mutable state**
- Task fields are final (immutable after construction)
- No shared counters, flags, or variables
- Each task is independent

**Reason 3: Implicit synchronization**
- `invokeAll()` provides necessary synchronization
- Java Memory Model ensures visibility
- No manual synchronization needed

---

## Summary

**Execution flow**:
1. Main thread creates root task and submits to pool
2. Root task splits into child tasks recursively
3. Leaf tasks sort small segments sequentially
4. Results merged bottom-up
5. Main thread unblocks when complete

**Data movement**:
- Each thread operates on its own array segment
- No data copying between threads (shared array)
- Temp buffer used for merging (also segmented)

**Synchronization**:
- `invokeAll()` waits for child tasks (implicit barrier)
- `pool.invoke()` waits for root task (main thread blocks)
- Java Memory Model ensures visibility (no explicit sync)

**Key insights**:
1. **Parallelism is recursive**: Each task may spawn more tasks
2. **Work-stealing balances load**: Idle threads steal work
3. **Synchronization is implicit**: Framework handles coordination
4. **No locks needed**: Disjoint access prevents conflicts
5. **Threshold controls granularity**: Prevents excessive overhead

Understanding this execution flow is essential for:
- Debugging parallel code (knowing which thread does what)
- Optimizing performance (understanding bottlenecks)
- Reasoning about correctness (ensuring no race conditions)
- Designing parallel algorithms (applying similar patterns)
