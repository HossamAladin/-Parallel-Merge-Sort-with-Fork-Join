## Parallel Merge Sort with Fork/Join (Java)

This project implements and compares:

- **Sequential merge sort** (custom).
- **Parallel merge sort** using Java’s **Fork/Join** framework.
- **Java built‑in sorts**: `Arrays.sort` and `Arrays.parallelSort`.

It also includes:

- A **benchmark driver** (`SortBenchmark`) to compare performance.
- A **Swing GUI** (`SortGUI`) to interactively run sorts and view a **time vs size** chart.
- A **correctness test runner** (`SortCorrectnessTests`) that checks edge cases and verifies **parallel == sequential** output.

All source files are plain `.java` files using the `package algorithms;` declaration.

---

### 1) How to Compile

From the project folder (where the `.java` files are):

**PowerShell (recommended):**

```bash
javac -d . (Get-ChildItem -File -Filter '*.java' | ForEach-Object Name)
```

This compiles classes into the `algorithms/` folder.

---

### 2) How to Run the Benchmark (Console)

```bash
java algorithms.SortBenchmark
```

Notes:
- Tests **multiple sizes** and **two patterns** (Random + Reverse).
- Prints average time over multiple runs.
- Validates that each algorithm output is sorted (throws an error if not).

---

### 3) Correctness Tests (Edge Cases + Parallel == Sequential)

```bash
java algorithms.SortCorrectnessTests
```

If everything is correct, it prints:

```text
ALL CORRECTNESS TESTS PASSED
```

---

### 4) GUI

```bash
java algorithms.SortGUI
```

The GUI includes:
- **Log tab**: prints algorithm, pattern, size, time, sorted flag, and before/after previews.
- **Performance Chart tab**: plots **time (ms)** vs **array size (n)** with labeled axes and ticks.

---

### 5) Git Notes

Do NOT commit build artifacts:
- `algorithms/` (compiled `.class` files)
- `*.class`


