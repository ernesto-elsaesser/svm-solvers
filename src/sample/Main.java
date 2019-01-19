package sample;

import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;
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

        List<SupportVector> training = null;
        List<SupportVector> testing = null;
        try {

            training = parse("data/svmguide1.csv");
            testing = parse("data/svmguide1-t.csv");
            // TODO: apply SVM
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getLocalizedMessage());
            return;
        }

        //double[][] points = {{4.0,2.0},{2.0,5.0},{3.0,8.0}};
        //classes = new int[]{-1, -1, 1};
        double[] wrongAlphas = {-0.679,13.654,8.419};

        solve(training);

        //double[][] points = {{2.0,1.0},{2.0,-1.0},{4.0,0.0}};
        //int[] classes = {-1, -1, 1};
        //double[] alphas = {3.25,3.25,3.5};

        JFreeChart chart = createChart(training);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        f.setContentPane(panel);
        SwingUtilities.updateComponentTreeUI(f);
    }

    private static void solve(List<SupportVector> vectors) {
        SVM svm = new SVM();
        svm.vectors = vectors;
        SMO smo = new SMO(svm);
        smo.train();
        svm.prune();
    }

    private static List<SupportVector> parse(String filename) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(filename), ' ');
        List<String[]> lines = reader.readAll();
        int num = lines.size();
        List<SupportVector> vectors = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String[] line = lines.get(i);
            double x1 = parseValue(line[1]);
            double x2 = parseValue(line[2]);
            double[] x = {x1, x2};
            byte y = (byte) Integer.parseInt(line[0]);
            SupportVector v = new SupportVector(x, y);
            vectors.add(v);
        }
        return vectors;
    }

    private static double parseValue(String s) {
        String[] parts = s.split(":");
        return Double.valueOf(parts[1]);
    }

    private static JFreeChart createChart(List<SupportVector> vectors) {
        XYSeries hyperplane = lineSeries(vectors, "Hyperplane");
        XYSeries class1points = pointSeries(vectors, (byte)0, "Class1Points");
        XYSeries class2points = pointSeries(vectors, (byte)1,"Class2Points");
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

    private static XYSeries lineSeries(List<SupportVector> vectors, String key) {
        double[] b = deriveLine(vectors);

        int res = 25;
        double[] range = range(vectors);
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

    private static double[] deriveLine(List<SupportVector> vectors) {
        double[] b = {0,0,0};
        for (SupportVector v : vectors) {
            double t = v.alpha * (double)v.y;
            b[1] += t * v.x[0];
            b[2] += t * v.x[1];
        }
        SupportVector vi = vectors.get(0);
        double yi = (double)vi.y;
        b[0] = (1/yi) - (vi.x[0] * b[1] + vi.x[1] * b[2]);
        return b;
    }

    private static double lineFunc(double[] b, double x) {
        return -(x * b[1] + b[0]) / b[2];
    }

    private static double[] range(List<SupportVector> vectors) {
        double max = 0.0;
        double min = 0.0;
        for (SupportVector v : vectors) {
            if (v.x[0] > max) {
                max = v.x[0];
            }
            if (v.x[1] > max) {
                max = v.x[1];
            }
            if (v.x[0] < min) {
                min = v.x[0];
            }
            if (v.x[1] < min) {
                min = v.x[1];
            }
        }
        double[] range = {min, max};
        return range;
    }

    private static XYSeries pointSeries(List<SupportVector> vectors, byte y, String key) {
        XYSeries series = new XYSeries(key);
        for (SupportVector v : vectors) {
            if (v.y == y) {
                series.add(v.x[0], v.x[1]);
            }
        }
        return series;
    }
}
