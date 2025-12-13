# 6. Parallel Constructs Breakdown

This section provides exhaustive analysis of every parallel construct used in the project, explaining what each does, why it's needed, what happens if removed, and common mistakes to avoid.

---

## Overview of Parallel Constructs Used

The project uses **Java's Fork/Join framework** for parallelism. Specifically:

1. **ForkJoinPool** - Thread pool for executing parallel tasks
2. **RecursiveAction** - Base class for tasks that don't return values
3. **invokeAll()** - Method to execute multiple tasks in parallel
4. **fork()** and **join()** - Alternative methods for task execution (not used directly, but related to invokeAll)

**Not used** (but worth understanding):
- Explicit threads (`Thread` class)
- Locks/mutexes (`synchronized`, `ReentrantLock`)
- Atomic variables (`AtomicInteger`, etc.)
- Barriers (`CyclicBarrier`, `CountDownLatch`)
- Semaphores (`Semaphore`)

---

## Construct 1: ForkJoinPool

### What It Does

`ForkJoinPool` is a specialized thread pool designed for executing Fork/Join tasks. It manages a pool of worker threads that execute tasks using a work-stealing algorithm.

**Key features**:
1. **Work-stealing scheduler**: Idle threads steal tasks from busy threads
2. **Managed thread pool**: Automatically creates and manages threads
3. **Optimal sizing**: Defaults to number of CPU cores
4. **Shared pool**: `commonPool()` provides JVM-wide shared instance

### Usage in Project

```java
public class ParallelMergeSort implements SortAlgorithm {
    private final ForkJoinPool pool;
    
    public ParallelMergeSort(int threshold) {
        this.pool = ForkJoinPool.commonPool();  // Get shared pool
    }
    
    @Override
    public void sort(int[] array) {
        MergeSortTask rootTask = new MergeSortTask(array, temp, 0, array.length - 1, threshold);
        pool.invoke(rootTask);  // Submit task and wait for completion
    }
}
```

### Why It's Needed Here

**Purpose**: Provides infrastructure for parallel task execution

**Specific needs**:
1. **Thread management**: Creates and manages worker threads
2. **Task scheduling**: Decides which thread executes which task
3. **Work-stealing**: Balances load across threads
4. **Synchronization**: Coordinates task completion

**Without ForkJoinPool**:
- Would need manual thread creation and management
- No automatic load balancing
- Complex synchronization code
- Higher overhead

### What Happens If Removed

**If removed entirely**:
```java
// This would not compile
pool.invoke(rootTask);  // Error: pool doesn't exist
```

**If replaced with sequential execution**:
```java
// No pool, just call compute directly
rootTask.compute();  // Sequential execution
```
- **Impact**: Loses all parallelism
- **Performance**: 3-4× slower (becomes sequential merge sort)
- **Correctness**: Still correct, just slower

**If replaced with manual threads**:
```java
// Manual thread creation
Thread t1 = new Thread(() -> sortLeftHalf());
Thread t2 = new Thread(() -> sortRightHalf());
t1.start(); t2.start();
t1.join(); t2.join();
```
- **Problems**:
  - Thread creation overhead (expensive)
  - No work-stealing (poor load balancing)
  - Doesn't scale to many tasks (100+ threads would be wasteful)
  - More complex code

### Common Mistakes

**Mistake 1: Creating new pool for each sort**
```java
// BAD: Creates new pool every time
public void sort(int[] array) {
    ForkJoinPool pool = new ForkJoinPool();  // Expensive!
    pool.invoke(rootTask);
    pool.shutdown();  // Must shut down
}
```
- **Problem**: Thread creation overhead (hundreds of milliseconds)
- **Fix**: Reuse pool (store as instance variable or use commonPool)

**Mistake 2: Not shutting down custom pool**
```java
// BAD: Pool never shut down
ForkJoinPool pool = new ForkJoinPool();
pool.invoke(rootTask);
// Threads keep running, wasting resources
```
- **Problem**: Threads remain alive, preventing JVM exit
- **Fix**: Call `pool.shutdown()` when done, or use `commonPool()`

**Mistake 3: Blocking operations in tasks**
```java
// BAD: Blocking I/O in ForkJoinPool
protected void compute() {
    // This blocks a worker thread!
    String data = readFromNetwork();  // Blocking I/O
    processData(data);
}
```
- **Problem**: Blocks worker thread, reduces parallelism
- **Fix**: Use separate thread pool for I/O, or use non-blocking I/O

