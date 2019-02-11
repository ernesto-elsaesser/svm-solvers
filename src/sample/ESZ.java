package sample;

import java.util.*;

public class ESZ {

	private SVM svm;
    private final int iMax = 10000000;

	public static final double EPSILON = 1e-5;//1e-7;
	public static final double C = 1000; // large c -> narrow margin

	@FunctionalInterface
	public interface AlphaGenerator {
		double generateFor(SupportVector v);
	}

	private class Evolver implements AlphaGenerator {

		private double delta   = 0.00001;

		public double generateFor(SupportVector v) {
			double offset = Math.random() * delta;
			if (Math.random()<0.5) offset *= -1;
			return v.alpha - offset;
		}
	}

    public ESZ(SVM svm) {
        this.svm = svm;
    }
    
    public void run() {
		List<SupportVector> resultVectors = this.clone(svm.vectors, v -> Math.random());

		double score = this.calculateObjective(resultVectors);
		AlphaGenerator evolver = new Evolver();

		for(int i=0; i<iMax; i++){
			List<SupportVector> evolvedVectors = this.clone(resultVectors, evolver);
			double newScore = this.calculateObjective(evolvedVectors);
			if(newScore > score) resultVectors = evolvedVectors;
		}

		svm.vectors = resultVectors;
		svm.b = this.calculateB(resultVectors);
    }

	private List<SupportVector> clone(List<SupportVector> vectors, AlphaGenerator generator) {
		while(true) {
			List<SupportVector> clonedVectors = new ArrayList<>();
			int constrainedIndex = 0; // TODO: chose randomly
			double sum = 0;
			for (int i = 0; i < vectors.size(); i++) {
				SupportVector v = vectors.get(i);
				double newAlpha = 0;
				if (i != constrainedIndex) {
					newAlpha = generator.generateFor(v);
					sum += newAlpha * v.sign();
				}
				clonedVectors.add(v.clone(newAlpha));
			}
			SupportVector constrainedVector = clonedVectors.get(constrainedIndex);
			constrainedVector.alpha = -sum * constrainedVector.sign();
			if(constrainedVector.alpha > 0) {
				return clonedVectors;
			}
		}
	}

	private double calculateObjective(List<SupportVector> vectors){
		double s1 = 0;
		double s2 = 0;

		for(SupportVector i : vectors) {
			if(i.alpha < EPSILON) {
				continue;
			}
			s1 += i.alpha;
			for(SupportVector j : vectors) {
				if(j.alpha > EPSILON){
					s2 += (i.alpha * j.alpha * i.sign() * j.sign() * svm.kernelFunc(i.x, j.x));
				}
			}
		}

		return s1 - (0.5 * s2);
	}

	private double calculateB(List<SupportVector> vectors){
		int    anz = 0;
		double sum = 0;
		for (SupportVector i : vectors) {
			if (i.alpha > EPSILON && i.alpha < C) {
				double erg = 0;
				for (SupportVector j : vectors) {
					if (j.alpha > EPSILON && j.alpha < C) {
						erg += j.alpha * j.sign() * svm.kernelFunc(i.x, j.x);
					}
				}
				double bw = i.sign() - erg;
				anz++;
				sum += bw;
			}
		}
		return sum / anz;
	}
}
