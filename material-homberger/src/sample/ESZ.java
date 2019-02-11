package sample;

import java.util.ArrayList;
import java.util.List;


public class ESZ {
    private SVM svm;
    private final int iMax = 10000000;
    private double delta   = 0.00001;//0.00001
    
     

    public ESZ(SVM svm) {
        this.svm = svm;
        this.alphaInit();
        svm.dualObjectiveFunction();       //svm.ausgabe();
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
				sum            += v.alpha*v.y;
			}
			SupportVector v = svm.vectors.get(svm.vectors.size()-1);	 
			v.alpha = -sum*v.y;
			if(v.alpha < 0)zulaessig = false;
		}
	}
    
    public void run() {    	
        SVM tmp = new SVM();
        copyAll(svm, tmp);
        
		for(int i=0;i<iMax;i++){
			move(tmp);
			tmp.dualObjectiveFunction();

			if(tmp.obj > svm.obj)copyTeil(tmp, svm);
				
			
			//if(i%100==0)System.out.println(i + " obj" + svm.obj);
		}
		svm.bBerechnen();
		svm.ausgabe();
    }
    
    public void move(SVM tmp){
    	boolean zulaessig = false;
    	double mut, dif, sum;
    	while(!zulaessig){
    		sum       = 0;
    		zulaessig = true;
    		for(int i=0;i<tmp.vectors.size()-1;i++){
    			tmp.vectors.get(i).alpha = svm.vectors.get(i).alpha;
    			mut = Math.random()*delta;
    			if(Math.random()<0.5)mut *=-1;
    			tmp.vectors.get(i).alpha = tmp.vectors.get(i).alpha - mut;
    			if(tmp.vectors.get(i).alpha < SVM.EPSILON)tmp.vectors.get(i).alpha = 0;
    			sum += tmp.vectors.get(i).alpha*tmp.vectors.get(i).y;
    		}
    		tmp.vectors.get(tmp.vectors.size()-1).alpha = -sum*tmp.vectors.get(tmp.vectors.size()-1).y;
			if(tmp.vectors.get(tmp.vectors.size()-1).alpha < 0)zulaessig = false;
    	}
    	
    	sum = 0;
//    	for(int i=0;i<tmp.vectors.size();i++){
//			sum += tmp.vectors.get(i).alpha*tmp.vectors.get(i).y;
//			System.out.println(i + " " + tmp.vectors.get(i).alpha);
//		}
//    	System.out.println(sum);
//    	System.out.println();  	
    	
    }
    
    
    public void move2(SVM tmp){
    	boolean zulaessig = false;
    	double mut, sum;
    	
    	while(!zulaessig){
    		zulaessig = true;
    		for(int i=0;i<tmp.vectors.size();i++){
    			tmp.vectors.get(i).alpha = svm.vectors.get(i).alpha;
    		}

        	int pos1  = (int)(tmp.vectors.size()*Math.random());
        	int pos2  = (int)(tmp.vectors.size()*Math.random());
        	while(pos1==pos2)pos2 = (int)(tmp.vectors.size()*Math.random());
    		mut = Math.random()*delta;
    		if(Math.random()<0.5)mut *=-1;
    		sum = (tmp.vectors.get(pos1).alpha*tmp.vectors.get(pos1).y) + (tmp.vectors.get(pos2).alpha*tmp.vectors.get(pos2).y); 
    		
    		tmp.vectors.get(pos1).alpha = tmp.vectors.get(pos1).alpha - mut;
    		if(tmp.vectors.get(pos1).alpha < SVM.EPSILON)tmp.vectors.get(pos1).alpha = 0;
    		tmp.vectors.get(pos2).alpha = (sum - (tmp.vectors.get(pos1).alpha*tmp.vectors.get(pos1).y))/tmp.vectors.get(pos2).y;
    		if(tmp.vectors.get(pos2).alpha < 0)zulaessig = false;
    	}
           	
    }
    
    
    public static void copyAll(SVM von, SVM nach){
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
    
    public static void copyTeil(SVM von, SVM nach){
    	for(int i=0;i<von.vectors.size();i++) {
    		nach.vectors.get(i).alpha = von.vectors.get(i).alpha;
    	}
    	nach.b = von.b;
    	nach.c = von.c;
    	nach.obj = von.obj;
    }
}

