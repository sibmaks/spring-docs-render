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

    private BigDecimal totalTime = BigDecimal.ZERO;

    public RequestStats() {
        this.values = new ConcurrentLinkedQueue<>();
    }

    public void addRequest(long time) {
        var bigTime = new BigDecimal(BigInteger.valueOf(time), 12);
        totalTime = totalTime.add(bigTime);
        values.add(bigTime);
    }

    public int getCount() {
        return values.size();
    }

    public BigDecimal getTotalTime() {
        return totalTime;
    }

    public BigDecimal getAverageTime() {
        var n = values.size();
        if (n < 2) return BigDecimal.ZERO;

        return totalTime.divide(BigDecimal.valueOf(n), RoundingMode.HALF_DOWN);
    }

    public BigDecimal getVariance() {
        var n = values.size();
        if (n < 2) return BigDecimal.ZERO;

        var sum = BigDecimal.ZERO;
        var sumSq = BigDecimal.ZERO;

        for (var x : values) {
            sum = sum.add(x);
            sumSq = sumSq.add(x.pow(2));
        }

        var mean = sum.divide(BigDecimal.valueOf(n), RoundingMode.HALF_DOWN);
        return sumSq.divide(BigDecimal.valueOf(n), RoundingMode.HALF_DOWN)
                .subtract(mean.pow(2));
    }

    public BigDecimal getPercentile90() {
        return getPercentile(0.90);
    }

    public BigDecimal getPercentile95() {
        return getPercentile(0.95);
    }

    public BigDecimal getPercentile99() {
        return getPercentile(0.99);
    }

    private BigDecimal getPercentile(double x) {
        var n = values.size();
        if (n == 0) {
            return BigDecimal.ZERO;
        }

        var sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        var index = (int) Math.ceil(x * n) - 1;
        index = Math.min(index, n - 1);

        return sorted.get(index);
    }

    public BigDecimal getMin() {
        var n = values.size();
        if (n == 0) {
            return BigDecimal.ZERO;
        }
        return values.stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getMax() {
        var n = values.size();
        if (n == 0) {
            return BigDecimal.ZERO;
        }
        return values.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public String toString() {
        return String.format("%-15d %-20.2f %-20.2f %-20.2f %-16.2f %-16.2f %-16.2f %-16.2f %-16.2f",
                getCount(),
                getTotalTime().doubleValue(),
                getAverageTime().doubleValue(),
                getVariance().doubleValue(),
                getPercentile90().doubleValue(),
                getPercentile95().doubleValue(),
                getPercentile99().doubleValue(),
                getMin().doubleValue(),
                getMax().doubleValue()
        );
    }
}
