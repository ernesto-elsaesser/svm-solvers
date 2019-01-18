package sample;

import java.io.FileReader;
import javax.swing.*;
import com.opencsv.CSVReader;

public class Main {

    public static void main(String[] args) {

        JFrame f = new JFrame();

        f.setSize(400,400);
        f.setLayout(null);
        f.setVisible(true);

        try {
            loadData();
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getLocalizedMessage());
        }
    }

    private static void loadData() throws Exception {
        CSVReader reader = new CSVReader(new FileReader("data/svmguide1.csv"));
        //TODO: read CSV
    }
}
