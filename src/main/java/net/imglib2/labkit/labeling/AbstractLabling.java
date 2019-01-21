package net.imglib2.labkit.labeling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.ColorSupplier;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.ARGBType;

public abstract class AbstractLabling extends AbstractWrappedInterval<Interval> implements Labeling {

	private final List<Label> labels;
	private final ColorSupplier colorSupplier;
	private List<CalibratedAxis> axes;

	public AbstractLabling(final List<Label> labels, final Interval source, final ColorSupplier colorSupplier) {
		super(source);
		this.labels = new ArrayList<>(labels);
		this.colorSupplier = colorSupplier;
		this.axes = initAxes(numDimensions());
	}

	@Override
	public List<Label> getLabels() {
		return labels;
	}

	@Override
	public Label getLabel(final String name) {
		for (final Label label : labels) {
			if (label.name().equals(name)) {
				return label;
			}
		}
		throw new NoSuchElementException("There is no label with the name '" + name + "'.");
	}

	@Override
	public Label addLabel(final String label) {
		Objects.requireNonNull(label);
		final Label e = new Label(label, colorSupplier.get());
		labels.add(e);
		return e;
	}

	@Override
	public Label addLabel(final String newName, final RandomAccessibleInterval<? extends BooleanType<?>> bitmap) {
		Label label = addLabel(newName);
		LoopBuilder.setImages(bitmap, this).forEachPixel((i, o) -> {
			if (i.get())
				o.add(label);
		});
		return label;
	}

	@Override
	public void removeLabel(final Label label) {
		if (labels.contains(label)) {
			labels.remove(label);
			clearLabel(label);
		}
	}

	@Override
	public void renameLabel(final Label oldLabel, final String newLabel) {
		oldLabel.setName(newLabel);
	}

	@Override
	public void setLabelOrder(final Comparator<? super Label> comparator) {
		labels.sort(comparator);
	}

	@Override
	public void clearLabel(Label label) {
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<Set<Label>> ra = randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			ra.setPosition(cursor);
			Set<Label> set = ra.get();
			set.remove(label);
		}
	}

	@Override
	public Interval interval() {
		return new FinalInterval(this);
	}

	@Override
	public void setAxes(final List<CalibratedAxis> axes) {
		this.axes = axes.stream().map(CalibratedAxis::copy).collect(Collectors.toList());
	}

	@Override
	public List<CalibratedAxis> axes() {
		return axes;
	}

	private List<CalibratedAxis> initAxes(int i) {
		return IntStream.range(0, i).mapToObj(ignore -> new DefaultLinearAxis()).collect(Collectors.toList());
	}

	@Override
	public void setLabelColor(final Label label, final ARGBType color) {
		label.setColor(color);
	}
}
