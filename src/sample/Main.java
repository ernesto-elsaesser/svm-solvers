package sample;

import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import com.opencsv.CSVReader;

import layout.SpringUtilities;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class Main {

    private static int DATA_SET = 0;
    private static boolean USE_SMO = false;
    private static boolean USE_POLY_KERNEL = true;

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

        if (USE_SMO) {
            SMO smo = new SMO(svm);
            smo.train();
        } else {
            ESZ esz = new ESZ(svm);
            esz.run();
        }

        JFreeChart chart;
        Panel p = new Panel(new BorderLayout());
        chart = createChart(trainingVectors, svm);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        Panel settingsPanel = new Panel(new SpringLayout());

        CheckboxGroup cbg = new CheckboxGroup();
        Checkbox mbaBox = new Checkbox("MBA", cbg, true);
        Checkbox btechBox = new Checkbox("B.Tech", cbg, false);
        Checkbox archBox = new Checkbox("B.Arch", cbg, false);
        Label lab = new Label("I Display Your Selection");

        Panel cbp = new Panel();

        settingsPanel.add(lab);
        cbp.add(mbaBox);
        cbp.add(btechBox);
        cbp.add(archBox);
        settingsPanel.add(cbp);

        JLabel l = new JLabel("test", JLabel.TRAILING);
        settingsPanel.add(l);
        JTextField textField = new JTextField(10);
        l.setLabelFor(textField);
        settingsPanel.add(textField);

        SpringUtilities.makeCompactGrid(settingsPanel,
                1, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

        p.add(panel, BorderLayout.CENTER);
        p.add(settingsPanel, BorderLayout.LINE_END);
        f.setContentPane(p);
        SwingUtilities.updateComponentTreeUI(f);

        if (testVectors != null) {
            classifyNewData(testVectors, svm);
        }
    }

    private static void classifyNewData(List<SupportVector> testing, SVM svm) {
        int rightClassification = 0;

        for (SupportVector testVector: testing) {
            if (svm.output(testVector.x) <= 0 && ((int)testVector.y) == 0)
                rightClassification++;
        }

        System.out.println("Zuverlässigkeit " + (((double)rightClassification / testing.size()) * 100) + "%");
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

    private static JFreeChart createChart(List<SupportVector> vectors, SVM svm) {

        XYSeries hyperplane   = areaSeries(svm, "Hyperplane");
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

        double stepX = (xMax - xMin) / 100.0;
        double stepY = (yMax - yMin) / 100.0;

        for(double x = xMin-3; x < xMax+3; x+=stepX) {
            for(double y = yMin-3; y < yMax+3; y+=stepY) {
                double classification = svm.output(new double[] {x, y});

                if (classification <= 0)
                    series.add(x, y);
            }
        }

        return series;
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
