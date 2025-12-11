## Sorting Algorithms Performance Comparison

This file summarizes the provided experimental results for:
- **Custom algorithms**: `SequentialMergeSort`, `ParallelMergeSort`
- **Java built-ins**: `Arrays.sort`, `Arrays.parallelSort`

All times are **average over 5 runs**, in **milliseconds (ms)**.

---

### 1. Average Time by Size and Pattern (Random Input)

**Table 1 – Random pattern**

| Size      | SequentialMergeSort | ParallelMergeSort | Arrays.sort | Arrays.parallelSort |
|-----------|---------------------|-------------------|-------------|----------------------|
| 10,000    | 1.157               | 2.246             | 1.528       | 2.067                |
| 100,000   | 10.514              | 3.971             | 8.671       | 6.119                |
| 500,000   | 58.521              | 16.303            | 27.115      | 8.572                |
| 1,000,000 | 119.410             | 32.522            | 57.144      | 17.632               |

**Observations (Random pattern):**
- **Small size (10,000)**:  
  - `SequentialMergeSort` is fastest; parallel versions are slightly slower due to **thread creation/synchronization overhead**.  
  - For this size, we explicitly used `ParallelMergeSort` with **threshold = 1,000**, while for sizes ≥100,000 we used **threshold = 10,000**.
- **Medium & large sizes (≥100,000)**:  
  - `ParallelMergeSort` clearly outperforms your `SequentialMergeSort`.  
  - For very large arrays (500,000 and 1,000,000), **`Arrays.parallelSort` is the fastest overall**, beating both your custom algorithms and `Arrays.sort`.
- **Built-ins vs custom**:  
  - For random input, Java built-ins (`Arrays.sort`, `Arrays.parallelSort`) are **very competitive**, especially at larger sizes where `Arrays.parallelSort` dominates.

---

### 2. Average Time by Size and Pattern (Reverse Input)

**Table 2 – Reverse pattern**

| Size      | SequentialMergeSort | ParallelMergeSort | Arrays.sort | Arrays.parallelSort |
|-----------|---------------------|-------------------|-------------|----------------------|
| 10,000    | 0.441               | 0.328             | 0.344       | 0.327                |
| 100,000   | 4.434               | 1.668             | 0.051       | 0.061                |
| 500,000   | 24.402              | 7.821             | 0.282       | 0.306                |
| 1,000,000 | 51.486              | 13.500            | 0.721       | 0.627                |

**Observations (Reverse pattern):**
- **For all sizes**, the **Java built-ins are dramatically faster** than your custom merge sorts on reverse-ordered input.
- `Arrays.sort` and `Arrays.parallelSort` stay **well below 1 ms** even for **1,000,000 elements**, while custom merge sorts take **tens of milliseconds**.
- This suggests that Java’s implementations are **highly optimized** and likely contain **specialized handling** for monotonic patterns (already sorted or reverse-sorted).
- Between the built-ins:  
  - `Arrays.sort` is slightly faster for **100,000 and 500,000 (reverse)**.  
  - `Arrays.parallelSort` is slightly faster at **1,000,000 (reverse)**.

---

### 3. Best Algorithm per Configuration

**Table 3 – Fastest algorithm for each Size & Pattern**

| Size      | Pattern | Fastest Algorithm      | Avg Time (ms) |
|-----------|---------|------------------------|---------------|
| 10,000    | Random  | SequentialMergeSort    | 1.157         |
| 10,000    | Reverse | Arrays.parallelSort    | 0.327         |
| 100,000   | Random  | ParallelMergeSort      | 3.971         |
| 100,000   | Reverse | Arrays.sort            | 0.051         |
| 500,000   | Random  | Arrays.parallelSort    | 8.572         |
| 500,000   | Reverse | Arrays.sort            | 0.282         |
| 1,000,000 | Random  | Arrays.parallelSort    | 17.632        |
| 1,000,000 | Reverse | Arrays.parallelSort    | 0.627         |

**Key points from Table 3:**
- **Parallelization helps as data grows**:  
  - At **10,000 random**, parallelism does **not** pay off; sequential is best.  
  - From **100,000 random and above**, parallel algorithms (`ParallelMergeSort`, `Arrays.parallelSort`) become **significantly faster**.
