package sample.solvers;

import sample.FeatureVector;
import sample.SVM;

public class ESZ implements Solver { ;

	class AlphaSet {
		double[] alphas;
		double score;
	}

	@FunctionalInterface
	public interface AlphaGenerator {
		double generate(int index);
	}

	private int iterations;
	private double delta;

	private SVM svm;
	AlphaSet resultSet;

	public ESZ(int iterations, double delta) {
		this.iterations = iterations;
		this.delta = delta;
	}
    
    public void solve(SVM svm) {
    	this.svm = svm;
		this.resultSet = this.generateSet(i -> Math.random());

		for(int s=0; s<iterations; s++) {
			AlphaSet evolvedSet = this.generateSet(i -> this.offsetRandomly(resultSet.alphas[i]));
			if(evolvedSet.score > resultSet.score)
				resultSet = evolvedSet;
		}

		this.apply(resultSet);
    }

	private AlphaSet generateSet(AlphaGenerator generator) {

    	int count = svm.vectors.size();
		double[] alphas = new double[count];

		// try out alpha combinations until one is valid
		while(true) {
			int constrainedIndex = count - 1;
			double sum = 0;
			for (int i = 0; i < count; i++) {
				FeatureVector v = svm.vectors.get(i);
				double newAlpha = 0;
				if (i != constrainedIndex) {
					newAlpha = generator.generate(i);
					sum += newAlpha * v.sign();
				}
				alphas[i] = newAlpha;
			}
			alphas[constrainedIndex] = -sum * svm.vectors.get(constrainedIndex).sign();
			if(alphas[constrainedIndex] > 0) {
				break;
			}
		}

		// calculate objective function
		double s1 = 0;
		double s2 = 0;

		for(int i = 0; i < count; i++) {
			double ai = alphas[i];
			if(ai < svm.epsilon) {
				continue;
			}
			s1 += ai;
			FeatureVector vi = svm.vectors.get(i);
			for(int j = 0; j < count; j++) {
				double aj = alphas[j];
				if(aj < svm.epsilon) {
					continue;
				}
				FeatureVector vj = svm.vectors.get(j);
				s2 += (ai * aj * vi.sign() * vj.sign() * svm.kernel.apply(vi.x, vj.x));
			}
		}

		AlphaSet set = new AlphaSet();
		set.alphas = alphas;
		set.score = s1 - (0.5 * s2);
		return set;
	}

	private double offsetRandomly(double alpha) {
		double offset = Math.random() * delta;
		if (Math.random()<0.5) offset *= -1;
		double newAlpha = alpha - offset;
		if (newAlpha < svm.epsilon) {
			return 0;
		} else {
			return newAlpha;
		}
	}

	private void apply(AlphaSet set) {
		for (int i = 0; i < svm.vectors.size(); i++) {
			svm.vectors.get(i).alpha = set.alphas[i];
		}
	}
}