**Mistake 4: Wrong pool size**
```java
// BAD: Too many threads
ForkJoinPool pool = new ForkJoinPool(1000);  // Way too many!
```
- **Problem**: Excessive context switching, memory waste
- **Fix**: Use default (CPU core count) or commonPool

---

## Construct 2: RecursiveAction

### What It Does

`RecursiveAction` is an abstract class for Fork/Join tasks that don't return values (void). It provides the framework for recursive task decomposition.

**Key features**:
1. **Abstract compute() method**: Subclasses implement task logic
2. **fork() method**: Pushes task to work queue for parallel execution
3. **join() method**: Waits for task completion
4. **invokeAll() method**: Executes multiple tasks in parallel

### Usage in Project

```java
private static class MergeSortTask extends RecursiveAction {
    private final int[] array;
    private final int[] temp;
    private final int left;
    private final int right;
    private final int threshold;
    
    @Override
    protected void compute() {
        // Task logic here
        if (length <= threshold) {
            sequentialMergeSort(array, temp, left, right);
        } else {
            MergeSortTask leftTask = new MergeSortTask(...);
            MergeSortTask rightTask = new MergeSortTask(...);
            invokeAll(leftTask, rightTask);  // Parallel execution
            merge(array, temp, left, mid, right);
        }
    }
}
```

### Why It's Needed Here

**Purpose**: Encapsulates a unit of parallel work (sorting a segment)

**Specific needs**:
1. **Task representation**: Each task represents sorting `[left..right]`
2. **Recursive decomposition**: Tasks create subtasks
3. **No return value**: Sorting is in-place (void)
4. **Framework integration**: Works with ForkJoinPool

**Why RecursiveAction vs RecursiveTask**:
- `RecursiveAction`: For void tasks (no return value)
- `RecursiveTask<V>`: For tasks that return value of type V
- Sorting is in-place → no return value → RecursiveAction

### What Happens If Removed

**Cannot remove** - RecursiveAction is the foundation of Fork/Join parallelism.

**If replaced with RecursiveTask**:
```java
private static class MergeSortTask extends RecursiveTask<Void> {
    @Override
    protected Void compute() {
        // Same logic
        return null;  // Must return something
    }
}
```
- **Works**: Functionally equivalent
- **Awkward**: Must return null (unnecessary)
- **Less clear**: RecursiveAction better expresses intent (void task)

**If replaced with Runnable**:
```java
private static class MergeSortTask implements Runnable {
    @Override
    public void run() {
        // Cannot use invokeAll(), fork(), join()
        // Must manually manage threads
    }
}
```
- **Problems**:
  - Loses Fork/Join framework benefits
  - No work-stealing
  - Manual synchronization needed
  - More complex code

### Common Mistakes

**Mistake 1: Not calling compute() in recursive case**
```java
// BAD: Creates tasks but doesn't execute them
protected void compute() {
    MergeSortTask leftTask = new MergeSortTask(...);
    MergeSortTask rightTask = new MergeSortTask(...);
    // Forgot to call invokeAll()!
    merge(array, temp, left, mid, right);  // Merging unsorted data!
}
```
- **Problem**: Tasks created but not executed, array not sorted
- **Fix**: Call `invokeAll(leftTask, rightTask)`

**Mistake 2: Forgetting base case**
```java
// BAD: Infinite recursion
protected void compute() {
    // No base case check!
    MergeSortTask leftTask = new MergeSortTask(...);
    MergeSortTask rightTask = new MergeSortTask(...);
    invokeAll(leftTask, rightTask);
    merge(...);
}
```
- **Problem**: Infinite recursion, stack overflow
- **Fix**: Add base case (`if (length <= threshold) { ... }`)

**Mistake 3: Accessing shared mutable state**
```java
// BAD: Race condition
private static int counter = 0;  // Shared mutable state

protected void compute() {
    counter++;  // Not thread-safe!
    // ...
}
```
- **Problem**: Race condition, incorrect results
- **Fix**: Avoid shared mutable state, or use synchronization

