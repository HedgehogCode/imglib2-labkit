package net.imglib2.labkit.labeling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.imglib2.Interval;
import net.imglib2.labkit.labeling.Labelings.Diff;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.type.numeric.integer.IntType;

public class SparseLabelingDifference extends ImgLabeling<Diff<Label>, IntType>
		implements LabelingDifference<LabelingType<Diff<Label>>> {

	// TODO Can we make this a list and keep the order?
	private final Set<Label> sourceLabels;

	private final Set<Label> addedLabels;

	private final Set<Label> removedLabels;

	private final Map<String, String> renamedLabels;

	public SparseLabelingDifference(final Interval interval, final Set<Label> sourceLabels) {
		super(new SparseRandomAccessIntType(interval));
		addedLabels = new HashSet<>();
		removedLabels = new HashSet<>();
		renamedLabels = new HashMap<>();
		this.sourceLabels = new HashSet<>(sourceLabels);
	}

	public SparseLabelingDifference(final Interval interval) {
		super(new SparseRandomAccessIntType(interval));
		addedLabels = new HashSet<>();
		removedLabels = new HashSet<>();
		renamedLabels = new HashMap<>();
		sourceLabels = new HashSet<>();
	}

	@Override
	public Set<Label> getSourceLabels() {
		return sourceLabels;
	}

	@Override
	public Set<Label> getAddedLabels() {
		return addedLabels;
	}

	@Override
	public Set<Label> getRemovedLabels() {
		return removedLabels;
	}

	@Override
	public Map<String, String> getRenamedLabels() {
		return renamedLabels;
	}
}
