package net.imglib2.labkit.labeling;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.labkit.labeling.Labelings.Diff;
import net.imglib2.labkit.utils.ColorSupplier;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

public final class DiffLabeling<T extends Set<Label>, D extends Set<Diff<Label>>> extends AbstractLabling {

	public static <D extends Set<Diff<Label>>> DiffLabeling<Set<Label>, D> fromSourceAndDiff(final Set<String> labels,
			final RandomAccessibleInterval<? extends Set<String>> source, final LabelingDifference<D> diff) {
		final ColorSupplier colorSupplier = new ColorSupplier();
		final List<Label> mappedLabels = mapStringLabels(labels, colorSupplier);
		final RandomAccessibleInterval<Set<Label>> mappedSource = createMappedSource(mappedLabels, source,
				colorSupplier);

		return new DiffLabeling<>(mappedLabels, colorSupplier, mappedSource, diff);
	}

	public static DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> fromEmpty(final Interval interval) {
		final ImgLabeling<Label, IntType> source = createEmptySource(interval);
		final LabelingDifference<LabelingType<Diff<Label>>> diff = createEmptyDiff(interval);
		return new DiffLabeling<>(Collections.emptyList(), new ColorSupplier(), source, diff);
	}

	public static DiffLabeling<Set<Label>, LabelingType<Diff<Label>>> fromSource(final Set<String> labels,
			final RandomAccessibleInterval<? extends Set<String>> source) {
		final ColorSupplier colorSupplier = new ColorSupplier();
		final List<Label> mappedLabels = mapStringLabels(labels, colorSupplier);
		final RandomAccessibleInterval<Set<Label>> mappedSource = createMappedSource(mappedLabels, source,
				colorSupplier);
		final LabelingDifference<LabelingType<Diff<Label>>> diff = createEmptyDiff(source);

		return new DiffLabeling<>(mappedLabels, colorSupplier, mappedSource, diff);
	}

	public static <D extends Set<Diff<Label>>> DiffLabeling<LabelingType<Label>, D> fromDiff(
			final LabelingDifference<D> diff) {
		final ImgLabeling<Label, IntType> source = createEmptySource(diff);
		return new DiffLabeling<>(Collections.emptyList(), new ColorSupplier(), source, diff);
	}

	private static ImgLabeling<Label, IntType> createEmptySource(final Interval interval) {
		final int n = interval.numDimensions();
		final RandomAccessibleInterval<IntType> indexImg = Views.interval(new AllZeroRandomAccessible(n), interval);
		return new ImgLabeling<>(indexImg);
	}

	private static LabelingDifference<LabelingType<Diff<Label>>> createEmptyDiff(final Interval interval) {
		return new SparseLabelingDifference(interval);
	}

	private static List<Label> mapStringLabels(final Set<String> labels, final ColorSupplier colorSupplier) {
		return labels.stream().map(n -> new Label(n, colorSupplier.get())).collect(Collectors.toList());
	}

	private static RandomAccessibleInterval<Set<Label>> createMappedSource(final List<Label> labels,
			final RandomAccessibleInterval<? extends Set<String>> source, final ColorSupplier colorSupplier) {
		final Map<String, Label> mapping = labels.stream().collect(Collectors.toMap(Label::name, Function.identity()));
		return new MappedRandomAccessibleInterval<>(source, s -> SetUtils.map(s, mapping::get));
	}

	private final RandomAccessibleInterval<T> source;

	private final LabelingDifference<D> diff;

	private final List<Label> sourceLabels;

	public DiffLabeling(final List<Label> labels, final ColorSupplier colorSupplier,
			final RandomAccessibleInterval<T> source, final LabelingDifference<D> diff) {
		super(labels, new FinalInterval(source), colorSupplier);
		this.source = source;
		this.diff = diff;

		sourceLabels = new ArrayList<>(labels);

		// Adapt the labels list according to the diff
		for (final Label l : diff.getAddedLabels()) {
			Objects.requireNonNull(l);
			getLabels().add(l);
		}
		for (final Label l : diff.getRemovedLabels()) {
			Objects.requireNonNull(l);
			getLabels().remove(l);
		}
	}

	public LabelingDifference<D> getDiff() {
		return diff;
	}

