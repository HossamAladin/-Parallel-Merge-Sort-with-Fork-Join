## Parallel Merge Sort with Fork/Join (Java)

This project implements and compares:

- **Sequential merge sort** (custom implementation).
- **Parallel merge sort** using Java’s **Fork/Join** framework.
- **Java built‑in sorts**: `Arrays.sort` and `Arrays.parallelSort`.

It also includes:

- A **benchmark driver** (`SortBenchmark`) to compare performance.
- A **Swing GUI** (`SortGUI`) to interactively run and visualize sorts.

All source files are plain `.java` files using the `package algorithms;` declaration.

---

### Project Structure (Source Files)

All these files live in the project root:

- `SortAlgorithm.java` – interface defining `void sort(int[] array);`.
- `SequentialMergeSort.java` – sequential merge sort implementation.
- `ParallelMergeSort.java` – parallel merge sort using Fork/Join and a threshold.
- `SortBenchmark.java` – console benchmark comparing all algorithms.
- `SortGUI.java` – Swing GUI to run and visualize sorts.
- `Driver.java` – simple manual test/driver (optional, not required to run benchmarks or GUI).

Compiled `.class` files are placed in the `algorithms/` subfolder by `javac -d . *.java` and **are not tracked in Git**.

---

### 1. How to Compile Everything

From your project folder (where the `.java` files are, e.g. `...Project\test` on Windows):

1. **Open PowerShell here**
   - Right‑click in the folder → “Open in Terminal” (or navigate there manually).

2. **Compile all Java files** (run this after any code changes):

   ```bash
   javac -d . *.java
   ```

   This command:

   - Uses the `package algorithms;` line at the top of each file.
   - Puts the compiled `.class` files in an `algorithms` subfolder.

   If there are no errors, you’re ready to run.

---

### 2. How to Run the Benchmark (Console Mode)

From the same project folder (after compilation):

```bash
java algorithms.SortBenchmark
```

You should see output similar to:

```text
=== Sort Benchmark ===
Runs per case: 5

Size = 10000, Pattern = Random
  SequentialMergeSort   : xxx ms
  ParallelMergeSort     : yyy ms
  Arrays.sort           : zzz ms
  Arrays.parallelSort   : www ms

Size = 10000, Pattern = Reverse
  SequentialMergeSort   : ...
  ParallelMergeSort     : ...
  Arrays.sort           : ...
  Arrays.parallelSort   : ...
```

Each line shows the **average runtime** over several runs for that algorithm, size, and pattern.

---

### 3. How to Run and Use the GUI

From the same project folder (after compilation):

```bash
java algorithms.SortGUI
```

#### In the window:

- **Algorithm**: choose one of:
  - `Sequential Merge Sort`
  - `Parallel Merge Sort`
  - `Arrays.sort`
  - `Arrays.parallelSort`
- **Array size**: type, for example, `10000` or `100000`.
- **Pattern**: choose `Random` or `Reverse`.
- **Parallel threshold**:
  - Only matters for `Parallel Merge Sort` (e.g., `10000`).
  - Controls when the parallel algorithm switches to sequential processing.
- Click **“Run Sort”**.

#### How to read the output area:

For each run, it prints:

- **Algorithm** name.
- **Pattern** used.
- **Size** of the array.
- **Time** in milliseconds (`Time : X ms`).
- **Sorted** flag: `true` or `false`.
- **Before**: first 20 elements of the input array (preview).
- **After**: first 20 elements of the sorted array (preview).

For **good results**:

- `Sorted : true`.
- The “After” preview should be in **non-decreasing order** (each element ≥ the previous one).
- If you click “Run Sort” multiple times with the same settings, times should be **roughly consistent**, allowing for normal variation due to other processes on the machine.

---

### 4. GitHub Notes (What to Commit)

You should commit and push:

- All **`.java` source files**.
- This `README.md`.
- Any documentation or report files you create.

You should **not** commit:

- The `algorithms/` folder containing compiled `.class` files.
- Any other `*.class` or build artifacts.

When someone clones the repository, they simply:

1. Run:

   ```bash
   javac -d . *.java
   ```

2. Then use:

   ```bash
   java algorithms.SortBenchmark
   ```

   or

   ```bash
   java algorithms.SortGUI
   ```

and the `algorithms` folder with `.class` files will be created automatically on their machine.


