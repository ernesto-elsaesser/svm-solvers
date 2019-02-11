package sample;

import java.util.*;

public class ESZ {
	private ExtendedSVM svm;
    private final int iMax = 10000000;
    private double delta   = 0.00001;//0.00001


    public ESZ(ExtendedSVM svm) {
        this.svm = svm;
    }
    
    public void run() {
		this.alphaInit();
		this.svm.dualObjectiveFunction();
		ExtendedSVM tmp = new ExtendedSVM();
        copyAll(svm, tmp);
        
		for(int i=0;i<iMax;i++){
			move(tmp);
			tmp.dualObjectiveFunction();

			if(tmp.obj > svm.obj)copyTeil(tmp, svm);
		}
		svm.bBerechnen();
    }

	public void alphaInit(){
		//muss angepasst werden, falls Fehler erlaubt sind
		//dann gilt 0<=alpha<=C
		boolean zulaessig = false;
		double  sum       = 0;
		while(!zulaessig){
			zulaessig  = true;
			sum        = 0;
			for(int i=0;i<svm.vectors.size()-1;i++){
				SupportVector v = svm.vectors.get(i);
				v.alpha         = Math.random();
				sum            += v.alpha*v.sign();
			}
			SupportVector v = svm.vectors.get(svm.vectors.size()-1);
			v.alpha = -sum*v.sign();
			if(v.alpha < 0)zulaessig = false;
		}
	}
    
    public void move(ExtendedSVM tmp){
    	boolean zulaessig = false;
    	double mut, sum;
    	while(!zulaessig){
    		sum       = 0;
    		zulaessig = true;
    		for(int i=0;i<tmp.vectors.size()-1;i++){
    			tmp.vectors.get(i).alpha = svm.vectors.get(i).alpha;
    			mut = Math.random()*delta;
    			if(Math.random()<0.5)mut *=-1;
    			tmp.vectors.get(i).alpha = tmp.vectors.get(i).alpha - mut;
    			if(tmp.vectors.get(i).alpha < SVM.EPSILON)tmp.vectors.get(i).alpha = 0;
    			sum += tmp.vectors.get(i).alpha*tmp.vectors.get(i).sign();
    		}
    		tmp.vectors.get(tmp.vectors.size()-1).alpha = -sum*tmp.vectors.get(tmp.vectors.size()-1).sign();
			if(tmp.vectors.get(tmp.vectors.size()-1).alpha < 0)zulaessig = false;
    	}
    }
    
    
    public static void copyAll(ExtendedSVM von, ExtendedSVM nach){
    	nach.vectors = new ArrayList<>();
    	for(int i=0;i<von.vectors.size();i++) {
    		int dim = von.vectors.get(i).x.length;
    		double[] vec = new double[dim];
    		for(int j=0;j<dim;j++){
    			double xvon = von.vectors.get(i).x[j];
    			vec[j] = xvon;
    		}
    		SupportVector s = new SupportVector(vec, von.vectors.get(i).y);
    		s.alpha = von.vectors.get(i).alpha;
    		s.bound = von.vectors.get(i).bound;
    		nach.vectors.add(s);
    	}
    	nach.b = von.b;
    	nach.c = von.c;
    	nach.obj = von.obj;
    }
    
    public static void copyTeil(ExtendedSVM von, ExtendedSVM nach){
    	for(int i=0;i<von.vectors.size();i++) {
    		nach.vectors.get(i).alpha = von.vectors.get(i).alpha;
    	}
    	nach.b = von.b;
    	nach.c = von.c;
    	nach.obj = von.obj;
    }
}

class ExtendedSVM extends SVM {

	public static final double EPSILON = 1e-5;//1e-7;

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
						q = w.alpha * w.sign() * kernelFunc(v.x, w.x);
						erg += q;
					}
				}
				bw = v.sign() - erg;
				anz++;
				sum += bw;
			}
		}
		//b = -sum/anz;
		b = sum/anz;
	}

	public void dualObjectiveFunction(){
		//Berechnet Funktionswert
		double erg1 = 0;
		double erg2 = 0;
		boolean check = true;
		double  wert  = 0;
		for(SupportVector i : vectors) {
			//if(i.alpha < 0)System.out.println("----------------------------------");
			wert += i.alpha*i.sign();
			if(i.alpha > EPSILON){
				erg1 = erg1 + i.alpha;
			}
		}
		//if(Math.abs(wert-0.0001) > 0.0001)System.out.println("+++++++++++++++++++++++++++++" + wert);

		for(SupportVector i : vectors) {
			if(i.alpha > EPSILON){
				for(SupportVector j : vectors) {
					if(j.alpha > EPSILON){
						erg2 = erg2 + (i.alpha * j.alpha * i.sign() * j.sign() * kernelFunc(i.x, j.x));
					}
				}
			}
		}
		this.obj = erg1 - (0.5*erg2);
	}
}

