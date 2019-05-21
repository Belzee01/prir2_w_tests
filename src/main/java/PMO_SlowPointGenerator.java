import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PMO_SlowPointGenerator implements PointGeneratorInterface {

	private final PMO_AtomicCounter parallelUsage;
	private final PMO_AtomicCounter maxUsage;
	private final AtomicBoolean suspendedFlag;
	private final long delay;
	private final List<Point2D> pointsRepository;
	private final AtomicInteger index = new AtomicInteger();
	private final int threadsAllowed;
	private final AtomicBoolean afterSuspensionUsage = new AtomicBoolean(false);
	private final AtomicBoolean numberOfThreadsExceeded = new AtomicBoolean(false);
	private final int points;
	
	{
		maxUsage = PMO_CountersFactory.createCommonMaxStorageCounter();
		parallelUsage = PMO_CountersFactory.createCounterWithMaxStorageSet();
	}

	public PMO_SlowPointGenerator(AtomicBoolean suspendedFlag, long delay, int threadsAllowed,
			List<Point2D> pointsRepository) {
		this.suspendedFlag = suspendedFlag;
		this.delay = delay;
		this.pointsRepository = pointsRepository;
		this.threadsAllowed = threadsAllowed;
		points = pointsRepository.size();
	}

	@Override
	public Point2D getPoint() {
		if (suspendedFlag.get()) {
			afterSuspensionUsage.set(true);
		}
		parallelUsage.incAndStoreMax();

		int threads = maxUsage.get();
		if (threads > threadsAllowed) {
			numberOfThreadsExceeded.set(true);
		}

		PMO_TimeHelper.sleep(delay);

		parallelUsage.dec();

		return pointsRepository.get(index.getAndIncrement() % points );
	}

	public int getMaxThreads() {
		return maxUsage.get();
	}

	public void resetMaxThreadCounter() {
		maxUsage.clear();
	}

	public int getIndex() {
		return index.get();
	}

	public void test() {
		assertFalse(afterSuspensionUsage.get(),
				"Oczekiwano, że po wykonaniu suspend punkty nie będą pobierane z generatora");
		assertFalse(numberOfThreadsExceeded.get(), "Przekroczono dozwoloną liczbę wątków");
		assertTrue(index.get() <= points , "Pobrano za dużo punktów z generatora! Jak??? Pobrano " + index.get() + " przygotowano " + points );
	}
}
