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

        double[][] points1 = {{5.0,12.0},{15.0,20.0}};
        double[][] points2 = {{20.0,8.0},{25.0,20.0}};
        JFreeChart chart = createChart(1.0, 0.0, 50.0, points1, points2);

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

    private static JFreeChart createChart(double a, double b, double maxX, double[][] points1, double[][] points2) {
        XYSeries lineSeries = lineSeries(a, b, maxX);
        XYSeries pointsSeries1 = pointSeries("PointSeries1", points1);
        XYSeries pointsSeries2 = pointSeries("PointSeries2", points2);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(lineSeries);
        dataset.addSeries(pointsSeries1);
        dataset.addSeries(pointsSeries2);

        JFreeChart chart = ChartFactory.createXYLineChart("TheChart", "X",
                "Y", dataset, PlotOrientation.VERTICAL, false, false, false);

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

    private static XYSeries lineSeries(double a, double b, double maxX) {
        int res = 100;
        XYSeries series = new XYSeries("LineSeries");
        double step = maxX / ((double) 100);
        for(int i = 0; i < res; i++) {
            double x = i * step;
            double y = a * x + b;
            series.add(x, y);
        }
        return series;
    }

    private static XYSeries pointSeries(String key, double[][] points) {
        XYSeries series = new XYSeries(key);
        for(int i = 0; i < points.length; i++) {
            series.add(points[i][0], points[i][1]);
        }
        return series;
    }
}
