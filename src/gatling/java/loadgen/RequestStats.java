package loadgen;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestStats implements Cloneable {
    private final Queue<BigDecimal> values;

    public RequestStats() {
        this.values = new ConcurrentLinkedQueue<>();
    }

    public synchronized void addRequest(long time) {
        var bigTime = new BigDecimal(BigInteger.valueOf(time)).setScale(12, RoundingMode.HALF_DOWN);
        values.add(bigTime);
    }

    public BigDecimal getTotalTime(int n) {
        return values.stream()
                .limit(n)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAverageTime(int n) {
        if (n < 2) return BigDecimal.ZERO;

        var totalTime = getTotalTime(n);

        return totalTime.divide(BigDecimal.valueOf(n), RoundingMode.HALF_DOWN);
    }

    public BigDecimal getVariance(int n) {
        if (n < 2) return BigDecimal.ZERO;

        var sum = BigDecimal.ZERO;
        var sumSq = BigDecimal.ZERO;

        var iterator = values.stream()
                .limit(n)
                .iterator();
        while (iterator.hasNext()) {
            var x = iterator.next();
            sum = sum.add(x);
            sumSq = sumSq.add(x.pow(2));
        }

        var nDecimal = BigDecimal.valueOf(n);
        var mean = sum.divide(nDecimal, RoundingMode.HALF_DOWN);
        return sumSq.divide(nDecimal, RoundingMode.HALF_DOWN)
                .subtract(mean.pow(2));
    }

    public BigDecimal getPercentile90(int n) {
        return getPercentile(n, 0.90);
    }

    public BigDecimal getPercentile95(int n) {
        return getPercentile(n, 0.95);
    }

    public BigDecimal getPercentile99(int n) {
        return getPercentile(n, 0.99);
    }

    private BigDecimal getPercentile(int n, double x) {
        if (n == 0) {
            return BigDecimal.ZERO;
        }

        var sorted = new ArrayList<>(values.stream().limit(n).toList());
        Collections.sort(sorted);
        var index = (int) Math.ceil(x * n) - 1;
        index = Math.min(index, n - 1);

        return sorted.get(index);
    }

    public BigDecimal getMin(int n) {
        if (n == 0) {
            return BigDecimal.ZERO;
        }
        return values.stream()
                .limit(n)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getMax(int n) {
        if (n == 0) {
            return BigDecimal.ZERO;
        }
        return values.stream()
                .limit(n)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public String toString(int n) {
        return String.format("%-15d %-20.2f %-20.2f %-20.2f %-16.2f %-16.2f %-16.2f %-16.2f %-16.2f",
                n,
                getTotalTime(n).doubleValue(),
                getAverageTime(n).doubleValue(),
                getVariance(n).doubleValue(),
                getPercentile90(n).doubleValue(),
                getPercentile95(n).doubleValue(),
                getPercentile99(n).doubleValue(),
                getMin(n).doubleValue(),
                getMax(n).doubleValue()
        );
    }
}
