package sample;

public class ESZ {

	private static final double EPSILON = 1e-5; // 1e-7;
	private static final int ITERATIONS = 10000000;
	private static final double DELTA = 0.00001;

	class AlphaSet {
		double[] alphas;
		double score;
	}

	@FunctionalInterface
	public interface AlphaGenerator {
		double generate(int index);
	}

	private SVM svm;
	AlphaSet resultSet;
	private int vectorCount;

    public ESZ(SVM svm) {
		svm.epsilon = EPSILON;
        this.svm = svm;
		this.vectorCount = svm.vectors.size();
    }
    
    public void run() {
		resultSet = this.generateSet(i -> Math.random());

		for(int s=0; s<ITERATIONS; s++) {
			AlphaSet evolvedSet = this.generateSet(i -> this.offsetRandomly(resultSet.alphas[i]));
			if(evolvedSet.score > resultSet.score)
				resultSet = evolvedSet;
		}

		this.apply(resultSet);
    }

	private AlphaSet generateSet(AlphaGenerator generator) {

		double[] alphas = new double[vectorCount];

		// try out alpha combinations until one is valid
		while(true) {
			int constrainedIndex = vectorCount - 1;
			double sum = 0;
			for (int i = 0; i < vectorCount; i++) {
				SupportVector v = svm.vectors.get(i);
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

		for(int i = 0; i < vectorCount; i++) {
			double ai = alphas[i];
			if(ai < EPSILON) {
				continue;
			}
			s1 += ai;
			SupportVector vi = svm.vectors.get(i);
			for(int j = 0; j < vectorCount; j++) {
				double aj = alphas[j];
				if(aj < EPSILON) {
					continue;
				}
				SupportVector vj = svm.vectors.get(j);
				s2 += (ai * aj * vi.sign() * vj.sign() * svm.kernelFunc(vi.x, vj.x));
			}
		}

		AlphaSet set = new AlphaSet();
		set.alphas = alphas;
		set.score = s1 - (0.5 * s2);
		return set;
	}

	private double offsetRandomly(double alpha) {
		double offset = Math.random() * DELTA;
		if (Math.random()<0.5) offset *= -1;
		double newAlpha = alpha - offset;
		if (newAlpha < EPSILON) {
			return 0;
		} else {
			return newAlpha;
		}
	}

	private void apply(AlphaSet set) {
		for (int i = 0; i < vectorCount; i++) {
			svm.vectors.get(i).alpha = set.alphas[i];
		}
	}
}