**Mistake 4: Blocking in compute()**
```java
// BAD: Blocking operation
protected void compute() {
    synchronized (someLock) {  // Blocking!
        // Critical section
    }
}
```
- **Problem**: Blocks worker thread, reduces parallelism
- **Fix**: Avoid blocking operations, use non-blocking algorithms

---

## Construct 3: invokeAll()

### What It Does

`invokeAll()` is a method that executes multiple tasks in parallel and waits for all to complete.

**Signature**:
```java
public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2)
public static void invokeAll(ForkJoinTask<?>... tasks)
```

**Behavior**:
1. Submits all tasks to the pool
2. Current thread may execute one task directly
3. Other tasks available for work-stealing
4. Blocks until all tasks complete
5. Returns when all tasks done

### Usage in Project

```java
protected void compute() {
    int mid = left + (right - left) / 2;
    MergeSortTask leftTask = new MergeSortTask(array, temp, left, mid, threshold);
    MergeSortTask rightTask = new MergeSortTask(array, temp, mid + 1, right, threshold);
    
    invokeAll(leftTask, rightTask);  // Execute both in parallel
    
    merge(array, temp, left, mid, right);  // Merge after both complete
}
```

### Why It's Needed Here

**Purpose**: Executes left and right subtasks in parallel and waits for both

**Specific needs**:
1. **Parallel execution**: Both halves sorted simultaneously
2. **Synchronization**: Must wait for both before merging
3. **Work-stealing**: Other threads can steal tasks
4. **Simplicity**: One line instead of fork/join boilerplate

**Data dependency**:
- Merge depends on both halves being sorted
- Must wait for both tasks to complete
- `invokeAll()` provides this guarantee

### What Happens If Removed

**If removed entirely**:
```java
// BAD: Tasks created but not executed
MergeSortTask leftTask = new MergeSortTask(...);
MergeSortTask rightTask = new MergeSortTask(...);
// No invokeAll()!
merge(array, temp, left, mid, right);  // Merging unsorted data!
```
- **Problem**: Array not sorted, incorrect output
- **Impact**: Algorithm is broken

**If replaced with sequential execution**:
```java
// Sequential: Execute one after another
leftTask.compute();   // Sort left half
rightTask.compute();  // Sort right half
merge(array, temp, left, mid, right);
```
- **Impact**: Loses parallelism, becomes sequential
- **Performance**: 3-4× slower
- **Correctness**: Still correct, just slower

**If replaced with fork/join**:
```java
// Alternative: Explicit fork/join
leftTask.fork();      // Push left task to queue
rightTask.compute();  // Execute right task directly
leftTask.join();      // Wait for left task
merge(array, temp, left, mid, right);
```
- **Impact**: Same effect, more verbose
- **Performance**: Identical to invokeAll()
- **Readability**: invokeAll() is cleaner

### Common Mistakes

**Mistake 1: Not waiting for completion**
```java
// BAD: Start tasks but don't wait
leftTask.fork();
rightTask.fork();
// Immediately merge without waiting!
merge(array, temp, left, mid, right);  // Wrong: tasks not done yet
```
- **Problem**: Race condition, merging unsorted data
- **Fix**: Use `invokeAll()` or call `join()` on both tasks

**Mistake 2: Wrong order of fork/join**
```java
// BAD: Fork both, then join both
leftTask.fork();
rightTask.fork();
leftTask.join();
rightTask.join();
```
- **Problem**: Current thread idle while both tasks execute elsewhere (inefficient)
- **Fix**: Execute one task directly:
  ```java
  leftTask.fork();
  rightTask.compute();  // Execute directly
  leftTask.join();
  ```
- **Better**: Use `invokeAll()` (handles this automatically)

**Mistake 3: Calling invokeAll() on already-started tasks**
```java
// BAD: Fork then invokeAll
leftTask.fork();
invokeAll(leftTask, rightTask);  // leftTask already started!
```
- **Problem**: Undefined behavior, may cause errors
- **Fix**: Use invokeAll() OR fork/join, not both

**Mistake 4: Forgetting to merge after invokeAll()**
```java
// BAD: Parallel sort but no merge
invokeAll(leftTask, rightTask);
// Forgot to merge!
// Array has two sorted halves but not merged
```
- **Problem**: Array not fully sorted
- **Fix**: Always merge after invokeAll()

---

