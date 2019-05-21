import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class PMO_FastPointGenerator implements PointGeneratorInterface {

	private final List<Point2D> pointsRepository;
	private final AtomicInteger index = new AtomicInteger();
	private final int points;

	public PMO_FastPointGenerator(List<Point2D> pointsRepository) {
		this.pointsRepository = pointsRepository;
		this.points = pointsRepository.size();
	}

	@Override
	public Point2D getPoint() {
		return pointsRepository.get(index.getAndIncrement() % points);
	}

	public int getIndex() {
		return index.get();
	}

	public int[][] getHistogram() {
		int[][] result = new int[PointGeneratorInterface.MAX_POSITION + 1][PointGeneratorInterface.MAX_POSITION + 1];

		IntStream.range(0, index.get()).forEach(i -> {
			Point2D point = pointsRepository.get(i % points);
			result[point.firstCoordinate][point.secondCoordinate]++;
		});

		return result;
	}

	public long getSum() {
		return IntStream.range(0, index.get()).mapToObj(i -> pointsRepository.get(i % points))
				.mapToLong(p -> p.firstCoordinate + p.secondCoordinate).sum();
	}

}
