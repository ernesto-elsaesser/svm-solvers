package sample;

import java.awt.*;
import java.io.FileReader;
import java.util.List;
import javax.swing.*;
import com.opencsv.CSVReader;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.DefaultXYDataset;

public class Main {

    public static void main(String[] args) {

        JFrame f = new JFrame();

        JLabel label = new JLabel("Loading ...");
        label.setBounds(160,150, 80,40);
        f.add(label);

        f.setSize(400,400);
        f.setLayout(null);
        f.setVisible(true);

        try {
            double[][] training = parse("data/svmguide1.csv");
            double[][] testing = parse("data/svmguide1-t.csv");
            // TODO: apply SVM
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getLocalizedMessage());
        }

        JFreeChart chart = createChart();
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(400, 400));
        f.setContentPane(panel);
        //SwingUtilities.updateComponentTreeUI(f);
    }

    private static double[][] parse(String filename) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(filename), ' ');
        List<String[]> lines = reader.readAll();
        int num = lines.size();
        double[][] values = new double[num][4];
        for (int i = 0; i < num; i++) {
            String[] line = lines.get(i);
            values[i][0] = parseValue(line[1]);
            values[i][1] = parseValue(line[2]);
            values[i][2] = parseValue(line[3]);
            values[i][3] = parseValue(line[4]);
        }
        return values;
    }

    private static double parseValue(String s) {
        String[] parts = s.split(":");
        return Double.valueOf(parts[1]);
    }

    private static JFreeChart createChart() {
        DefaultXYDataset dataset = lineDataset(1.0, 0.0, 50.0);
        return ChartFactory.createXYLineChart("TheChart", "X",
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
    }

    private static DefaultXYDataset lineDataset(double a, double b, double maxX) {
        int res = 100;
        double data[][] = new double[2][res];
        double step = maxX / ((double) 100);
        for(int i = 0; i < res; i++) {
            double x = i * step;
            data[0][i] = x;
            data[1][i] = a * x + b;
        }
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("TheLine", data);
        return dataset;
    }
}
