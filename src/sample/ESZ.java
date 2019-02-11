package sample;

public class ESZ {

	private static final int ITERATIONS = 10000000;
	private static final double EPSILON = 1e-5; // 1e-7;
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
	private int vectorCount;

    public ESZ(SVM svm) {
        this.svm = svm;
		this.vectorCount = svm.vectors.size();
    }
    
    public void run() {
		AlphaSet currentSet = new AlphaSet();
		currentSet.alphas = new double[vectorCount];
		AlphaSet evolvedSet = new AlphaSet();
		evolvedSet.alphas = new double[vectorCount];

		this.fillSet(currentSet, i -> Math.random());

		for(int s=0; s<ITERATIONS; s++) {
			final double[] as = currentSet.alphas;
			this.fillSet(evolvedSet, i -> this.offsetRandomly(as[i]));
			if(evolvedSet.score > currentSet.score) currentSet = evolvedSet;
		}

		this.apply(currentSet);
    }

	private void fillSet(AlphaSet set, AlphaGenerator generator) {

		// try out alpha combination until one is valid
		while(true) {
			int constrainedIndex = 0; // TODO: chose randomly
			double sum = 0;
			for (int i = 0; i < vectorCount; i++) {
				SupportVector v = svm.vectors.get(i);
				double newAlpha = 0;
				if (i != constrainedIndex) {
					newAlpha = generator.generate(i);
					sum += newAlpha * v.sign();
				}
				set.alphas[i] = newAlpha;
			}
			set.alphas[constrainedIndex] = -sum * svm.vectors.get(constrainedIndex).sign();
			if(set.alphas[constrainedIndex] > 0) {
				break;
			}
		}

		// calculate objective function
		double s1 = 0;
		double s2 = 0;

		for(int i = 0; i < vectorCount; i++) {
			double ai = set.alphas[i];
			if(ai < EPSILON) {
				continue;
			}
			s1 += ai;
			SupportVector vi = svm.vectors.get(i);
			for(int j = 0; j < vectorCount; j++) {
				double aj = set.alphas[j];
				if(aj < EPSILON) {
					continue;
				}
				SupportVector vj = svm.vectors.get(j);
				s2 += (ai * aj * vi.sign() * vj.sign() * svm.kernelFunc(vi.x, vj.x));
			}
		}

		set.score = s1 - (0.5 * s2);
	}

	private double offsetRandomly(double alpha) {
		double offset = Math.random() * DELTA;
		if (Math.random()<0.5) offset *= -1;
		return alpha - offset;
	}

	private void apply(AlphaSet set) {
		double sum = 0;
		int effectiveCount = 0;
		for (int i = 0; i < vectorCount; i++) {
			double ai = set.alphas[i];
			svm.vectors.get(i).alpha = ai;
			if (ai < EPSILON) {
				continue;
			}

			SupportVector vi = svm.vectors.get(i);
			double subsum = 0;
			for(int j = 0; j < vectorCount; j++) {
				double aj = set.alphas[j];
				if(aj < EPSILON) {
					continue;
				}
				SupportVector vj = svm.vectors.get(j);
				subsum += aj * vj.sign() * svm.kernelFunc(vi.x, vj.x);
			}
			sum += vi.sign() - subsum;

			effectiveCount++;
		}
		svm.b = sum / effectiveCount;
	}
}
