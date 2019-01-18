package sample;

import java.io.FileReader;
import java.util.List;
import javax.swing.*;
import com.opencsv.CSVReader;

public class Main {

    public static void main(String[] args) {

        JFrame f = new JFrame();

        f.setSize(400,400);
        f.setLayout(null);
        f.setVisible(true);

        try {
            double[][] values = loadData();
            // TODO: apply SVM
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getLocalizedMessage());
        }
    }

    private static double[][] loadData() throws Exception {
        CSVReader reader = new CSVReader(new FileReader("data/svmguide1.csv"), ' ');
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
}
