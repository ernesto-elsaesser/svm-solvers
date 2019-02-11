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

    private static int DATA_SET = 3;
    private static boolean USE_CACHED_PLANE = false;
    private static boolean USE_SMO = true;
    private static boolean USE_POLY_KERNEL = false;

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

        String trainingFile = "";
        List<SupportVector> testVectors = null;
        switch (DATA_SET) {
            case 0:
                trainingFile = "fourclass";
                testVectors = parse("fourclass-t");
                break;
            case 1:
                trainingFile = "svmguide1";
                testVectors = parse("svmguide1-t");
                break;
            case 2:
                trainingFile = "separatable";
                break;
            case 3:
                trainingFile = "circular";
                break;
            case 4:
                trainingFile = "threepoint1";
                break;
            case 5:
                trainingFile = "threepoint2";
                break;
        }
        List<SupportVector> trainingVectors = parse(trainingFile);
        SVM svm = new SVM(USE_POLY_KERNEL);
        svm.vectors = trainingVectors;

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
                ESZ esz = new ESZ(svm);
                esz.run();
            }
            h = deriveHyperplane(trainingVectors);
        }

        JFreeChart chart;
        if (USE_POLY_KERNEL)
            chart = createChart(trainingVectors, h);
        else
            chart = testcreateChart(trainingVectors, svm);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        f.setContentPane(panel);
        SwingUtilities.updateComponentTreeUI(f);

        if (testVectors != null) {
            classifyNewData(testVectors, h);
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

    private static JFreeChart testcreateChart(List<SupportVector> vectors, SVM svm) {

        XYSeries hyperplane   = areaSeries2(svm, "Hyperplane");
        XYSeries class1points = pointSeries(vectors, (byte)0, "Class1Points");
        XYSeries class2points = pointSeries(vectors, (byte)1,"Class2Points");


        XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(class1points);

        dataset.addSeries(class2points);
        dataset.addSeries(hyperplane);

        JFreeChart chart = ChartFactory.createXYLineChart("SVMChart", "",
                "", dataset, PlotOrientation.VERTICAL, false, false, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesPaint(1, Color.red);

        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesPaint(2, Color.yellow);

        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesPaint(0, Color.blue);

        plot.setRenderer(renderer);

        return chart;
    }

    private static XYSeries areaSeries(SVM svm, String key) {
        XYSeries series = new XYSeries(key);
        for (int i=-100;i<100;i++) {
            for (int j=-100;j<100;j++) {
                int wert1 = i;
                int wert2 = j;
                double[] werte = {wert1, wert2};
                double erg = svm.output(werte);
                if(erg < 0){
                    series.add(wert1, wert2);
                }
                double wert3 = i+0.5;
                double wert4 = j+0.5;
                double[] werte2 = {wert3, wert4};
                erg = svm.output(werte2);
                if(erg < 0){
                    series.add(wert3, wert4);
                }
            }
        }

        return series;
    }

    private static XYSeries areaSeries2(SVM svm, String key) {
        XYSeries series = new XYSeries(key);

        double xMin = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;
        double yMax = 0.0;

        for (SupportVector v : svm.vectors) {
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

        for(double x = xMin-3; x < xMax+3; x+=0.5) {
            for(double y = yMin-3; y < yMax+3; y+=0.5) {
                double classification = svm.output(new double[] {x, y});

                if (classification <= 0)
                    series.add(x, y);
            }
        }

        return series;
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
