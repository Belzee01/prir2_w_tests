import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PMO_PointsRepository {

	public static int estimateSize(long time, long delay, int threads) {
		return (int) ((threads * time) / delay);
	}

	public static List<PointGeneratorInterface.Point2D> getRepository(int size) {
		List<PointGeneratorInterface.Point2D> points = new ArrayList<>(size);
		Random rnd = ThreadLocalRandom.current();
		int x, y;
		do {
			x = rnd.nextInt(PointGeneratorInterface.MAX_POSITION);
			y = rnd.nextInt(PointGeneratorInterface.MAX_POSITION);
			points.add(new PointGeneratorInterface.Point2D(x, y));
		} while (points.size() < size);

		return points;
	}
}