## Construct 4: ForkJoinPool.commonPool()

### What It Does

`ForkJoinPool.commonPool()` returns a shared, JVM-wide ForkJoinPool instance.

**Key features**:
1. **Shared**: One pool for entire JVM
2. **Automatic sizing**: Thread count = CPU cores
3. **Lazy initialization**: Created on first use
4. **No shutdown needed**: Managed by JVM

### Usage in Project

```java
public ParallelMergeSort(int threshold) {
    this.pool = ForkJoinPool.commonPool();  // Get shared pool
}
```

### Why It's Needed Here

**Purpose**: Provides thread pool without creating new threads

**Specific needs**:
1. **Efficiency**: Reuses existing threads
2. **Simplicity**: No pool management needed
3. **Optimal sizing**: Automatically matches CPU cores
4. **No shutdown**: JVM handles lifecycle

**Alternative (custom pool)**:
```java
this.pool = new ForkJoinPool();  // Create new pool
```
- **Problems**:
  - Must call `shutdown()` when done
  - Wastes resources (duplicate thread pools)
  - More complex lifecycle management

### What Happens If Removed

**If not using commonPool**:
```java
// Must create and manage custom pool
private ForkJoinPool pool;

public ParallelMergeSort(int threshold) {
    this.pool = new ForkJoinPool();  // Create pool
}

public void close() {
    pool.shutdown();  // Must shut down
}
```
- **Impact**: More complex, must manage pool lifecycle
- **Risk**: Forgetting to shutdown causes resource leak

### Common Mistakes

**Mistake 1: Shutting down commonPool**
```java
// BAD: Never shut down commonPool!
ForkJoinPool pool = ForkJoinPool.commonPool();
pool.invoke(task);
pool.shutdown();  // DON'T DO THIS!
```
- **Problem**: Shuts down shared pool, breaks other code using it
- **Fix**: Never shut down commonPool (JVM manages it)

**Mistake 2: Assuming specific thread count**
```java
// BAD: Assuming 8 threads
ForkJoinPool pool = ForkJoinPool.commonPool();
// Assumes pool has 8 threads (may have 2, 4, 16, etc.)
```
- **Problem**: Code breaks on systems with different core counts
- **Fix**: Don't assume thread count, let work-stealing handle it

---

## Construct 5: Work-Stealing (Implicit)

### What It Does

Work-stealing is an algorithm where idle threads "steal" tasks from busy threads' work queues.

**How it works**:
1. Each worker thread has a deque (double-ended queue) of tasks
2. Thread pushes new tasks to its own deque
3. Thread pops tasks from its own deque (LIFO - stack-like)
4. Idle thread steals tasks from other threads' deques (FIFO - queue-like)
5. Stealing happens at opposite end (reduces contention)

**Key benefits**:
1. **Automatic load balancing**: Idle threads find work
2. **Cache efficiency**: Thread works on own tasks first (better cache locality)
3. **Reduced contention**: Stealing from opposite end minimizes conflicts

### Usage in Project

**Implicit** - work-stealing happens automatically when using ForkJoinPool.

**Example scenario** (8-core system, sorting 1M elements):
```
Thread 1: Working on [0..499999]
  ├─ Pushes [0..249999] to own queue
  ├─ Pushes [250000..499999] to own queue
  ├─ Starts working on [250000..499999]
  
Thread 2: Idle
  ├─ Steals [0..249999] from Thread 1's queue
  ├─ Starts working on [0..249999]
  
Thread 3: Idle
  ├─ Steals [0..124999] from Thread 2's queue
  ├─ Starts working on [0..124999]
  
... (continues)
```

### Why It's Needed Here

**Purpose**: Ensures all threads stay busy, maximizing CPU utilization

**Specific needs**:
1. **Load balancing**: Some tasks finish faster than others
2. **Adaptivity**: Handles uneven work distribution
3. **Efficiency**: Minimizes idle time

**Without work-stealing**:
- Static partitioning: Divide array into fixed chunks per thread
- **Problem**: Some threads finish early, sit idle
- **Example**: If one chunk is already sorted (fast), that thread finishes early

### What Happens If Removed

**Cannot remove** - work-stealing is built into ForkJoinPool.

