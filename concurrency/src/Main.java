import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

/**
 * ForkJoinPool, ThreadLocalRandom, Parallel Streams
 */

public class Main {




    public static void main(String[] args) {

        int N = 45_678_999;

        LongStream.generate(() -> ThreadLocalRandom.current().nextInt(1000000))
                .parallel().limit(N).sum();


        ForkJoinPool pool = ForkJoinPool.commonPool();

        long started = System.nanoTime();

        RecursiveTask<Long> task = new IntegerRecursiveTask(N);

        ForkJoinTask<Long> result = pool.submit(task);

        System.out.println("Result= " + result.join());
        System.out.println("count= " + count.get());
        System.out.println("elapsed= " + (1.0 * (System.nanoTime() - started)) /TimeUnit.SECONDS.toNanos(1) );

        started = System.nanoTime();
        long result2 = LongStream.generate(() -> ThreadLocalRandom.current().nextInt(1000000))
                .parallel().limit(N).sum();
        System.out.println("Result 2 = " + result2);
        System.out.println("elapsed= " + (1.0 * (System.nanoTime() - started)) /TimeUnit.SECONDS.toNanos(1) );


    }

    static AtomicInteger count = new AtomicInteger(0);
    static Random r = new Random();

    private static class IntegerRecursiveTask extends RecursiveTask<Long> {

        private static final int NUM_TASKS = 10;
        private static final int MIN = 10;
        final int N;



        private IntegerRecursiveTask(int n) {
            N = n;
//            System.out.println("N="+N);

        }


        @Override
        protected Long compute() {

            long result  = 0;
            if(N <= MIN) {
                count.addAndGet(N);
                for (int i = 0; i < N; i++) {
                    result += ThreadLocalRandom.current().nextInt(1000000);
                }
//                System.out.println("Log " + result);
                return result;
            }


            List<IntegerRecursiveTask> taskList = new ArrayList<>(NUM_TASKS);

            int remaining = N;

            while (remaining > 0) {

                IntegerRecursiveTask task =
                        new IntegerRecursiveTask(remaining > NUM_TASKS? NUM_TASKS : remaining);
                remaining -= NUM_TASKS;
                task.fork();

                taskList.add(task);

            }

            return taskList.stream().mapToLong(task -> task.join()).sum();
        }
    }
}

