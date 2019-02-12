package sample;

import java.awt.*;
import java.awt.event.*;
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

import sample.kernels.*;
import sample.solvers.*;

public class Main implements ActionListener {

    private JFrame frame;
    private JPanel configPanel;
    private JPanel mainPanel;
    private JComboBox dataSetSelector;
    private JCheckBox kernelToggle;

    private SpinnerModel smoEpsilonModel;
    private JButton smoRunButton;

    private SpinnerModel eszEpsilonModel;
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

        kernelToggle = new JCheckBox("Use polynomial kernel");
        configPanel.add(kernelToggle);

        configPanel.add(this.solverHeader("SMO"));

        JLabel smoEpsilonLabel = new JLabel("Epsilon Exp.");
        configPanel.add(smoEpsilonLabel);

        smoEpsilonModel = new SpinnerNumberModel(-5, -10, -1, 1);
        JSpinner smoEpsilonSpinner = new JSpinner(smoEpsilonModel);
        configPanel.add(smoEpsilonSpinner);

        smoRunButton = new JButton("Run SMO");
        smoRunButton.addActionListener(this);
        configPanel.add(smoRunButton);

        configPanel.add(this.solverHeader("Evolution"));

        JLabel eszEpsilonLabel = new JLabel("Epsilon Exp.");
        configPanel.add(eszEpsilonLabel);

        eszEpsilonModel = new SpinnerNumberModel(-5, -10, -1, 1);
        JSpinner eszEpsilonSpinner = new JSpinner(eszEpsilonModel);
        configPanel.add(eszEpsilonSpinner);

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

    private JPanel solverHeader(String title) {
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

        SVM svm = new SVM();
        svm.vectors = trainingVectors;
        if (kernelToggle.isSelected()) {
            svm.kernel = new PolynomialKernel();
        } else {
            svm.kernel = new DotProductKernel();
        }

        SpinnerModel model;
        Solver solver;

        if (e.getSource() == smoRunButton) {
            model = smoEpsilonModel;
            solver = new SMO();
        } else {
            model = eszEpsilonModel;
            solver = new ESZ();
        }

        svm.epsilon = Math.pow(10, (int) model.getValue());
        solver.solve(svm);
        svm.updateB();

        JFreeChart chart = createChart(trainingVectors, svm);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(mainPanel.getSize());
        mainPanel.removeAll();
        mainPanel.add(chartPanel);
        SwingUtilities.updateComponentTreeUI(frame);
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
}
