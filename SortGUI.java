package algorithms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Swing GUI to experiment with different sorting algorithms.
 * <p>
 * Allows choosing:
 * - Algorithm: Sequential merge sort, Parallel merge sort, Arrays.sort, Arrays.parallelSort
 * - Input size
 * - Input pattern: random or reverse-sorted
 * - Threshold for parallel merge sort
 * <p>
 * Displays execution time and a preview of the first elements before and after sorting.
 */
public class SortGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SortGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Parallel Merge Sort Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel algorithmLabel = new JLabel("Algorithm:");
        String[] algorithms = {"Sequential Merge Sort", "Parallel Merge Sort", "Arrays.sort", "Arrays.parallelSort"};
        JComboBox<String> algorithmCombo = new JComboBox<>(algorithms);

        JLabel sizeLabel = new JLabel("Array size:");
        JTextField sizeField = new JTextField("100000", 10);

        JLabel patternLabel = new JLabel("Pattern:");
        String[] patterns = {"Random", "Reverse"};
        JComboBox<String> patternCombo = new JComboBox<>(patterns);

        JLabel thresholdLabel = new JLabel("Parallel threshold:");
        JTextField thresholdField = new JTextField("10000", 10);

        JButton runButton = new JButton("Run Sort");

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        ArrayVisualizationPanel visualizationPanel = new ArrayVisualizationPanel();
        PerformanceChartPanel performanceChartPanel = new PerformanceChartPanel();

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        controlPanel.add(algorithmLabel, gbc);
        gbc.gridx = 1;
        controlPanel.add(algorithmCombo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        controlPanel.add(sizeLabel, gbc);
        gbc.gridx = 1;
        controlPanel.add(sizeField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        controlPanel.add(patternLabel, gbc);
        gbc.gridx = 1;
        controlPanel.add(patternCombo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        controlPanel.add(thresholdLabel, gbc);
        gbc.gridx = 1;
        controlPanel.add(thresholdField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(runButton, gbc);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Log", scrollPane);
        tabbedPane.addTab("Array Visualization", visualizationPanel);
        tabbedPane.addTab("Performance Chart", performanceChartPanel);

        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(tabbedPane, BorderLayout.CENTER);

        runButton.addActionListener((ActionEvent e) -> {
            String algorithmName = (String) algorithmCombo.getSelectedItem();
            String patternName = (String) patternCombo.getSelectedItem();

            int size;
            try {
                size = Integer.parseInt(sizeField.getText().trim());
                if (size <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a positive integer for array size.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int threshold = 10_000;
            if ("Parallel Merge Sort".equals(algorithmName)) {
                try {
                    threshold = Integer.parseInt(thresholdField.getText().trim());
                    if (threshold <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a positive integer for threshold.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int[] original;
            if ("Random".equals(patternName)) {
                original = SortBenchmark.generateRandomArray(size);
            } else {
                original = SortBenchmark.generateReverseSortedArray(size);
            }

            int[] arrayToSort = Arrays.copyOf(original, original.length);
            int[] visualizationArray = Arrays.copyOf(original, original.length);

            SortAlgorithm algorithm;
            if ("Sequential Merge Sort".equals(algorithmName)) {
                algorithm = new SequentialMergeSort();
            } else if ("Parallel Merge Sort".equals(algorithmName)) {
                algorithm = new ParallelMergeSort(threshold);
            } else if ("Arrays.sort".equals(algorithmName)) {
                algorithm = new SortBenchmark.ArraysSortAlgorithm();
            } else { // Arrays.parallelSort
                algorithm = new SortBenchmark.ArraysParallelSortAlgorithm();
            }

            long start = System.nanoTime();
            algorithm.sort(arrayToSort);
            long end = System.nanoTime();

            long durationMs = (end - start) / 1_000_000;
            boolean sorted = SortBenchmark.isSorted(arrayToSort);

            int previewLength = Math.min(20, arrayToSort.length);
            int[] beforePreview = Arrays.copyOf(original, previewLength);
            int[] afterPreview = Arrays.copyOf(arrayToSort, previewLength);

            outputArea.append("Algorithm: " + algorithmName + "\n");
            outputArea.append("Pattern : " + patternName + "\n");
            outputArea.append("Size    : " + size + "\n");
            outputArea.append(String.format("Time    : %.1f ms%n", (double) durationMs));
            outputArea.append("Sorted  : " + sorted + "\n");
            outputArea.append("Before (first " + previewLength + "): " + Arrays.toString(beforePreview) + "\n");
            outputArea.append("After  (first " + previewLength + "): " + Arrays.toString(afterPreview) + "\n");
            outputArea.append("------------------------------------------------------------\n");

            // Update visualization only for reasonably small arrays
            if (size <= 200 && visualizationArray.length > 0) {
                animateMergeSortVisualization(visualizationArray, visualizationPanel);
            } else if (size > 200) {
                visualizationPanel.showMessage("Visualization is only shown for array sizes up to 200 elements.");
            } else {
                visualizationPanel.showMessage("No data to visualize.");
            }

            // Update performance chart with latest measurement
            performanceChartPanel.addPoint(size, (double) durationMs, algorithmName);
        });

        frame.setVisible(true);
    }

    /**
     * Panel that draws the current state of an integer array as vertical bars.
     */
    private static class ArrayVisualizationPanel extends JPanel {

        private int[] array;
        private String message;

        void setArray(int[] array) {
            this.array = array;
            this.message = null;
            repaint();
        }

        void showMessage(String message) {
            this.message = message;
            this.array = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (array == null || array.length == 0) {
                g.setColor(Color.DARK_GRAY);
                String text = (message != null) ? message : "No data to visualize.";
                g.drawString(text, 10, getHeight() / 2);
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int n = array.length;

            int barWidth = Math.max(1, width / Math.max(1, n));

            int min = array[0];
            int max = array[0];
            for (int v : array) {
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            }
            double range = max - min;
            if (range == 0) {
                range = 1;
            }

            g.setColor(new Color(220, 220, 220));
            g.fillRect(0, 0, width, height);

            g.setColor(new Color(100, 149, 237));
            for (int i = 0; i < n; i++) {
                double normalized = (array[i] - min) / range;
                int barHeight = (int) (normalized * (height - 20));
                int x = i * barWidth;
                int y = height - barHeight;
                g.fillRect(x, y, barWidth - 1, barHeight);
            }

            g.setColor(Color.DARK_GRAY);
            g.drawString("Array visualization (bars scaled by value)", 10, 15);
        }
    }

    /**
     * Simple panel that plots time vs. array size as points, colored by algorithm.
     */
    private static class PerformanceChartPanel extends JPanel {

        private static class DataPoint {
            final int size;
            final double millis;
            final String algorithm;

            DataPoint(int size, double millis, String algorithm) {
                this.size = size;
                this.millis = millis;
                this.algorithm = algorithm;
            }
        }

        private final List<DataPoint> points = new ArrayList<>();

        void addPoint(int size, double millis, String algorithm) {
            points.add(new DataPoint(size, millis, algorithm));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (points.isEmpty()) {
                g.setColor(Color.DARK_GRAY);
                g.drawString("Run sorts in the GUI to see time vs. size plotted here.", 10, getHeight() / 2);
                return;
            }

            int width = getWidth();
            int height = getHeight();

            int padding = 40;
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;

            int minSize = points.get(0).size;
            int maxSize = points.get(0).size;
            double minTime = points.get(0).millis;
            double maxTime = points.get(0).millis;

            for (DataPoint p : points) {
                if (p.size < minSize) {
                    minSize = p.size;
                }
                if (p.size > maxSize) {
                    maxSize = p.size;
                }
                if (p.millis < minTime) {
                    minTime = p.millis;
                }
                if (p.millis > maxTime) {
                    maxTime = p.millis;
                }
            }

            if (minSize == maxSize) {
                maxSize = minSize + 1;
            }
            if (minTime == maxTime) {
                maxTime = minTime + 1.0;
            }

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            g.setColor(Color.BLACK);
            int x0 = padding;
            int y0 = height - padding;
            int x1 = width - padding;
            int y1 = padding;

            g.drawLine(x0, y0, x1, y0); // X axis
            g.drawLine(x0, y0, x0, y1); // Y axis

            // Axis labels
            g.drawString("Array size", (x0 + x1) / 2 - 20, height - 10);
            g.drawString("Time (ms)", 10, (y0 + y1) / 2);

            // Draw numeric ticks for X (array size)
            int xTicks = 5;
            for (int i = 0; i <= xTicks; i++) {
                double t = i / (double) xTicks;
                int x = x0 + (int) (t * chartWidth);
                int y = y0;
                g.drawLine(x, y - 4, x, y + 4);
                int value = (int) Math.round(minSize + t * (maxSize - minSize));
                String label = Integer.toString(value);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.drawString(label, x - labelWidth / 2, y + 18);
            }

            // Draw numeric ticks for Y (time in ms)
            int yTicks = 5;
            for (int i = 0; i <= yTicks; i++) {
                double t = i / (double) yTicks;
                int x = x0;
                int y = y0 - (int) (t * chartHeight);
                g.drawLine(x - 4, y, x + 4, y);
                double value = minTime + t * (maxTime - minTime);
                String label = String.format("%.1f", value);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.drawString(label, x - labelWidth - 6, y + 4);
            }

            for (DataPoint p : points) {
                double xNorm = (p.size - minSize) / (double) (maxSize - minSize);
                double yNorm = (p.millis - minTime) / (maxTime - minTime);

                int x = x0 + (int) (xNorm * chartWidth);
                int y = y0 - (int) (yNorm * chartHeight);

                g.setColor(colorForAlgorithm(p.algorithm));
                g.fillOval(x - 3, y - 3, 6, 6);
            }

            int legendX = x0 + 10;
            int legendY = y1 + 15;
            int dy = 15;

            String[] algorithms = {"Sequential Merge Sort", "Parallel Merge Sort", "Arrays.sort", "Arrays.parallelSort"};
            for (String name : algorithms) {
                g.setColor(colorForAlgorithm(name));
                g.fillRect(legendX, legendY - 8, 10, 10);
                g.setColor(Color.BLACK);
                g.drawString(name, legendX + 15, legendY);
                legendY += dy;
            }
        }

        private Color colorForAlgorithm(String algorithm) {
            if ("Sequential Merge Sort".equals(algorithm) || "SequentialMergeSort".equals(algorithm)) {
                return new Color(52, 152, 219); // blue
            }
            if ("Parallel Merge Sort".equals(algorithm) || "ParallelMergeSort".equals(algorithm)) {
                return new Color(46, 204, 113); // green
            }
            if ("Arrays.sort".equals(algorithm)) {
                return new Color(231, 76, 60); // red
            }
            if ("Arrays.parallelSort".equals(algorithm)) {
                return new Color(155, 89, 182); // purple
            }
            return Color.DARK_GRAY;
        }
    }

    /**
     * Builds a sequence of intermediate array states corresponding to merges in a
     * sequential merge sort, then plays them back as an animation in the given panel.
     */
    private static void animateMergeSortVisualization(int[] original, ArrayVisualizationPanel panel) {
        if (original == null || original.length == 0) {
            panel.showMessage("No data to visualize.");
            return;
        }

        int[] working = Arrays.copyOf(original, original.length);
        List<int[]> steps = new ArrayList<>();

        int[] temp = new int[working.length];
        captureMergeSortSteps(working, 0, working.length - 1, temp, steps);

        if (steps.isEmpty()) {
            panel.setArray(working);
            return;
        }

        final int[] index = {0};
        panel.setArray(steps.get(0));

        Timer timer = new Timer(80, e -> {
            index[0]++;
            if (index[0] >= steps.size()) {
                ((Timer) e.getSource()).stop();
            } else {
                panel.setArray(steps.get(index[0]));
            }
        });
        timer.start();
    }

    private static void captureMergeSortSteps(int[] array, int left, int right, int[] temp, List<int[]> steps) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            captureMergeSortSteps(array, left, mid, temp, steps);
            captureMergeSortSteps(array, mid + 1, right, temp, steps);
            captureMerge(array, left, mid, right, temp);
            steps.add(Arrays.copyOf(array, array.length));
        }
    }

    private static void captureMerge(int[] array, int left, int mid, int right, int[] temp) {
        System.arraycopy(array, left, temp, left, right - left + 1);

        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            if (temp[i] <= temp[j]) {
                array[k++] = temp[i++];
            } else {
                array[k++] = temp[j++];
            }
        }

        while (i <= mid) {
            array[k++] = temp[i++];
        }

        while (j <= right) {
            array[k++] = temp[j++];
        }
    }
}
