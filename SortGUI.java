package algorithms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

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
        frame.setSize(800, 600);
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

        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

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
            outputArea.append("Time    : " + durationMs + " ms\n");
            outputArea.append("Sorted  : " + sorted + "\n");
            outputArea.append("Before (first " + previewLength + "): " + Arrays.toString(beforePreview) + "\n");
            outputArea.append("After  (first " + previewLength + "): " + Arrays.toString(afterPreview) + "\n");
            outputArea.append("------------------------------------------------------------\n");
        });

        frame.setVisible(true);
    }
}


