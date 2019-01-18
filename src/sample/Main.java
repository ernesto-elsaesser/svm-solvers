package sample;

import java.awt.*;
import java.io.FileReader;
import java.util.List;
import javax.swing.*;
import com.opencsv.CSVReader;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class Main {

    public static void main(String[] args) {

        JFrame f = new JFrame();

        JLabel label = new JLabel("Loading ...");
        label.setBounds(260,250, 80,40);
        f.add(label);

        f.setSize(600,600);
        f.setLayout(null);
        f.setVisible(true);

        try {
            double[][] training = parse("data/svmguide1.csv");
            double[][] testing = parse("data/svmguide1-t.csv");
            // TODO: apply SVM
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getLocalizedMessage());
        }

        double[][] points = {{4.0,2.0},{2.0,5.0},{3.0,8.0}};
        int[] classes = {-1, -1, 1};
        double[] alphas = {-8.42,-13.65,0.68};
        JFreeChart chart = createChart(points, classes, alphas);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        f.setContentPane(panel);
        SwingUtilities.updateComponentTreeUI(f);
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

    private static JFreeChart createChart(double[][] points, int[] classes, double[] alphas) {
        XYSeries hyperplane = lineSeries(points, classes, alphas, "Hyperplane");
        XYSeries class1points = pointSeries(points, classes, 1, "Class1Points");
        XYSeries class2points = pointSeries(points, classes, -1,"Class2Points");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(hyperplane);
        dataset.addSeries(class1points);
        dataset.addSeries(class2points);

        JFreeChart chart = ChartFactory.createXYLineChart("SVMChart", "",
                "", dataset, PlotOrientation.VERTICAL, false, false, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesPaint(1, Color.red);
        Shape s = renderer.getSeriesShape(0);
        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesPaint(2, Color.blue);
        plot.setRenderer(renderer);

        return chart;
    }

    private static XYSeries lineSeries(double[][] points, int[] classes, double[] alphas, String key) {
        double[] max = maxValues(points);
        double b1 = 0.0;
        double b2 = 0.0;
        for (int i = 0; i < points.length; i++) {
            double t = alphas[i] * (double)classes[i];
            b1 += t * points[i][0];
            b2 += t * points[i][1];
        }
        double[] anyP = points[0];
        double b0 = -(anyP[0] * b1 + anyP[1] * b2);

        int res = 25;
        XYSeries series = new XYSeries(key);
        double xStep = max[0] / ((double) res);
        for(int i = 0; i < res; i++) {
            double x = i * xStep;
            double y = lineFunc(b0, b1, b2, x);
            if (y < max[1]) {
                series.add(x, y);
            }
        }
        return series;
    }

    private static double lineFunc(double b0, double b1, double b2, double x) {
        return -(x * b1 + b0) / b2;
    }

    private static double[] maxValues(double[][] points) {
        double[] max = {0.0, 0.0};
        for (int i = 0; i < points.length; i++) {
            double[] p = points[i];
            if (p[0] > max[0]) {
                max[0] = p[0];
            }
            if (p[1] > max[1]) {
                max[1] = p[1];
            }
        }
        return max;
    }

    private static XYSeries pointSeries(double[][] points, int[] classes, int c, String key) {
        XYSeries series = new XYSeries(key);
        for(int i = 0; i < points.length; i++) {
            if (classes[i] == c) {
                series.add(points[i][0], points[i][1]);
            }
        }
        return series;
    }
}
