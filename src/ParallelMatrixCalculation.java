import java.util.concurrent.*;
import java.util.concurrent.Exchanger;
import java.util.Arrays;

class InputProcessor implements Runnable {
    private final Exchanger<Integer> exchangerAB;
    private final Exchanger<int[]> exchangerBMZ;
    private final int a;
    private final int[] B;
    private final int[][] MZ;

    public InputProcessor(Exchanger<Integer> exchangerAB, Exchanger<int[]> exchangerBMZ, int a, int[] B, int[][] MZ) {
        this.exchangerAB = exchangerAB;
        this.exchangerBMZ = exchangerBMZ;
        this.a = a;
        this.B = B;
        this.MZ = MZ;
    }

    @Override
    public void run() {
        try {
            // Відправлення даних B та MZ
            exchangerBMZ.exchange(B);
            exchangerBMZ.exchange(flattenMatrix(MZ)); // Flatten the matrix

            // Отримання та виведення результату a
            int result = exchangerAB.exchange(a);
            System.out.println("Result: " + result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // Helper method to flatten a 2D array into a 1D array
    private int[] flattenMatrix(int[][] matrix) {
        return Arrays.stream(matrix)
                .flatMapToInt(Arrays::stream)
                .toArray();
    }
    // Helper method to flatten a 1D array
    private int[] flattenVector(int[] vector) {
        return vector;
    }
}

class MatrixProcessor implements Runnable {
    private final int id;
    private final Exchanger<int[]> exchangerBMZ;

    public MatrixProcessor(int id, Exchanger<int[]> exchangerBMZ) {
        this.id = id;
        this.exchangerBMZ = exchangerBMZ;
    }

    @Override
    public void run() {
        try {
            // Отримання даних B та MZ
            int[] B = exchangerBMZ.exchange(null);
            int[][] MZ = exchangerBMZ.exchange(null);

            // Виконання обчислень (замість min, використовуйте власний алгоритм)
            int result = 0;
            for (int i = 0; i < B.length; i++) {
                for (int j = 0; j < MZ.length; j++) {
                    result += B[i] * MZ[j][i];
                }
            }

            // Відправлення результату
            System.out.println("Thread " + id + ": " + result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class VectorProcessor implements Runnable {
    private final int id;
    private final Exchanger<Integer> exchangerAB;

    public VectorProcessor(int id, Exchanger<Integer> exchangerAB) {
        this.id = id;
        this.exchangerAB = exchangerAB;
    }

    @Override
    public void run() {
        try {
            // Отримання даних a
            int a = exchangerAB.exchange(null);

            // Виконання обчислень (замість min, використовуйте власний алгоритм)
            int result = a * id;

            // Відправлення результату
            exchangerAB.exchange(result);
            System.out.println("Thread " + id + ": " + result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
public class ParallelMatrixCalculation {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // Отримання введених даних (a, B, MZ)
        int a = 0; // Призначте значення a
        int[] B = {1, 2, 3}; // Призначте значення вектора B
        int[][] MZ = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}; // Призначте значення матриці MZ

        // Конфігурація обмінників для передачі даних між потоками
        Exchanger<Integer> exchangerAB = new Exchanger<>();
        Exchanger<int[]> exchangerBMZ = new Exchanger<>();

        // Запуск потоків
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(new InputProcessor(exchangerAB, exchangerBMZ, a, B, MZ));
        executorService.submit(new MatrixProcessor(1, exchangerBMZ));
        executorService.submit(new MatrixProcessor(2, exchangerBMZ));
        executorService.submit(new VectorProcessor(3, exchangerAB));

        // Очікування завершення потоків
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Total execution time: " + executionTime + " milliseconds");

        // Теоретичний обрахунок коефіцієнту прискорення
        int theoreticalSpeedup = 4; // Кількість потоків
        System.out.println("Theoretical speedup: " + theoreticalSpeedup);

        // Обрахунок реального коефіцієнту прискорення (загальний та для паралельної частини)
        double totalSpeedup = (double) executionTime / (double) theoreticalSpeedup;
        double parallelSpeedup = (double) executionTime / (double) (executionTime - theoreticalSpeedup);
        System.out.println("Total speedup: " + totalSpeedup);
        System.out.println("Parallel speedup: " + parallelSpeedup);
    }

}