- **Java built-ins dominate on structured (reverse) input**:  
  - For every reverse case, a **Java built-in** (`Arrays.sort` or `Arrays.parallelSort`) is the fastest.
- **For very large sizes (500,000–1,000,000 random)**:  
  - **`Arrays.parallelSort` is the best choice**, combining Java’s optimized implementation with effective use of multiple cores.

---

### 4. High-Level Algorithm Comparison

- **SequentialMergeSort (your implementation)**:
  - Predictable \(O(n \log n)\) behavior.
  - Competitive at **small sizes random (10,000)**.
  - Becomes **much slower** than parallel versions as size grows, especially for random input.

- **ParallelMergeSort (your implementation)**:
  - Shows clear **speedup over SequentialMergeSort** for random input at **≥100,000 elements**.
  - Overhead makes it **slower on small arrays** (10,000 random).
  - Still noticeably **slower than Java’s built-in parallel sort** for large random arrays.

- **Arrays.sort (Java)**:
  - Very strong **baseline performance**.
  - On reverse input, it is **extremely fast** (e.g., 0.051 ms at 100,000 elements), far ahead of custom algorithms.
  - On large random inputs, it is good but usually **slower than Arrays.parallelSort**.

- **Arrays.parallelSort (Java)**:
  - For **large random arrays**, it is often the **overall best performer**.
  - For **reverse input**, it stays under 1 ms even at 1,000,000 elements, and is usually the fastest or very close.
  - Demonstrates how a **well-tuned parallel algorithm in the standard library** can outperform both custom sequential and custom parallel implementations.

---

### 5. Summary of What We Notice

- **Parallelism is beneficial only beyond a certain size**:  
  - At **10,000 elements (random)**, the **sequential** version is still best.  
  - From **100,000 elements onward**, parallel versions offer **2–4× speedups** over sequential custom merge sort.
- **Java’s standard library is highly optimized**:  
  - Both `Arrays.sort` and `Arrays.parallelSort` are **hard to beat**, especially on **reverse** and **very large random** inputs.
- **Best  choices based on the data**:
  - **Small inputs (≈10,000)**: use **`SequentialMergeSort`** or `Arrays.sort` (differences are small). For this size we tested `ParallelMergeSort` with **threshold = 1,000**, while for all larger sizes we used **threshold = 10,000**.  
  - **Medium to large random inputs (≥100,000)**: prefer **`Arrays.parallelSort`**, then `ParallelMergeSort`.  
  - **Reverse/structured inputs**: always prefer **Java built-ins**, especially `Arrays.sort` / `Arrays.parallelSort`.

---

### 6. Detailed Per-Run Timings (5 Runs + Average)

This section lists the **5 individual runs** for each configuration, along with the **average** (already used in the earlier tables).

#### 6.1 Size = 10,000

These runs use `ParallelMergeSort` with **threshold = 1,000**.

**Pattern: Random**

| Algorithm                         | Run 1  | Run 2  | Run 3  | Run 4  | Run 5  | Average |
|-----------------------------------|--------|--------|--------|--------|--------|---------|
| SequentialMergeSort               | 1.762  | 0.966  | 1.249  | 0.959  | 0.851  | 1.157   |
| ParallelMergeSort (threshold=1000)| 5.309  | 2.674  | 1.058  | 1.046  | 1.144  | 2.246   |
| Arrays.sort                       | 3.392  | 1.221  | 0.734  | 1.139  | 1.155  | 1.528   |
| Arrays.parallelSort               | 2.149  | 1.754  | 2.364  | 1.870  | 2.197  | 2.067   |

**Pattern: Reverse**

| Algorithm                         | Run 1  | Run 2  | Run 3  | Run 4  | Run 5  | Average |
|-----------------------------------|--------|--------|--------|--------|--------|---------|
| SequentialMergeSort               | 0.667  | 0.296  | 0.312  | 0.460  | 0.470  | 0.441   |
| ParallelMergeSort (threshold=1000)| 0.430  | 0.373  | 0.311  | 0.265  | 0.262  | 0.328   |
| Arrays.sort                       | 0.369  | 0.381  | 0.302  | 0.271  | 0.397  | 0.344   |
| Arrays.parallelSort               | 0.247  | 0.421  | 0.255  | 0.420  | 0.294  | 0.327   |

