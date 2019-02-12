package sample;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

import com.opencsv.CSVReader;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class Main implements ActionListener {

    private static int DATA_SET = 2;
    private static boolean USE_SMO = true;
    private static boolean USE_POLY_KERNEL = false;

    private JFrame frame;
    private JPanel configPanel;
    private JPanel mainPanel;
    private JComboBox dataSetSelector;

    private JCheckBox smoKernelToggle;
    private JButton smoRunButton;

    private JCheckBox eszKernelToggle;
    private JButton eszRunButton;

    private String[] dataSets = {"trivial1","trivial2","separatable","circular","real1","real2"};

    public static void main(String[] args) {
        Main instance = new Main();
        instance.startUI();
    }

    public void startUI() {

        frame = new JFrame();
        frame.setSize(900,600);
        frame.setLayout(null);

        JPanel splitPanel = new JPanel(new BorderLayout());

        configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.setPreferredSize(new Dimension(200,600));
        configPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        dataSetSelector = new JComboBox(dataSets);
        configPanel.add(dataSetSelector);

        configPanel.add(this.algorithmHeader("SMO"));

        smoKernelToggle = new JCheckBox("Use polynomial kernel");
        configPanel.add(smoKernelToggle);

        smoRunButton = new JButton("Run SMO");
        smoRunButton.addActionListener(this);
        configPanel.add(smoRunButton);

        configPanel.add(this.algorithmHeader("Evolution"));

        eszKernelToggle = new JCheckBox("Use polynomial kernel");
        configPanel.add(eszKernelToggle);

        eszRunButton = new JButton("Run ESZ");
        eszRunButton.addActionListener(this);
        configPanel.add(eszRunButton);

        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(700,600));

        splitPanel.add(configPanel, BorderLayout.WEST);
        splitPanel.add(mainPanel, BorderLayout.EAST);

        frame.setContentPane(splitPanel);
        frame.setVisible(true);
    }

    private JPanel algorithmHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(180,40));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String dataSet = (String) dataSetSelector.getSelectedItem();
        List<FeatureVector> trainingVectors = parse(dataSet);

        SVM svm;
        if (e.getSource() == smoRunButton) {
            boolean usePolyKernel = smoKernelToggle.isSelected();
            svm = new SVM(usePolyKernel);
            svm.vectors = trainingVectors;
            SMO smo = new SMO(svm);
            smo.train();
        } else {
            boolean usePolyKernel = eszKernelToggle.isSelected();
            svm = new SVM(usePolyKernel);
            svm.vectors = trainingVectors;
            ESZ esz = new ESZ(svm);
            esz.run();
        }

        List<FeatureVector> supportVectors = findSupportVectors(svm);
        svm.b = calculateB(supportVectors, svm);

        JFreeChart chart = createChart(trainingVectors, svm);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(mainPanel.getSize());
        mainPanel.removeAll();
        mainPanel.add(chartPanel);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private List<FeatureVector> findSupportVectors(SVM svm) {
        List<FeatureVector> featureVectors = new ArrayList<>();
        for (FeatureVector v: svm.vectors) {
            if (v.alpha > svm.epsilon) {
                featureVectors.add(v);
            }
        }
        return featureVectors;
    }

    private double calculateB(List<FeatureVector> featureVectors, SVM svm) {
        double bsum = 0;
        for (FeatureVector i: featureVectors) {
            double subsum = 0;
            for(FeatureVector j: featureVectors) {
                subsum += j.alpha * j.sign() * svm.kernelFunc(i.x, j.x);
            }
            bsum += i.sign() - subsum;
        }
        return bsum / featureVectors.size();
    }

    private double calculateBAlt(List<FeatureVector> featureVectors, SVM svm) {
        double[] w = new double[2];
        for (FeatureVector v : featureVectors) {
            w[0] += v.alpha * v.sign() * v.x[0];
            w[1] += v.alpha * v.sign() * v.x[1];
        }
        double bsum = 0;
        for (FeatureVector v : featureVectors) {
            bsum += v.sign() - svm.kernelFunc(v.x, w);
        }
        return bsum / featureVectors.size();
    }

    private List<FeatureVector> parse(String filename) {
        List<String[]> lines;
        try {
            CSVReader reader = new CSVReader(new FileReader("data/" + filename + ".csv"), ' ');
            lines = reader.readAll();
        } catch (Exception e) {
            lines = new ArrayList<>();
        }
        int num = lines.size();
        List<FeatureVector> vectors = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String[] line = lines.get(i);
            double x1 = parseValue(line[1]);
            double x2 = parseValue(line[2]);
            int y = Integer.parseInt(line[0]);
            FeatureVector v = new FeatureVector(x1, x2, y);
            vectors.add(v);
        }
        return vectors;
    }

    private double parseValue(String s) {
        String[] parts = s.split(":");
        return Double.valueOf(parts[1]);
    }

    private JFreeChart createChart(List<FeatureVector> vectors, SVM svm) {

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

    private XYSeries areaSeries(SVM svm, String key) {
        XYSeries series = new XYSeries(key);

        double xMin = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;
        double yMax = 0.0;

        for (FeatureVector v : svm.vectors) {
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

    private XYSeries pointSeries(List<FeatureVector> vectors, byte y, String key) {
        XYSeries series = new XYSeries(key);
        for (FeatureVector v : vectors) {
            if (v.y == y) {
                series.add(v.x[0], v.x[1]);
            }
        }
        return series;
    }

    private void classifyNewData(List<FeatureVector> testing, SVM svm) {
        int rightClassification = 0;

        for (FeatureVector testVector: testing) {
            if (svm.output(testVector.x) <= 0 && ((int)testVector.y) == 0)
                rightClassification++;
        }

        System.out.println("Zuverlässigkeit " + (((double)rightClassification / testing.size()) * 100) + "%");
    }
}
