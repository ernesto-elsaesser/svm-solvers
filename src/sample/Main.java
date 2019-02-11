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

    private static int DATA_SET = 2;
    private static boolean USE_CACHED_PLANE = false;
    private static boolean USE_SMO = false;

    static class Hyperplane {
        double b = 0, w0 = 0, w1 = 0;
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();

        JLabel label = new JLabel("Loading ...");
        label.setBounds(260,250, 80,40);
        f.add(label);

        f.setSize(600,600);
        f.setLayout(null);
        f.setVisible(true);

        ExtendedSVM svm = new ExtendedSVM();
        List<SupportVector> testData = null;
        switch (DATA_SET) {
            case 0:
                svm.vectors = parse("fourclass");
                testData = parse("fourclass-t");
                break;
            case 1:
                svm.vectors = parse("svmguide1");
                testData = parse("svmguide1-t");
                break;
            case 2:
                svm.vectors = parse("separatable");
                svm.dataIsLinearilySeparatable = true;
                break;
            case 3:
                svm.vectors = parse("circular");
                break;
            case 4:
                svm.vectors = parse("threepoint1");
                svm.dataIsLinearilySeparatable = true;
                break;
            case 5:
                svm.vectors = parse("threepoint2");
                svm.dataIsLinearilySeparatable = true;
                break;
        }
        List<SupportVector> originalVectors = svm.vectors;

        Hyperplane h = new Hyperplane();
        if (USE_CACHED_PLANE) {
            h.b = -4.2;
            h.w0 = 0.6;
            h.w1 = 0.4;
        } else {
            if (USE_SMO) {
                SMO smo = new SMO(svm);
                smo.train();
            } else {
                svm.c = 1000; // large c -> narrow margin
                ESZ esz = new ESZ(svm);
                esz.run();
            }
            h = deriveHyperplane(originalVectors);
        }

        JFreeChart chart = createChart(originalVectors, h);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        f.setContentPane(panel);
        SwingUtilities.updateComponentTreeUI(f);

        if (testData != null) {
            classifyNewData(testData, h);
        }
    }

    private static void classifyNewData(List<SupportVector> testing, Hyperplane h) {
        int rightClassification = 0;

        for (SupportVector testVector:
                testing) {
            double res = testVector.x[0] * h.w0 + testVector.x[1] * h.w1 + h.b;
            if (res <= 0 && ((int)testVector.y) == 0)
                rightClassification++;
        }

        System.out.println("Zuverlässigkeit " + (((double)rightClassification / testing.size()) * 100) + "%");
    }

    private static Hyperplane deriveHyperplane(List<SupportVector> vectors) {
        Hyperplane h = new Hyperplane();
        for (SupportVector v : vectors) {
            h.w0 += v.alpha * v.sign() * v.x[0];
            h.w1 += v.alpha * v.sign() * v.x[1];
        }
        for (SupportVector v : vectors) {
            if (v.alpha > 0.0) {
                h.b = v.sign() - (v.x[0] * h.w0 + v.x[1] * h.w1);
                return h;
            }
        }
        throw new IllegalArgumentException("all alphas are zero");
    }

    private static List<SupportVector> parse(String filename) {
        List<String[]> lines;
        try {
            CSVReader reader = new CSVReader(new FileReader("data/" + filename + ".csv"), ' ');
            lines = reader.readAll();
        } catch (Exception e) {
            lines = new ArrayList<>();
        }
        int num = lines.size();
        List<SupportVector> vectors = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String[] line = lines.get(i);
            double x1 = parseValue(line[1]);
            double x2 = parseValue(line[2]);
            int y = Integer.parseInt(line[0]);
            SupportVector v = new SupportVector(x1, x2, y);
            vectors.add(v);
        }
        return vectors;
    }

    private static double parseValue(String s) {
        String[] parts = s.split(":");
        return Double.valueOf(parts[1]);
    }

    private static JFreeChart createChart(List<SupportVector> vectors, Hyperplane h) {
        XYSeries class1points = pointSeries(vectors, (byte)0, "Class1Points");
        XYSeries class2points = pointSeries(vectors, (byte)1,"Class2Points");
        XYSeries hyperplane = lineSeries(vectors, h,"Hyperplane");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(hyperplane);
        dataset.addSeries(class1points);
        dataset.addSeries(class2points);

        JFreeChart chart = ChartFactory.createXYLineChart("SVMChart", "",
                "", dataset, PlotOrientation.VERTICAL, false, false, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
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

    private static XYSeries lineSeries(List<SupportVector> vectors, Hyperplane h, String key) {
        XYSeries series = new XYSeries(key);

        double xMin = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;
        double yMax = 0.0;
        for (SupportVector v : vectors) {
            if (v.x[0] > xMax) {
                xMax = v.x[0];
            }
            if (v.x[1] > yMax) {
                yMax = v.x[1];
            }
            if (v.x[0] < xMin) {
                xMin = v.x[0];
            }
            if (v.x[1] < yMin) {
                yMin = v.x[1];
            }
        }
        if (h.w1 == 0) {
            series.add(-h.b, yMin);
            series.add(-h.b, yMax);
        } else {
            int res = 50;
            double xStep = (xMax - xMin) / (double)res;
            for(int i = -3; i < res+3; i++) {
                double x = xMin + i * xStep;
                double y = lineFunc(h, x);
                if (y >= 0.66 * yMin && y <= 1.5 * yMax) {
                    series.add(x, y);
                }
            }
        }
        return series;
    }

    private static double lineFunc(Hyperplane h, double x) {
        return -(x * h.w0 + h.b) / h.w1;
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