**If using static partitioning instead**:
```java
// Static partitioning (no work-stealing)
int chunkSize = array.length / numThreads;
for (int i = 0; i < numThreads; i++) {
    int start = i * chunkSize;
    int end = (i + 1) * chunkSize;
    threads[i] = new Thread(() -> sort(array, start, end));
    threads[i].start();
}
for (Thread t : threads) {
    t.join();
}
```
- **Problem**: Uneven work distribution
- **Example**: One chunk already sorted (fast), thread finishes early and idles
- **Impact**: Lower CPU utilization, worse performance

### Common Mistakes

**Mistake 1: Creating too many small tasks**
```java
// BAD: No threshold, creates millions of tasks
protected void compute() {
    if (left >= right) return;  // Base case: 1 element
    // Creates task for every element!
    invokeAll(new Task(left, mid), new Task(mid+1, right));
}
```
- **Problem**: Work-stealing overhead dominates (queue operations expensive)
- **Fix**: Use threshold to limit task granularity

**Mistake 2: Unbalanced task sizes**
```java
// BAD: Unbalanced split
int mid = left + 1;  // Left task has 1 element, right has n-1
invokeAll(new Task(left, mid), new Task(mid+1, right));
```
- **Problem**: One task finishes quickly, thread idles
- **Fix**: Split evenly (`mid = left + (right - left) / 2`)

---

## Synchronization Mechanisms

### No Explicit Locks Needed

**Why no locks**:
1. **Disjoint access**: Each task operates on non-overlapping segments
2. **No shared mutable state**: Tasks don't modify shared variables
3. **Implicit synchronization**: `invokeAll()` provides happens-before guarantee

**Memory model guarantees**:
- Changes made by child tasks visible to parent after `invokeAll()` returns
- Java Memory Model ensures visibility without explicit synchronization

**Example** (no race condition):
```java
// Thread 1 sorts [0..499999]
for (int i = 0; i <= 499999; i++) {
    array[i] = ...;  // Modifies left half
}

// Thread 2 sorts [500000..999999]
for (int i = 500000; i <= 999999; i++) {
    array[i] = ...;  // Modifies right half
}

// No conflict: Different indices
```

### Implicit Barriers

**invokeAll() acts as barrier**:
- All tasks must complete before proceeding
- Provides synchronization point
- Ensures data dependencies satisfied

**Example**:
```java
invokeAll(leftTask, rightTask);  // Barrier: Wait for both
merge(array, temp, left, mid, right);  // Safe: Both halves sorted
```

---

## Summary Table

| Construct | Type | Purpose | Removal Impact | Common Mistakes |
|-----------|------|---------|----------------|-----------------|
| ForkJoinPool | Thread pool | Manage worker threads | No parallelism | Creating multiple pools, not shutting down custom pools |
| RecursiveAction | Task class | Encapsulate parallel work | Cannot parallelize | Forgetting base case, accessing shared state |
| invokeAll() | Synchronization | Execute tasks in parallel | No parallelism or race conditions | Not waiting for completion, wrong fork/join order |
| commonPool() | Pool instance | Shared thread pool | Must manage custom pool | Shutting down commonPool, assuming thread count |
| Work-stealing | Algorithm | Load balancing | Poor load balancing | Too many small tasks, unbalanced splits |

---

## Key Takeaways

1. **ForkJoinPool is essential**: Provides infrastructure for parallel execution
2. **RecursiveAction encapsulates tasks**: Each task represents sorting a segment
3. **invokeAll() simplifies synchronization**: One line replaces complex fork/join code
4. **Work-stealing is automatic**: No manual load balancing needed
5. **No explicit locks needed**: Disjoint access patterns prevent conflicts
6. **Threshold prevents overhead**: Limits task granularity
7. **commonPool() is convenient**: Shared pool, no management needed

The parallel constructs work together to provide:
- **Automatic parallelism**: Tasks executed across multiple cores
- **Load balancing**: Work-stealing keeps all threads busy
- **Synchronization**: invokeAll() ensures data dependencies satisfied
- **Simplicity**: Clean code without manual thread management
- **Efficiency**: Optimized by Java team, production-ready

Understanding these constructs is essential for:
- Implementing parallel algorithms correctly
- Avoiding common pitfalls (race conditions, deadlocks)
- Achieving good performance (proper task granularity)
- Debugging parallel code (understanding execution model)
