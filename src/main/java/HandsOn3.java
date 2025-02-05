import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class HandsOn3 extends JPanel {
    private final List<Integer> nValues;
    private final List<Long> originalTimes;
    private final List<Long> modifiedTimes;

    public HandsOn3(List<Integer> nValues, List<Long> originalTimes, List<Long> modifiedTimes) {
        this.nValues = nValues;
        this.originalTimes = originalTimes;
        this.modifiedTimes = modifiedTimes;
    }

    // 绘制曲线和坐标轴
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        int pointRadius = 5;

        long maxTime = Math.max(originalTimes.get(originalTimes.size() - 1), modifiedTimes.get(modifiedTimes.size() - 1));

        // 画坐标轴
        g2.drawLine(padding, height - padding, width - padding, height - padding); // X 轴
        g2.drawLine(padding, height - padding, padding, padding); // Y 轴

        // 添加 X 轴标签 ("n")
        g2.drawString("n (Input Size)", width / 2, height - 10);

        // 添加 Y 轴标签 ("Time (ns)")
        g2.rotate(-Math.PI / 2); // 旋转90度，使文本竖直
        g2.drawString("Time (ns)", -height / 2, 20);
        g2.rotate(Math.PI / 2); // 旋转回来

        // 画 X 轴刻度
        for (int i = 0; i < nValues.size(); i++) {
            int x = padding + (i * (width - 2 * padding)) / (nValues.size() - 1);
            g2.drawLine(x, height - padding, x, height - padding + 5);
            g2.drawString(nValues.get(i).toString(), x - 10, height - padding + 20);
        }

        // 画 Y 轴刻度
        int numYTicks = 5;
        for (int i = 0; i <= numYTicks; i++) {
            long yValue = maxTime * i / numYTicks;
            int y = height - padding - (int) ((yValue * (height - 2 * padding)) / maxTime);
            g2.drawLine(padding - 5, y, padding, y);
            g2.drawString(Long.toString(yValue), padding - 40, y + 5);
        }

        // 画数据点和曲线
        drawCurve(g2, nValues, originalTimes, Color.BLUE, pointRadius);
        drawCurve(g2, nValues, modifiedTimes, Color.RED, pointRadius);

        // 图例
        g2.setColor(Color.BLUE);
        g2.drawString("Original Function", width - 150, padding + 20);
        g2.fillOval(width - 170, padding + 15, 10, 10);

        g2.setColor(Color.RED);
        g2.drawString("Modified Function", width - 150, padding + 40);
        g2.fillOval(width - 170, padding + 35, 10, 10);
    }


    // 画曲线
    private void drawCurve(Graphics2D g2, List<Integer> xData, List<Long> yData, Color color, int pointRadius) {
        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        long maxTime = Math.max(originalTimes.get(originalTimes.size() - 1), modifiedTimes.get(modifiedTimes.size() - 1));

        g2.setColor(color);
        for (int i = 0; i < xData.size() - 1; i++) {
            int x1 = padding + (i * (width - 2 * padding)) / (xData.size() - 1);
            int y1 = height - padding - (int) ((yData.get(i) * (height - 2 * padding)) / maxTime);
            int x2 = padding + ((i + 1) * (width - 2 * padding)) / (xData.size() - 1);
            int y2 = height - padding - (int) ((yData.get(i + 1) * (height - 2 * padding)) / maxTime);
            g2.fillOval(x1 - pointRadius, y1 - pointRadius, 2 * pointRadius, 2 * pointRadius);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    // 计算原始函数运行时间
    public static long originalFunction(int n) {
        long startTime = System.nanoTime();
        int x = 1;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                x = x + 1;
            }
        }
        return System.nanoTime() - startTime;
    }

    // 计算修改后函数运行时间
    public static long modifiedFunction(int n) {
        long startTime = System.nanoTime();
        int x = 1, y = 1;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                x = x + 1;
                y = i + j;
            }
        }
        return System.nanoTime() - startTime;
    }

    public static void main(String[] args) {
        int[] testArray = {5, 2, 4, 7, 1, 3, 2, 6};
        System.out.println("Original Array: " + Arrays.toString(testArray));
        mergeSort(testArray, 0, testArray.length - 1);
        System.out.println("Sorted Array: " + Arrays.toString(testArray));

        List<Integer> nValues = new ArrayList<>();
        for (int n = 1; n <= 500; n *= 2) {  // 让 n 最高到 2048
            nValues.add(n);
        }

        List<Long> originalTimes = new ArrayList<>();
        List<Long> modifiedTimes = new ArrayList<>();

        for (int n : nValues) {
            long timeOriginal = originalFunction(n);
            long timeModified = modifiedFunction(n);
            originalTimes.add(timeOriginal);
            modifiedTimes.add(timeModified);
            System.out.printf("n = %d -> Original: %d ns, Modified: %d ns%n", n, timeOriginal, timeModified);
        }

        fitPolynomial(nValues, originalTimes);
        fitPolynomial(nValues, modifiedTimes);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Execution Time Plot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(new HandsOn3(nValues, originalTimes, modifiedTimes));
            frame.setVisible(true);
        });
    }

    // 归并排序
    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;

        while (i <= mid && j <= right) temp[k++] = (arr[i] <= arr[j]) ? arr[i++] : arr[j++];
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];

        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    // 拟合二次多项式
    public static void fitPolynomial(List<Integer> xValues, List<Long> yValues) {
        WeightedObservedPoints points = new WeightedObservedPoints();
        for (int i = 0; i < xValues.size(); i++) points.add(xValues.get(i), yValues.get(i));
        double[] coefficients = PolynomialCurveFitter.create(2).fit(points.toList());
        System.out.printf("Fitted Polynomial: %.3e * n² + %.3e * n + %.3e%n", coefficients[0], coefficients[1], coefficients[2]);
    }
}
