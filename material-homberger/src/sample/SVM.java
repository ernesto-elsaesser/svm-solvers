package sample;

import java.util.ArrayList;
import java.util.List;


public class SVM {

    public static final double EPSILON = 1e-5;//1e-7;
   
    
    List<SupportVector> vectors;
    double b   = 0; 	// treshold
    double c   = 1000; 	// soft-margin parameter (grosses c -> schmale Strasse)
    double obj = 0;     // Zielfunktionswert duales Problem

    public void bBerechnen(){
    	int    anz = 0;
    	double erg = 0;
    	double sum = 0;
    	double bw  = 0;
        for (SupportVector v : vectors) {
            if (v.alpha > EPSILON && v.alpha < c) {
            	erg = 0;
            	for (SupportVector w : vectors) {
            		double q = 0;
                    if (w.alpha > EPSILON && w.alpha < c) {
                    	q = w.alpha * w.y  * kernelFunc(v.x, w.x);
                    	erg += q;
                    }
            	}
            	bw = v.y - erg;
            	anz++;
            	sum += bw;
            }
        }
        //b = -sum/anz;
        b = sum/anz;       
    }
  
    
 /*   
    public void bBerechnen(){

    	double erg = 0;

    	double max = Double.MAX_VALUE;
    	double min = -max;

    	for (SupportVector v : vectors) {
            if (v.alpha > EPSILON && v.alpha < c  && v.y == -1) {
            	erg = 0;
            	for (SupportVector w : vectors) {
            		double q = 0;
                    if (w.alpha > EPSILON && w.alpha < c) {
                    	q = w.alpha * w.y  * kernelFunc(v.x, w.x);
                    	erg += q;
                    }
            	}
            	if(erg > min)min = erg;
            }
        }
    	
    	for (SupportVector v : vectors) {
            if (v.alpha > EPSILON && v.alpha < c  && v.y == 1) {
            	erg = 0;
            	for (SupportVector w : vectors) {
            		double q = 0;
                    if (w.alpha > EPSILON && w.alpha < c) {
                    	q = w.alpha * w.y  * kernelFunc(v.x, w.x);
                    	erg += q;
                    }
            	}
            	if(erg < max)max = erg;
            }
        }
    	b = -0.5*(min+max);
    	
    }
 */   
    
    public double outputNeu(double[] x) {
        //double u = -b;
    	double u = b;
        for(SupportVector v : vectors) {
            if(v.alpha <= EPSILON)
                continue; // ignore non-support vectors
            u += v.alpha * v.y * kernelFunc(v.x, x);
        }
        return u;
    }
    
    public double output(double[] x) {
        double u = -b;
        for(SupportVector v : vectors) {
            if(v.alpha <= EPSILON)continue;
            u += v.alpha * v.y * kernelFunc(v.x, x);
        }
        return u;
    }
   
    
    

    public void prune() {
        for (SupportVector v: vectors) {
            if (v.alpha < EPSILON) {
                v.alpha = 0;
            }
        }
    }

    public double kernelFunc(double[] x1, double[] x2) {
        
    	if(Main.linear){
            double prod = 0;
            for(int i = 0; i < x2.length; i++)
                prod += x1[i] * x2[i];
            return prod;
    	}
    	else{
    		
          double prod = 0;
          double wert = 0;
         
          
          //polynomieller Kern
          
          
          for(int i = 0; i < x2.length; i++){
        	  wert  = 1 + (x1[i] * x2[i]);
        	  prod += Math.pow(wert, 2);
          } 
          
          
          /*
          double o = 0.0001;
          double lambda = -1./(2.*Math.pow(o, 2)); 
          for(int i = 0; i < x2.length; i++){
        	  prod += (x1[i] - x2[i])*(x1[i] - x2[i]);
          } 
          Math.exp(lambda*prod);
          */
          
          
          return prod;
    	}
    }
    
    
    public void ausgabe(){
    
    	if(Main.linear)System.out.println("Lin. Kernel");
    	else           System.out.println("Pol. Kernel");
    	
    	int j = 1;
    	for(SupportVector i : vectors) {
    		//if(i.alpha > EPSILON) 
    			System.out.println("x" + j + " = " + i.x[0] + " " + i.x[1] + " " + i.y + "\t Alpha= " + round(i.alpha));
            j++;
    	}
    	System.out.println("Ziel (max) = " + obj);
    	
    	
    }
    public void dualObjectiveFunction(){
    	//Berechnet Funktionswert 
    	double erg1 = 0;
    	double erg2 = 0;
    	boolean check = true;
    	double  wert  = 0;
    	for(SupportVector i : vectors) {
    		//if(i.alpha < 0)System.out.println("----------------------------------");
    		wert += i.alpha*i.y;
            if(i.alpha > EPSILON){
            	erg1 = erg1 + i.alpha;
            }
    	}
    	//if(Math.abs(wert-0.0001) > 0.0001)System.out.println("+++++++++++++++++++++++++++++" + wert);
    	
    	for(SupportVector i : vectors) {
             if(i.alpha > EPSILON){
            	 for(SupportVector j : vectors) {
                     if(j.alpha > EPSILON){
                    	 erg2 = erg2 + (i.alpha * j.alpha * i.y * j.y * kernelFunc(i.x, j.x)); 
                     }
                 } 
             }
        }
    	this.obj = erg1 - (0.5*erg2); 
    }
    public static double round(double w){
		int erg = (int)(w * 1000);
		return erg/100.;
	}

}