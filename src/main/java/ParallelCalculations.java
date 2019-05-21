import java.util.stream.Stream;

public class ParallelCalculations implements ParallelCalculationsInterface, ThreadControllInterface {

    private PointGeneratorInterface generator;
    private int numberOfThreads;
    private Thread[] threads;

    private volatile boolean suspended;
    private boolean terminated;

    private volatile Long sum = 0L;

    private final Object lock;

    private int[][] histogram;

    public ParallelCalculations() {
        this.histogram = new int[PointGeneratorInterface.MAX_POSITION + 1][PointGeneratorInterface.MAX_POSITION + 1];
        this.suspended = false;
        this.terminated = false;

        this.sum = 0L;
        this.lock = new Object();
    }

    public void calculateHistogram() {
        while (!terminated) {
            while (suspended) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            final PointGeneratorInterface.Point2D point2D = this.generator.getPoint();
            synchronized (this.lock) {
                    histogram[point2D.firstCoordinate][point2D.secondCoordinate]++;
                    this.sum += (point2D.firstCoordinate + point2D.secondCoordinate);
            }
            if (terminated)
                break;
        }
    }

    @Override
    public void setPointGenerator(PointGeneratorInterface generator) {
        this.generator = generator;
    }

    @Override
    public long getSum() {
        return this.sum;
    }

    @Override
    public int getCountsInBucket(int firstCoordinate, int secondCoordinate) {
        return this.histogram[firstCoordinate][secondCoordinate];
    }

    @Override
    public void setNumberOfThreads(int threads) {
        this.numberOfThreads = threads;
    }

    @Override
    public void createThreads() {
        threads = new Thread[numberOfThreads];

        for (int i = 0; i < this.numberOfThreads; i++) {
            threads[i] = new Thread(() -> calculateHistogram());
        }
    }

    @Override
    public Thread getThread(int thread) {
        return this.threads[thread];
    }

    @Override
    public void start() {
        for (int i = 0; i < this.numberOfThreads; i++) {
            threads[i].start();
        }
    }

    @Override
    public void suspend() {
        synchronized (this) {
            this.suspended = true;
        }
        boolean allMatch = false;
        while (!allMatch) {
            allMatch = Stream.of(this.threads).allMatch(t -> t.getState().equals(Thread.State.WAITING));
        }
    }

    @Override
    public void resume() {
        this.suspended = false;

        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public void terminate() {
        this.terminated = true;
        synchronized (this) {
            this.notifyAll();
        }
        Stream.of(this.threads).forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public Thread[] getThreads() {
        return threads;
    }
}
