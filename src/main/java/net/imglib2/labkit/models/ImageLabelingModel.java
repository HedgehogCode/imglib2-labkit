
package net.imglib2.labkit.models;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.List;
import java.util.stream.IntStream;

public class ImageLabelingModel implements LabelingModel {

	private final AffineTransform3D labelTransformation = new AffineTransform3D();

	private Holder<Labeling> labelingHolder;

	private Notifier<Runnable> dataChangedNotifier = new Notifier<>();

	private Holder<Label> selectedLabelHolder;

	private Holder<Boolean> imageVisibility = new DefaultHolder<>(true);

	private Holder<Boolean> labelingVisibility = new DefaultHolder<>(true);

	private final boolean isTimeSeries;

	private final TransformationModel transformationModel =
		new TransformationModel();

	private BdvShowable showable;

	private final Holder<String> labelingFileName = new DefaultHolder<>("");

	public ImageLabelingModel(
		RandomAccessibleInterval<? extends NumericType<?>> image, Labeling labeling,
		boolean isTimeSeries)
	{
		this(BdvShowable.wrap(image), labeling, isTimeSeries);
	}

	public ImageLabelingModel(BdvShowable showable, Labeling labeling,
		boolean isTimeSeries)
	{
		this.showable = showable;
		this.labelingHolder = new DefaultHolder<>(labeling);
		this.labelingHolder.notifier().add(this::labelingReplacedEvent);
		updateLabelTransform();
		this.selectedLabelHolder = new DefaultHolder<>(labeling.getLabels().stream()
			.findAny().orElse(null));
		this.isTimeSeries = isTimeSeries;
	}

	private void updateLabelTransform() {
		labelTransformation.set(multiply(showable.transformation(), getScaling(
			showable.interval(), labelingHolder.get().interval())));
	}

	private AffineTransform3D multiply(AffineTransform3D transformation,
		AffineTransform3D scaling)
	{
		AffineTransform3D result = new AffineTransform3D();
		result.set(transformation);
		result.concatenate(scaling);
		return result;
	}

	private void labelingReplacedEvent(Labeling labeling) {
		updateLabelTransform();
		Label selectedLabel = selectedLabelHolder.get();
		List<Label> labels = labelingHolder.get().getLabels();
		if (!labels.contains(selectedLabel)) selectedLabelHolder.set(labels
			.isEmpty() ? null : labels.get(0));
	}

	public BdvShowable showable() {
		return showable;
	}

	// -- LabelingModel methods --

	@Override
	public AffineTransform3D labelTransformation() {
		return labelTransformation;
	}

	@Override
	public Holder<String> labelingFileName() {
		return labelingFileName;
	}

	@Override
	public Holder<Labeling> labeling() {
		return labelingHolder;
	}

	@Override
	public Holder<Label> selectedLabel() {
		return selectedLabelHolder;
	}

	@Override
	public Notifier<Runnable> dataChangedNotifier() {
		return dataChangedNotifier;
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	public Holder<Boolean> imageVisibility() {
		return imageVisibility;
	}

	@Override
	public Holder<Boolean> labelingVisibility() {
		return labelingVisibility;
	}

	public TransformationModel transformationModel() {
		return transformationModel;
	}

	public Dimensions spatialDimensions() {
		Interval interval = labelingHolder.get().interval();
		int n = interval.numDimensions() - (isTimeSeries() ? 1 : 0);
		return new FinalDimensions(IntStream.range(0, n).mapToLong(
			interval::dimension).toArray());
	}

	private AffineTransform3D getScaling(Interval inputImage,
		Interval initialLabeling)
	{
		long[] dimensionsA = get3dDimensions(inputImage);
		long[] dimensionsB = get3dDimensions(initialLabeling);
		double[] values = IntStream.range(0, 3).mapToDouble(
			i -> (double) dimensionsA[i] / (double) dimensionsB[i]).toArray();
		AffineTransform3D affineTransform3D = new AffineTransform3D();
		affineTransform3D.set(values[0], 0.0, 0.0, 0.0, 0.0, values[1], 0.0, 0.0,
			0.0, 0.0, values[2], 0.0);
		return affineTransform3D;
	}

	private long[] get3dDimensions(Interval interval) {
		long[] result = new long[3];
		int n = interval.numDimensions();
		for (int i = 0; i < n & i < 3; i++)
			result[i] = interval.dimension(i);
		for (int i = n; i < 3; i++)
			result[i] = 1;
		return result;
	}
}
