package sample;

import sample.kernels.Kernel;

import java.util.ArrayList;
import java.util.List;

public class SVM {

    public List<FeatureVector> vectors;
    public Kernel kernel;
    public double epsilon;
    public double b = 0;

    public List<FeatureVector> getSupportVectors() {
        List<FeatureVector> featureVectors = new ArrayList<>();
        for (FeatureVector v: vectors) {
            if (v.alpha > epsilon)
                featureVectors.add(v);
        }
        return featureVectors;
    }

    public double output(double[] x) {
        List<FeatureVector> supportVectors = this.getSupportVectors();
        double h = b;
        for(FeatureVector v : supportVectors) {
            h += v.alpha * v.sign() * kernel.apply(v.x, x);
        }
        return h;
    }

    public void updateB() {
        List<FeatureVector> supportVectors = this.getSupportVectors();
        double bsum = 0;
        for (FeatureVector i: supportVectors) {
            double subsum = 0;
            for(FeatureVector j: supportVectors) {
                subsum += j.alpha * j.sign() * kernel.apply(i.x, j.x);
            }
            bsum += i.sign() - subsum;
        }
        b = bsum / supportVectors.size();
    }

    public void updateBAlternative() {
        List<FeatureVector> supportVectors = this.getSupportVectors();
        double[] w = new double[2];
        for (FeatureVector v : supportVectors) {
            w[0] += v.alpha * v.sign() * v.x[0];
            w[1] += v.alpha * v.sign() * v.x[1];
        }
        double bsum = 0;
        for (FeatureVector v : supportVectors) {
            bsum += v.sign() - kernel.apply(v.x, w);
        }
        b = bsum / supportVectors.size();
    }

    public double assessAccuracy(List<FeatureVector> testVectors) {
        int correctClassifications = 0;
        for (FeatureVector v: testVectors) {
            if (Math.signum(this.output(v.x)) == v.sign())
                correctClassifications++;
        }
        return correctClassifications / testVectors.size();
    }
}