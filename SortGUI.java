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

            // Update performance chart with latest measurement
            performanceChartPanel.addPoint(size, (double) durationMs, algorithmName);
        });

        frame.setVisible(true);
    }

    /**
     * Simple time-vs-size plot shown in the GUI.
     */
    private static class PerformanceChartPanel extends JPanel {
        private static class DataPoint {
            final int size;
            final double timeMs;
            final String algorithm;

            DataPoint(int size, double timeMs, String algorithm) {
                this.size = size;
                this.timeMs = timeMs;
                this.algorithm = algorithm;
            }
        }

        private final List<DataPoint> points = new ArrayList<>();

        void addPoint(int size, double timeMs, String algorithm) {
            points.add(new DataPoint(size, timeMs, algorithm));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (points.isEmpty()) {
                g.drawString("Run sorts in the GUI to see time vs. size plotted here.", 10, getHeight() / 2);
                return;
            }

            int width = getWidth();
            int height = getHeight();

            int paddingLeft = 70;   // room for Y-axis numbers
            int paddingRight = 30;
            int paddingTop = 30;
            int paddingBottom = 60; // room for X-axis numbers + label

            int x0 = paddingLeft;
            int y0 = height - paddingBottom;
            int x1 = width - paddingRight;
            int y1 = paddingTop;

            // Axes
            g.setColor(Color.DARK_GRAY);
            g.drawLine(x0, y0, x1, y0); // x-axis
            g.drawLine(x0, y0, x0, y1); // y-axis

            // Find min/max for scaling
            int minSize = Integer.MAX_VALUE;
            int maxSize = Integer.MIN_VALUE;
            double minTime = Double.POSITIVE_INFINITY;
            double maxTime = Double.NEGATIVE_INFINITY;

            for (DataPoint p : points) {
                minSize = Math.min(minSize, p.size);
                maxSize = Math.max(maxSize, p.size);
                minTime = Math.min(minTime, p.timeMs);
                maxTime = Math.max(maxTime, p.timeMs);
            }

            if (minSize == maxSize) {
                maxSize = minSize + 1;
            }
            if (minTime == maxTime) {
                maxTime = minTime + 1.0;
            }

            // Ticks + numeric labels (make the chart readable)
            drawTicksAndLabels(g, x0, y0, x1, y1, minSize, maxSize, minTime, maxTime);

            // Axis labels
            g.setColor(Color.BLACK);
            g.drawString("Array size (n)", (x0 + x1) / 2 - 35, height - 15);
            g.drawString("Time (ms)", 10, y1 + 10);

            // Plot points (colored by algorithm)
            for (DataPoint p : points) {
                int x = x0 + (int) ((p.size - minSize) * 1.0 * (x1 - x0) / (maxSize - minSize));
                int y = y0 - (int) ((p.timeMs - minTime) * 1.0 * (y0 - y1) / (maxTime - minTime));

                g.setColor(colorForAlgorithm(p.algorithm));
                g.fillOval(x - 4, y - 4, 8, 8);
            }

            // Legend
            int lx = x0 + 10;
            int ly = y1 + 10;
            String[] algs = {"Sequential Merge Sort", "Parallel Merge Sort", "Arrays.sort", "Arrays.parallelSort"};
            for (String a : algs) {
                g.setColor(colorForAlgorithm(a));
                g.fillRect(lx, ly - 8, 10, 10);
                g.setColor(Color.BLACK);
                g.drawString(a, lx + 15, ly);
                ly += 15;
            }
        }

        private void drawTicksAndLabels(
                Graphics g,
                int x0, int y0, int x1, int y1,
                int minSize, int maxSize,
                double minTime, double maxTime
        ) {
            g.setColor(Color.GRAY);

            int xTicks = 5;
            int yTicks = 5;
            int tickLen = 6;

            // X-axis ticks (array size)
            for (int t = 0; t <= xTicks; t++) {
                double frac = t / (double) xTicks;
                int x = x0 + (int) (frac * (x1 - x0));
                g.drawLine(x, y0, x, y0 + tickLen);

                int value = (int) Math.round(minSize + frac * (maxSize - minSize));
                String label = formatSize(value);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.drawString(label, x - labelWidth / 2, y0 + tickLen + 14);
            }

            // Y-axis ticks (time ms)
            for (int t = 0; t <= yTicks; t++) {
                double frac = t / (double) yTicks;
                int y = y0 - (int) (frac * (y0 - y1));
                g.drawLine(x0 - tickLen, y, x0, y);

                double value = minTime + frac * (maxTime - minTime);
                String label = formatMs(value);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.drawString(label, x0 - tickLen - 6 - labelWidth, y + 5);
            }
        }

        private String formatSize(int n) {
            // Use k/M suffixes for readability
            if (n >= 1_000_000) {
                double m = n / 1_000_000.0;
                return (m == Math.floor(m)) ? String.format("%.0fM", m) : String.format("%.1fM", m);
            }
            if (n >= 1_000) {
                double k = n / 1_000.0;
                return (k == Math.floor(k)) ? String.format("%.0fk", k) : String.format("%.1fk", k);
            }
            return Integer.toString(n);
        }

        private String formatMs(double ms) {
            // Keep labels compact
            if (ms >= 10) return String.format("%.0f", ms);
            if (ms >= 1) return String.format("%.1f", ms);
            return String.format("%.2f", ms);
        }

        private Color colorForAlgorithm(String algorithm) {
            if ("Sequential Merge Sort".equals(algorithm)) return new Color(0x1f77b4); // blue
            if ("Parallel Merge Sort".equals(algorithm)) return new Color(0xff7f0e);  // orange
            if ("Arrays.sort".equals(algorithm)) return new Color(0x2ca02c);          // green
            if ("Arrays.parallelSort".equals(algorithm)) return new Color(0xd62728);  // red
            return Color.GRAY;
        }
    }
}
