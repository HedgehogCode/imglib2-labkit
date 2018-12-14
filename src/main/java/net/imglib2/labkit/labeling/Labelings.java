
package net.imglib2.labkit.labeling;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.sparse.DifferenceRandomAccessibleIntType;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.labkit.labeling.DiffImgLabeling.Diff;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Labelings {

	private static Object labelSets;

	public static List<Labeling> slices(Labeling labeling) {
		int sliceDimension = labeling.numDimensions() - 1;
		Interval sliceInterval = DimensionUtils.removeLastDimension(labeling);
		List<Label> labels = labeling.getLabels();
		List<Labeling> slices = IntStream.range(0, Math.toIntExact(labeling
			.dimension(sliceDimension))).mapToObj(ignore -> Labeling
				.createEmptyLabels(labels, sliceInterval)).collect(Collectors.toList());
		sparseCopy(labeling, Views.stack(slices));
		return slices;
	}

	private static void sparseCopy(Labeling labeling,
		RandomAccessibleInterval<Set<Label>> target)
	{
		Cursor<?> cursor = labeling.sparsityCursor();
		RandomAccess<Set<Label>> out = target.randomAccess();
		RandomAccess<Set<Label>> in = labeling.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			in.setPosition(cursor);
			out.setPosition(cursor);
			copy(in.get(), out.get());
		}
	}

	private static <T> void copy(Set<T> in, Set<T> out) {
		out.clear();
		out.addAll(in);
	}

	public static Labeling singleton(Interval interval, String labelName,
		long... coordinates)
	{
		Labeling labeling = Labeling.createEmpty(Collections.singletonList(
			labelName), interval);
		Label label = labeling.getLabels().iterator().next();
		RandomAccess<Set<Label>> ra = labeling.randomAccess();
		ra.setPosition(coordinates);
		ra.get().add(label);
		return labeling;
	}

	static <S, T> ImgLabeling<T, ?> mapLabels(ImgLabeling<S, ?> input,
		Function<S, T> mapping)
	{
		LabelingMapping<S> oldLabels = input.getMapping();
		Map<S, T> map = oldLabels.getLabels().stream().collect(Collectors.toMap(
			s -> s, s -> mapping.apply(s)));
		List<Set<T>> newLabels = new ArrayList<>();
		for (int i = 0; i < oldLabels.numSets(); i++) {
			final Set<S> old = oldLabels.labelsAtIndex(i);
			final Set<T> newer = old.stream().map(map::get).collect(Collectors
				.toSet());
			newLabels.add(newer);
		}
		return LabelingSerializer.fromImageAndLabelSets(input.getIndexImg(),
			newLabels);
	}

	public static List<Set<String>> getLabelSets(
		LabelingMapping<String> mapping)
	{
		return IntStream.range(0, mapping.numSets()).mapToObj(
			mapping::labelsAtIndex).collect(Collectors.toList());
	}

	/**
	 * Computes the difference between a and b. The resulting labeling will contain
	 * for each pixel a set of differences that are between the labeling a and
	 * labeling b. The difference describes how to get from the labeling a to the
	 * labeling b.
	 *
	 * @param a
	 *            the first labeling
	 * @param b
	 *            the second labeling
	 * @return
	 */
	public static <T> ImgLabeling<Diff<T>, IntType> diff(final RandomAccessibleInterval<LabelingType<T>> a,
			final RandomAccessibleInterval<LabelingType<T>> b) {
		// Get the cursor over the difference
		Cursor<?> cursor = null;
		if (b instanceof ImgLabeling) {
			final RandomAccessibleInterval<?> indexImg = ((ImgLabeling<T, ?>) b).getIndexImg();
			if (indexImg instanceof DifferenceRandomAccessibleIntType) {
				cursor = ((DifferenceRandomAccessibleIntType) indexImg).differencePattern().localizingCursor();
			}
		}
		if (cursor == null) {
			cursor = Views.iterable(b).localizingCursor();
		}

		// Create the result
		final SparseRandomAccessIntType img = new SparseRandomAccessIntType(a);
		final ImgLabeling<Diff<T>, IntType> result = new ImgLabeling<>(img);

		// Loop over the difference and build the difference labeling
		final RandomAccess<LabelingType<T>> aRA = a.randomAccess();
		final RandomAccess<LabelingType<T>> bRA = b.randomAccess();
		final RandomAccess<LabelingType<Diff<T>>> rRA = result.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			aRA.setPosition(cursor);
			bRA.setPosition(cursor);
			rRA.setPosition(cursor);
			final LabelingType<T> aLabelSet = aRA.get();
			final LabelingType<T> bLabelSet = bRA.get();
			final LabelingType<Diff<T>> diff = rRA.get();
			writeDifferenceSet(aLabelSet, bLabelSet, diff);
		}
		return result;
	}

	private static <T> void writeDifferenceSet(Set<T> aLabelSet,
		Set<T> bLabelSet, LabelingType<Diff<T>> diff)
	{
		for (T label : aLabelSet)
			if (!bLabelSet.contains(label))
				diff.add(Diff.remove(label));
		for (T label : bLabelSet)
			if (!aLabelSet.contains(label))
				diff.add(Diff.add(label));
	}

	public static <T> void applyDiff(final RandomAccessibleInterval<LabelingType<T>> lab,
			final RandomAccessibleInterval<LabelingType<Diff<T>>> diff) {
		// Get a cursor over the difference
		Cursor<?> cursor = null;
		if (diff instanceof ImgLabeling) {
			final RandomAccessibleInterval<?> indexImg = ((ImgLabeling<Diff<T>, ?>) diff).getIndexImg();
			if (indexImg instanceof SparseRandomAccessIntType) {
				cursor = ((SparseRandomAccessIntType) indexImg).sparseCursor();
			}
		}
		if (cursor == null) {
			cursor = Views.iterable(diff).localizingCursor();
		}

		// Loop over the difference and
		final RandomAccess<LabelingType<T>> lRA = lab.randomAccess();
		final RandomAccess<LabelingType<Diff<T>>> dRA = diff.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			lRA.setPosition(cursor);
			dRA.setPosition(cursor);
			final LabelingType<T> labelSet = lRA.get();
			final LabelingType<Diff<T>> diffSet = dRA.get();
			applyDifferenceSet(labelSet, diffSet);
		}
	}

	public static <T> void applyDifferenceSet(final LabelingType<T> labelSet, final LabelingType<Diff<T>> diffSet) {
		for (final Diff<T> d : diffSet) {
			switch (d.action) {
			case ADD:
				labelSet.add(d.label);
				break;
			case REMOVE:
				labelSet.remove(d.label);
				break;
			}
		}
	}
}
