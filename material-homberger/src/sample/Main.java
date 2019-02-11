package sample;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.*;

import com.opencsv.CSVReader;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class Main {
	
	public static boolean linear = false;
	public static boolean SMO    = false;

    static class Hyperplane {
        double b0 = 0, b1 = 0, b2 = 0;
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();
        JLabel label = new JLabel("Loading ...");
        label.setBounds(260,250, 80,40);
        f.add(label);
        f.setSize(600,600);
        f.setLayout(null);
        f.setVisible(true);
        JFreeChart chart;

        String file;
        SVM    svm;
        List<SupportVector> training;
        
        if(linear){
        	file = "data/svmlinear0.txt";
        	//file = "data/svmKreis4.txt";
            //schreiben(file);
        	training = einlesenAus(new File(file));
        }
        else{
            file = "data/svmKreis6.txt";
            
            //file = "data/XOR.txt";
        	
            training = einlesenAus(new File(file));
            //schreibenKreis(file);
//          List<SupportVector> training = parse("data/svmguide1.csv");
        }
      
        if(SMO)svm = solveSMO(training);
        else   svm = solveESZ(training);
   		
        
        
        if(linear){
        	Hyperplane h = new Hyperplane();
        	h = deriveHyperplane(training);
      		System.out.println("Vektordarstellung:  " + " w = (" + h.b1 + ", " + h.b2 + "); b = " + h.b0);
       		System.out.println("Geradendarstellung: " + " m = " + (-h.b1 / h.b2) + " b = " + (-h.b0 / h.b2));
       		chart = createChart(training, h);
       		
        }
        else{
        	chart = testcreateChart(training, svm);        
        }
   		
        	
        
        

 		
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(f.getSize());
        f.setContentPane(panel);
        SwingUtilities.updateComponentTreeUI(f);
       
        
        
//
//        classifyNewData(testing, h);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private static JFreeChart testcreateChart(List<SupportVector> vectors, SVM svm) {
    	
    	XYSeries hyperplane   = areaSeries(svm, "Hyperplane");
        XYSeries class1points = pointSeries(vectors, (byte)-1, "Class1Points");
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
     
        
        Shape s = renderer.getSeriesShape(1);
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesPaint(0, Color.blue);
        


       
        plot.setRenderer(renderer);

        
        
        return chart;
    }


    
    
    
    
    
    
    
    
    
	private static SVM solveSMO(List<SupportVector> vectors) {
		SVM svm = new SVM();
		svm.vectors = vectors;
		SMO smo = new SMO(svm);
		smo.train();
		
		svm.dualObjectiveFunction();
		svm.bBerechnen();
		svm.ausgabe();	
		return svm;
	}

	private static SVM solveESZ(List<SupportVector> vectors) {
		// es werden nur zulässige "Z" Lösungen zugelassen
		SVM svm = new SVM();
		svm.vectors = vectors;
		ESZ esz = new ESZ(svm);
		esz.run();
		
		svm.dualObjectiveFunction();
		svm.bBerechnen();
		return svm;
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    private static void classifyNewData(List<SupportVector> testing, Hyperplane h) {
        int rightClassification = 0;

        for (SupportVector testVector:
                testing) {
            double res = testVector.x[0] * h.b1 + testVector.x[1] * h.b2 + h.b0;
            if (res <= 0 && ((int)testVector.y) == 0)
                rightClassification++;
        }

        System.out.println("Zuverlässigkeit " + (((double)rightClassification / testing.size()) * 100) + "%");
    }


    
    
    
    
    private static Hyperplane deriveHyperplane(List<SupportVector> vectors) {
        Hyperplane h = new Hyperplane();
        for (SupportVector v : vectors) {
            h.b1 += v.alpha * v.y * v.x[0];
            h.b2 += v.alpha * v.y * v.x[1];
        }
        for (SupportVector v : vectors) {
            if (v.alpha > 0.0) {
                h.b0 = v.y - (v.x[0] * h.b1 + v.x[1] * h.b2);
                return h;
            }
        }
        throw new IllegalArgumentException("all alphas are zero");
    }

    
    
    
    
    /*
     * 
     * 
     * 
     * 
     * 
     */
    
	public static void schreiben(String name){
		try{
			double xwert = 100;
			double ywert = 100;
			double m     = 2;
			double c     = 15;
			double rand  = 5;
			int plus     = 10;
			int minus    = 10;
			int anz      = plus + minus;
			
			PrintWriter pu = new PrintWriter(new FileWriter(name));
			int k1 = 0;
			int k2 = 0;
			int nr = 0;
			
			while(nr < anz){
				double x = xwert*Math.random();
				double y = ywert*Math.random();
				double z = m*x+c;
				if(z > y + rand){
					if(k1 <= k2){
						pu.println("1"  + " " + x + " " + y);
						k1++;
						nr++;
					}
				}
				else if(z < y + rand){
					if(k2 <= k1){
						pu.println("-1" + " " + x + " " + y);
						k2++;
						nr++;
					}
				}
			}	
			
			pu.close();	
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

    
	public static void schreibenKreis(String name){
		try{
			double xwert = 100;
			double ywert = 100;
			double mwx   = 50;
			double mwy   = 50;
			double r     = 10;
			int anz      = 10;
			int nr       = 0;
			PrintWriter pu = new PrintWriter(new FileWriter(name));
			
			while(nr < anz/2){
				double x = xwert*Math.random();
				double y = ywert*Math.random();
				if(x > mwx-r && x < mwx+r && y > mwy-r && y < mwy+r){
					
				}
				else{
					pu.println("1"  + " " + x + " " + y);
					nr++;
				}
			}
			while(nr < anz){
				double wx = r*Math.random();
				double wy = r*Math.random();				
				if(Math.random() < 0.5)wx = -r*Math.random();
				if(Math.random() < 0.5)wy = -r*Math.random();
				
				double x = mwx + wx;
				double y = mwy + wy;
				pu.println("-1"  + " " + x + " " + y);
				nr++;
			}
			
			
			pu.close();	
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

    
	public static List<SupportVector> einlesenAus(File file) {
		List<SupportVector> vectors = new ArrayList<>();
		
		try{
    		Scanner scanner      = new Scanner(file);            
            while(scanner.hasNext()) {
            	int y     = Integer.valueOf(scanner.next());
            	double x1 = Double.valueOf (scanner.next());
                double x2 = Double.valueOf (scanner.next());
//            	double x3 = Double.valueOf (scanner.next());
//                double x4 = Double.valueOf (scanner.next());
                //SupportVector v = new SupportVector(x1, x2, x3, x4, y);
                SupportVector v = new SupportVector(x1, x2, y);
                vectors.add(v);
            } 
            scanner.close();
        }
        catch(FileNotFoundException e){
			System.out.println(e.getMessage());
        }
        return vectors;
	}

    
    
 /*
    private static List<SupportVector> parse(String filename) {
        List<String[]> lines;
        try {
            CSVReader reader = new CSVReader(new FileReader(filename), ' ');
            lines = reader.readAll();
        } catch (Exception e) {
            lines = new ArrayList<>();
        }
        int num = lines.size();
        List<SupportVector> vectors = new ArrayList<>();
        for (int i = 0; i < num; i++) {
        	System.out.println("gelesene WErte: " + num);
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
*/
    
    /*
     * 
     * 
     * 
     * 
     * 
     * 
     */
 
    
	
	
	
	
	
	
	
    
    private static JFreeChart createChart(List<SupportVector> vectors, Hyperplane h) {
        XYSeries class1points = pointSeries(vectors, (byte)-1, "Class1Points");
        XYSeries class2points = pointSeries(vectors, (byte)1,"Class2Points");
        XYSeries hyperplane   = lineSeries(vectors, h,"Hyperplane");

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
        if (h.b2 == 0) {
            series.add(-h.b0, yMin);
            series.add(-h.b0, yMax);
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
        return -(x * h.b1 + h.b0) / h.b2;
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
    
    private static XYSeries areaSeries(SVM svm, String key) {
        XYSeries series = new XYSeries(key);
        for (int i=-100;i<100;i++) {
        	for (int j=-100;j<100;j++) {
        		int wert1 = i;
        		int wert2 = j;    
        		double[] werte = {wert1, wert2};
        		double erg = svm.outputNeu(werte);
        		if(erg < 0){
        			series.add(wert1, wert2);	
        		}
        		double wert3 = i+0.5;
        		double wert4 = j+0.5;    
        		double[] werte2 = {wert3, wert4};
        		erg = svm.outputNeu(werte2);
        		if(erg < 0){
        			series.add(wert3, wert4);	
        		}
        		
        		
        		
        		//System.out.println(wert1 + " " + wert2 + " " + erg);
        	}            
        }        
        
        return series;
    }

    private static XYSeries paintSeries(String key) {
        XYSeries series = new XYSeries(key);
        for (int i=-100;i<100;i++) {
        	for (int j=-100;j<100;j++) {
        		series.add(i+10, j+55);
        		series.add(i+0.5, j+0.5);
        	}            
        }
        return series;
    }
    
    
    
    
    
    
    
    
    /*
     * 
     * 
     * 
     * 
     * 
     * 
     */
    
    
    
    
    
    
    
    private static List<SupportVector> simpleSet0() {
		List<SupportVector> list = new ArrayList<>();
		list.add(new SupportVector(2.0, 5.0, -1));
		list.add(new SupportVector(4.0, 2.0, -1));
		list.add(new SupportVector(3.0, 8.0, 1));
		list.add(new SupportVector(6.0, 10.0, 1));
		
		return list;
	}
    private static List<SupportVector> simpleSet1() {
        List<SupportVector> list = new ArrayList<>();
        list.add(new SupportVector(4.0, 2.0, 0));
        list.add(new SupportVector(2.0, 5.0, 0));
        list.add(new SupportVector(3.0, 8.0, 1));
        return list;
    }

    private static List<SupportVector> simpleSet2() {
        List<SupportVector> list = new ArrayList<>();
        list.add(new SupportVector(2.0, 1.0, 0));
        list.add(new SupportVector(2.0, -1.0, 0));
        list.add(new SupportVector(4.0, 0.0, 1));
        return list;
    }
	

	private static List<SupportVector> simpleSet3() {
		List<SupportVector> list = new ArrayList<>();
		list.add(new SupportVector(1.0, 9.0, 1));
		//list.add(new SupportVector(3.0, 10.0, 1));
		list.add(new SupportVector(4.0, 3.0, 1));		
		list.add(new SupportVector(1.0, 4.0, -1));
		list.add(new SupportVector(7.0, 4.0, -1));

		return list;
	}
	
}
