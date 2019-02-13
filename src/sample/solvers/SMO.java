package sample.solvers;

import sample.FeatureVector;
import sample.SVM;

import java.util.*;

public class SMO implements Solver {

    private static final Random random = new Random();

    private double c;
    private double tolerance = 1e-3; // TODO: make configurable
    private SVM svm;
    private Map<FeatureVector,Double> lastErrors = null;

    public SMO(double c) {
        this.c = c;
    }

    public void solve(SVM svm) {
        this.svm = svm;

        int numChanged = 0;
        boolean examineAll = true; // examine entire training set initially
        while(numChanged > 0 || examineAll) {
            numChanged = 0;
            for(FeatureVector v : svm.vectors)
                if((examineAll || !v.bound) && examineExample(v))
                    numChanged++;
            if(examineAll)
                // only examine non-bound examples in next pass
                examineAll = false;
            else if(numChanged == 0)
                // all of the non-bound examples satisfy the KKT conditions,
                // so examine the entire training set again
                examineAll = true;
        }
    }

    private boolean examineExample(FeatureVector v) {
        if(satisfiesKKTConditions(v)) // not eligible for optimisation
            return false;
        // choose a vector with the second choice heuristic
        if(!this.allErrors().isEmpty() && takeStep(secondChoice(error(v)), v))
            return true;
        // the heuristic did not make positive progress,
        // so try all non-bound examples
        final int n = svm.vectors.size();
        final int pos = random.nextInt(n); // iterate from random position
        for(FeatureVector v1 : svm.vectors.subList(pos, n))
            if(!v1.bound && takeStep(v1, v))
                return true;
        for(FeatureVector v1 : svm.vectors.subList(0, pos))
            if(!v1.bound && takeStep(v1, v))
                return true;
        // positive progress was not made, so try entire training set
        for(FeatureVector v1 : svm.vectors.subList(pos, n))
            if(v1.bound && takeStep(v1, v))
                return true;
        for(FeatureVector v1 : svm.vectors.subList(0, pos))
            if(v1.bound && takeStep(v1, v))
                return true;
        // no adequate second example exists, so pick another first example
        return false;
    }

    private Map<FeatureVector, Double> allErrors() {
        if (lastErrors != null) {
            return lastErrors;
        }
        Map<FeatureVector,Double> allErrors = new HashMap<>();
        for (FeatureVector v: svm.vectors) {
            if (v.bound) {
                continue;
            }
            allErrors.put(v, this.error(v));
        }
        lastErrors = allErrors;
        return allErrors;
    }

    private double error(FeatureVector v) {
        if (lastErrors != null && lastErrors.containsKey(v)) {
            return lastErrors.get(v);
        }
        return svm.output(v.x) - v.y;
    }

    private boolean satisfiesKKTConditions(FeatureVector v) {
        final double r = error(v) * v.y; // (u-y)*y = y*u-1
        if (r < -tolerance && v.alpha < c) {
            return false;
        } else if (r > tolerance && v.alpha > 0) {
            return false;
        } else {
            return true;
        }
    }

    private FeatureVector secondChoice(double error) {
        Map<FeatureVector,Double> allErrors = this.allErrors();
        Map.Entry<FeatureVector,Double> best = null;
        if(error > 0) { // return vector with minimum error
            for(Map.Entry<FeatureVector,Double> entry : allErrors.entrySet())
                if(best == null || entry.getValue() < best.getValue())
                    best = entry;
        } else { // return vector with maximum error
            for(Map.Entry<FeatureVector,Double> entry : allErrors.entrySet())
                if(best == null || entry.getValue() > best.getValue())
                    best = entry;
        }
        return best.getKey();
    }

    private boolean takeStep(FeatureVector v1, FeatureVector v2) {
        if(v1.x == v2.x)
            // identical inputs cause objective function to become
            // semi-definite, so positive progress cannot be made
            return false;
        final double alpha1 = v1.alpha, alpha2 = v2.alpha;
        final double y1 = v1.y, y2 = v2.y;

        // endpoints (in terms of values of alpha2) of the diagonal line
        // segment representing the constraint between the two alpha values
        double l, h;
        if(y1 != y2) {
            // equation (12.3)
            l = Math.max(0, alpha2 - alpha1);
            h = Math.min(c, c + alpha2 - alpha1);
        } else /* v1.y == v2.y */ {
            // equation (12.4)
            l = Math.max(0, alpha2 + alpha1 - c);
            h = Math.min(c, alpha2 + alpha1);
        }
        if(l == h) // the alpha values are constrained to a single point
            return false;

        final double k11 = svm.kernel.apply(v1.x, v1.x),
                k12 = svm.kernel.apply(v1.x, v2.x),
                k22 = svm.kernel.apply(v2.x, v2.x);
        final double s = y1 * y2;
        final double e1 = error(v1), e2 = error(v2);

        // second derivative of the objective function along the diagonal line
        final double eta = k11 + k22 - 2*k12; // equation (12.5)
        // unusual circumstances
        if(eta < 0)
            throw new RuntimeException(
                    "The kernel function does not obey Mercer's condition");
        if(eta == 0) // two training examples have the same input vector
            return false;
        // normal circumstances - the objective function is positive
        // definite and there is a minimum along the diagonal line
        v2.alpha = alpha2 + y2 * (e1-e2) / eta; // equation (12.6)
        v2.alpha = this.clamp(v2.alpha, l, h); // equation (12.7)

        double diff = Math.abs(v2.alpha - alpha2);
        if(diff < svm.epsilon*(v2.alpha + alpha2 + svm.epsilon)) {
            // change in alpha2 was too small
            v2.alpha = alpha2;
            return false;
        }

        v1.alpha = alpha1 + s*(alpha2-v2.alpha); // equation (12.8)
        v1.bound = this.isBound(v1.alpha);
        v2.bound = this.isBound(v2.alpha);

        lastErrors = null; // invalidate cached errors

        return true;
    }

    boolean isBound(double alpha) {
        return this.equalDoubles(alpha,0) || this.equalDoubles(alpha,c);
    }

    boolean equalDoubles(double a, double b) {
        return Math.abs(a - b) <= 0.00001;
    }

    double clamp(double x, double low, double high) {
        return Math.min(Math.max(x, low), high);
    }
}