	@Override
	public RandomAccessibleInterval<BitType> getRegion(final Label label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Label, IterableRegion<BitType>> iterableRegions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor<?> sparsityCursor() {
		// TODO overwrite this when we know about the sparsity
		return Views.iterable(this).cursor();
	}

	@Override
	public RandomAccessibleInterval<? extends IntegerType<?>> getIndexImg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Set<Label>> getLabelSets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RandomAccess<Set<Label>> randomAccess() {
		return new DiffRandomAccess();
	}

	@Override
	public RandomAccess<Set<Label>> randomAccess(final Interval interval) {
		return new DiffRandomAccess();
	}

	@Override
	public Label addLabel(final String label) {
		final Label l = super.addLabel(label);
		diff.getAddedLabels().add(l);
		return l;
	}

	@Override
	public void removeLabel(final Label label) {
		super.removeLabel(label);
		if (diff.getAddedLabels().contains(label)) {
			diff.getAddedLabels().remove(label);
		} else {
			diff.getRemovedLabels().add(label);
		}
	}

	@Override
	public void renameLabel(final Label label, final String name) {
		if (diff.getAddedLabels().contains(label)) {
			// New label just rename it
			label.setName(name);
		} else {
			// Remember that it was renamed
			diff.getRenamedLabels().put(label.name(), name);
			label.setName(name);
		}
	}

	protected Set<Label> getAppliedSet(final T source, final D diff) {
		// TODO cache mapping
		// TODO Optimize somehow
		final HashSet<Label> result = new HashSet<>(source);
		for (final Diff<Label> d : diff) {
			switch (d.action) {
			case ADD:
				result.add(d.label);
				break;
			case REMOVE:
				result.remove(d.label);
				break;
			}
		}
		return Collections.unmodifiableSet(result);
	}

	protected boolean addLabel(final T source, final D diff, final Label label) {
		if (source.contains(label)) {
			// Already in source
			final Diff<Label> removeDiff = Diff.remove(label);
			if (diff.contains(removeDiff)) {
				// Was removed: We don't remove it anymore
				diff.remove(removeDiff);
				return true;
			} else {
				// Was not removed: We don't add it
				return false;
			}
		} else {
			// Not in source
			final Diff<Label> addDiff = Diff.add(label);
			if (diff.contains(addDiff)) {
				// Was added: We don't add it
				return false;
			} else {
				// Was not added: We add it
				diff.add(addDiff);
				return true;
			}
		}
	}

	protected void removeLabel(final T source, final D diff, final Label label) {
		if (source.contains(label)) {
			diff.add(Diff.remove(label));
			return;
		}
		final Diff<Label> addDiff = Diff.add(label);
		if (diff.contains(addDiff)) {
			diff.remove(addDiff);
		}
	}

	private class DiffRandomAccess extends Point implements RandomAccess<Set<Label>> {

		private final RandomAccess<T> sourceRA;

		private final RandomAccess<D> diffRA;

		private final Set<Label> val = new AbstractSet<Label>() {

			@Override
			public Iterator<Label> iterator() {
				// TODO remove is called on the iterator :(
				final Iterator<Label> iterator = getCurrent().iterator();
				return new Iterator<Label>() {

					private Label last;

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Label next() {
						last = iterator.next();
						return last;
					}

					@Override
					public void remove() {
						updateSourcePos();
						removeLabel(sourceRA.get(), diffRA.get(), last);
					};
				};
			}

			@Override
			public int size() {
				return getCurrent().size();
			}

			public boolean add(final Label e) {
				updateSourcePos();
				return addLabel(sourceRA.get(), diffRA.get(), e);
			}

			private Set<Label> getCurrent() {
				updateSourcePos();
				return getAppliedSet(sourceRA.get(), diffRA.get());
			}
		};

		private DiffRandomAccess() {
			super(DiffLabeling.this.numDimensions());
			sourceRA = source.randomAccess();
			diffRA = diff.randomAccess();
		}

		private void updateSourcePos() {
			sourceRA.setPosition(this);
			diffRA.setPosition(this);
		}

		@Override
		public Set<Label> get() {
			return val;
		}

		@Override
		public Sampler<Set<Label>> copy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RandomAccess<Set<Label>> copyRandomAccess() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
