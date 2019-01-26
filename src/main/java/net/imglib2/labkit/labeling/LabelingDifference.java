package net.imglib2.labkit.labeling;

import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Labelings.Diff;

public interface LabelingDifference<D extends Set<Diff<Label>>> extends RandomAccessibleInterval<D> {

	Set<Label> getSourceLabels();

	Set<Label> getAddedLabels();

	Set<Label> getRemovedLabels();

	Map<String, String> getRenamedLabels();
}
