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
		final Map<String, Label> sourceDiffLabels = diff.getSourceLabels().stream()
				.collect(Collectors.toMap(Label::name, Function.identity()));
		for (final String l : labels) {
			if (!sourceDiffLabels.containsKey(l)) {
				sourceDiffLabels.put(l, new Label(l, colorSupplier.get()));
			}
		}
		final RandomAccessibleInterval<Set<Label>> mappedSource = createMappedSource(sourceDiffLabels, source,
				colorSupplier);
		return new DiffLabeling<>(new ArrayList<>(sourceDiffLabels.values()), colorSupplier, mappedSource, diff);
	}

	public static DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> fromEmpty(final Interval interval) {
		final ImgLabeling<Label, IntType> source = createEmptySource(interval);
		final LabelingDifference<LabelingType<Diff<Label>>> diff = createEmptyDiff(interval, null);
		return new DiffLabeling<>(Collections.emptyList(), new ColorSupplier(), source, diff);
	}

	public static DiffLabeling<Set<Label>, LabelingType<Diff<Label>>> fromSource(final Set<String> labels,
			final RandomAccessibleInterval<? extends Set<String>> source) {
		final ColorSupplier colorSupplier = new ColorSupplier();
		final Map<String, Label> mapping = mapStringLabels(labels, colorSupplier);
		final RandomAccessibleInterval<Set<Label>> mappedSource = createMappedSource(mapping, source, colorSupplier);
		final LabelingDifference<LabelingType<Diff<Label>>> diff = createEmptyDiff(source,
				new HashSet<>(mapping.values()));

		return new DiffLabeling<>(new ArrayList<>(mapping.values()), colorSupplier, mappedSource, diff);
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

	private static LabelingDifference<LabelingType<Diff<Label>>> createEmptyDiff(final Interval interval,
			final Set<Label> sourceLables) {
		if (sourceLables != null) {
			return new SparseLabelingDifference(interval, sourceLables);
		} else {
			return new SparseLabelingDifference(interval);
		}
	}

	private static Map<String, Label> mapStringLabels(final Set<String> labels, final ColorSupplier colorSupplier) {
		return labels.stream().map(n -> new Label(n, colorSupplier.get()))
				.collect(Collectors.toMap(Label::name, Function.identity()));
	}

	private static RandomAccessibleInterval<Set<Label>> createMappedSource(final Map<String, Label> mapping,
			final RandomAccessibleInterval<? extends Set<String>> source, final ColorSupplier colorSupplier) {
		return new MappedRandomAccessibleInterval<>(source, s -> SetUtils.map(s, mapping::get));
	}

	private final RandomAccessibleInterval<T> source;

	private final LabelingDifference<D> diff;

	private final List<Label> sourceLabels;

	public DiffLabeling(final List<Label> sourceLabels, final ColorSupplier colorSupplier,
			final RandomAccessibleInterval<T> source, final LabelingDifference<D> diff) {
		super(sourceLabels, new FinalInterval(source), colorSupplier);
		this.source = source;
		this.diff = diff;
		this.sourceLabels = new ArrayList<>(sourceLabels);

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
			return diff.remove(Diff.remove(label));
		} else {
			// Not in source
			return diff.add(Diff.add(label));
		}
	}

	protected boolean removeLabel(final T source, final D diff, final Label label) {
		if (source.contains(label)) {
			// Source contains label: Add remove to diff
			return diff.add(Diff.remove(label));
		} else {
			// Not in source: Remove add:label if it is in the diff
			return diff.remove(Diff.add(label));
		}
	}

	private class DiffRandomAccess extends Point implements RandomAccess<Set<Label>> {

		private final RandomAccess<T> sourceRA;

		private final RandomAccess<D> diffRA;

		private final Set<Label> val = new AbstractSet<Label>() {

			@Override
			public Iterator<Label> iterator() {
				return getCurrent().iterator();
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

			public boolean remove(final Object o) {
				if (!(o instanceof Label)) {
					return false;
				}
				updateSourcePos();
				return removeLabel(sourceRA.get(), diffRA.get(), (Label) o);
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