#### 6.2 Size = 100,000

**Pattern: Random**

| Algorithm           | Run 1   | Run 2   | Run 3   | Run 4   | Run 5   | Average |
|---------------------|---------|---------|---------|---------|---------|---------|
| SequentialMergeSort | 11.881  | 9.843   | 9.001   | 10.547  | 11.300  | 10.514  |
| ParallelMergeSort   | 4.009   | 4.253   | 3.771   | 3.753   | 4.066   | 3.971   |
| Arrays.sort         | 7.029   | 8.914   | 10.311  | 8.473   | 8.630   | 8.671   |
| Arrays.parallelSort | 11.335  | 11.459  | 3.690   | 2.207   | 1.906   | 6.119   |

**Pattern: Reverse**

| Algorithm           | Run 1  | Run 2  | Run 3  | Run 4  | Run 5  | Average |
|---------------------|--------|--------|--------|--------|--------|---------|
| SequentialMergeSort | 3.442  | 5.716  | 3.601  | 4.163  | 5.250  | 4.434   |
| ParallelMergeSort   | 1.644  | 1.701  | 1.534  | 1.457  | 2.001  | 1.668   |
| Arrays.sort         | 0.053  | 0.052  | 0.050  | 0.050  | 0.050  | 0.051   |
| Arrays.parallelSort | 0.066  | 0.065  | 0.056  | 0.057  | 0.059  | 0.061   |

#### 6.3 Size = 500,000

**Pattern: Random**

| Algorithm           | Run 1   | Run 2   | Run 3   | Run 4   | Run 5   | Average |
|---------------------|---------|---------|---------|---------|---------|---------|
| SequentialMergeSort | 62.270  | 59.734  | 60.884  | 51.473  | 58.243  | 58.521  |
| ParallelMergeSort   | 16.242  | 15.967  | 16.643  | 16.540  | 16.126  | 16.303  |
| Arrays.sort         | 26.532  | 26.910  | 26.316  | 28.898  | 26.918  | 27.115  |
| Arrays.parallelSort | 8.767   | 8.843   | 8.500   | 8.051   | 8.700   | 8.572   |

**Pattern: Reverse**

| Algorithm           | Run 1   | Run 2   | Run 3   | Run 4   | Run 5   | Average |
|---------------------|---------|---------|---------|---------|---------|---------|
| SequentialMergeSort | 29.430  | 20.131  | 25.238  | 19.542  | 27.669  | 24.402  |
| ParallelMergeSort   | 7.593   | 8.615   | 7.317   | 8.126   | 7.452   | 7.821   |
| Arrays.sort         | 0.309   | 0.286   | 0.285   | 0.275   | 0.255   | 0.282   |
| Arrays.parallelSort | 0.280   | 0.267   | 0.295   | 0.389   | 0.299   | 0.306   |

#### 6.4 Size = 1,000,000

**Pattern: Random**

| Algorithm           | Run 1    | Run 2    | Run 3    | Run 4    | Run 5    | Average |
|---------------------|----------|----------|----------|----------|----------|---------|
| SequentialMergeSort | 120.097  | 108.420  | 129.753  | 121.633  | 117.145  | 119.410 |
| ParallelMergeSort   | 32.672   | 32.303   | 31.658   | 33.572   | 32.404   | 32.522  |
| Arrays.sort         | 57.596   | 56.792   | 57.204   | 56.843   | 57.286   | 57.144  |
| Arrays.parallelSort | 16.164   | 17.912   | 17.567   | 18.299   | 18.219   | 17.632  |

**Pattern: Reverse**

| Algorithm           | Run 1   | Run 2   | Run 3   | Run 4   | Run 5   | Average |
|---------------------|---------|---------|---------|---------|---------|---------|
| SequentialMergeSort | 63.797  | 61.548  | 39.944  | 39.370  | 52.770  | 51.486  |
| ParallelMergeSort   | 13.002  | 13.277  | 15.516  | 12.586  | 13.117  | 13.500  |
| Arrays.sort         | 0.578   | 0.541   | 1.362   | 0.544   | 0.577   | 0.721   |
| Arrays.parallelSort | 0.613   | 0.721   | 0.603   | 0.588   | 0.610   | 0.627   |



