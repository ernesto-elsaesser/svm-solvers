package sample;

import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import com.opencsv.CSVReader;

import javafx.util.Pair;
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

        Pair<double[][], int[]> training = null;
        Pair<double[][], int[]> testing = null;
        try {

            training = parse("data/svmguide1.csv");
            testing = parse("data/svmguide1-t.csv");
            // TODO: apply SVM
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getLocalizedMessage());
        }

        //double[][] points = {{4.0,2.0},{2.0,5.0},{3.0,8.0}};
        //classes = new int[]{-1, -1, 1};
        double[] wrongAlphas = {-0.679,13.654,8.419};
        double[] optimizedAlphas = calcAlphas(training.getKey(), training.getValue());

        //double[][] points = {{2.0,1.0},{2.0,-1.0},{4.0,0.0}};
        //int[] classes = {-1, -1, 1};
        //double[] alphas = {3.25,3.25,3.5};

        JFreeChart chart = createChart(training.getKey(), training.getValue(), optimizedAlphas);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        f.setContentPane(panel);
        SwingUtilities.updateComponentTreeUI(f);
    }

    private static double[] calcAlphas(double[][] points, int[] classes) {
        SVM svm = new SVM();
        for (int i = 0; i < points.length; i++) {
            svm.add(points[i], classes[i]);
        }

        SMO smo = new SMO(svm);
        smo.train();

        double[] alphas = new double[points.length];
        for(int i = 0; i < points.length; i++) {
            SupportVector v = svm.vectors.get(i);
            if (v.alpha <= SVM.EPSILON) {
                alphas[i] = 0;
            } else {
                alphas[i] = v.alpha;
            }
        }
        return alphas;
    }

    private static Pair<double[][], int[]> parse(String filename) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(filename), ' ');
        List<String[]> lines = reader.readAll();
        int num = lines.size();
        int classes[] = new int[num];
        double[][] values = new double[num][4];
        for (int i = 0; i < num; i++) {
            String[] line = lines.get(i);
            values[i][0] = parseValue(line[1]);
            values[i][1] = parseValue(line[2]);
            values[i][2] = parseValue(line[3]);
            values[i][3] = parseValue(line[4]);

            classes[i] = normalizeClassification(Integer.parseInt(line[0]));
        }
        return new Pair<>(values, classes);
    }

    private static int normalizeClassification(int parsedInt) {
        if (parsedInt == 0)
            return -1;
        else
            return 1;
    }

    private static double parseValue(String s) {
        String[] parts = s.split(":");
        return Double.valueOf(parts[1]);
    }

    private static JFreeChart createChart(double[][] points, int[] classes, double[] alphas) {
        XYSeries hyperplane = lineSeries(points, classes, alphas, "Hyperplane");
        XYSeries class1points = pointSeries(points, classes, 1, "Class1Points");
        XYSeries class2points = pointSeries(points, classes, -1,"Class2Points");
        XYSeries origin = new XYSeries("Origin");
        origin.add(0, 0);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(hyperplane);
        dataset.addSeries(class1points);
        dataset.addSeries(class2points);
        //dataset.addSeries(origin);

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
        double[] b = deriveLine(points, classes, alphas);

        int res = 25;
        double[] range = range(points);
        double xStep = range[1] / ((double) res);
        XYSeries series = new XYSeries(key);
        if (b[2] == 0) {
            series.add(-b[0], range[0]);
            series.add(-b[0], range[1]);
        } else {
            for(int i = 0; i < res; i++) {
                double x = i * xStep;
                double y = lineFunc(b, x);
                if (y >= range[0] || y <= range[1]) {
                    series.add(x, y);
                }
            }
        }

        return series;
    }

    private static double[] deriveLine(double[][] points, int[] classes, double[] alphas) {
        double[] b = {0,0,0};
        for (int i = 0; i < points.length; i++) {
            double t = alphas[i] * (double)classes[i];
            b[1] += t * points[i][0];
            b[2] += t * points[i][1];
        }
        double[] pi = points[0];
        double yi = (double)classes[0];
        b[0] = (1/yi) - (pi[0] * b[1] + pi[1] * b[2]);
        return b;
    }

    private static double lineFunc(double[] b, double x) {
        return -(x * b[1] + b[0]) / b[2];
    }

    private static double[] range(double[][] points) {
        double max = 0.0;
        double min = 0.0;
        for (int i = 0; i < points.length; i++) {
            double[] p = points[i];
            if (p[0] > max) {
                max = p[0];
            }
            if (p[1] > max) {
                max = p[1];
            }
            if (p[0] < min) {
                min = p[0];
            }
            if (p[1] < min) {
                min = p[1];
            }
        }
        double[] range = {min, max};
        return range;
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
