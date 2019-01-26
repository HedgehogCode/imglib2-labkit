package net.imglib2.labkit.labeling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.labeling.Labelings.Diff;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

public class DiffLabelingTest {

	@Test
	public void testNewLabelingDiffAddLabel() {
		final DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> diffLabeling = DiffLabeling
				.fromEmpty(new FinalInterval(10, 10));
		diffLabeling.addLabel("foo");
		final Set<Label> addedLabels = diffLabeling.getDiff().getAddedLabels();
		assertEquals(1, addedLabels.size());
		final Label label = addedLabels.iterator().next();
		assertEquals("foo", label.name());
	}

	@Test
	public void testNewLabelingDiffAddAndRemoveLabel() {
		final DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> diffLabeling = DiffLabeling
				.fromEmpty(new FinalInterval(10, 10));
		diffLabeling.addLabel("foo");
		diffLabeling.addLabel("bar");
		assertEquals(2, diffLabeling.getDiff().getAddedLabels().size());
		final Label label = diffLabeling.getLabel("foo");
		diffLabeling.removeLabel(label);
		assertEquals(1, diffLabeling.getDiff().getAddedLabels().size());
		assertEquals(0, diffLabeling.getDiff().getRemovedLabels().size());
		final Label barLabel = diffLabeling.getDiff().getAddedLabels().iterator().next();
		assertEquals("bar", barLabel.name());
	}

	@Test
	public void testNewLabelingDraw() {
		final DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> diffLabeling = DiffLabeling
				.fromEmpty(new FinalInterval(10, 10));
		final String name = "foo";
		final int[] pixel1 = { 1, 1 };
		final int[] pixel2 = { 1, 2 };
		final int[] pixel3 = { 2, 2 };

		diffLabeling.addLabel(name);
		final Label label = diffLabeling.getLabel("foo");

		final List<Pair<Label, int[]>> fill = new ArrayList<>();
		fill.add(new ValuePair<>(label, pixel1));
		fill.add(new ValuePair<>(label, pixel2));
		drawOn(diffLabeling, fill);

		final LabelingDifference<LabelingType<Diff<Label>>> diff = diffLabeling.getDiff();
		final DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> recreated = DiffLabeling.fromDiff(diff);
		final RandomAccess<Set<Label>> ra = recreated.randomAccess();
		ra.setPosition(pixel1);
		assertEquals(1, ra.get().size());
		assertTrue(ra.get().contains(label));
		ra.setPosition(pixel2);
		assertEquals(1, ra.get().size());
		assertTrue(ra.get().contains(label));
		ra.setPosition(pixel3);
		assertEquals(0, ra.get().size());
	}

	@Test
	public void testNewLabelingDrawAndRemove() {
		final DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> diffLabeling = DiffLabeling
				.fromEmpty(new FinalInterval(10, 10));
		final String name = "foo";
		final int[] pixel1 = { 1, 1 };
		final int[] pixel2 = { 1, 2 };
		final int[] pixel3 = { 2, 2 };

		diffLabeling.addLabel(name);
		final Label label = diffLabeling.getLabel("foo");

		final List<Pair<Label, int[]>> fill = new ArrayList<>();
		fill.add(new ValuePair<>(label, pixel1));
		fill.add(new ValuePair<>(label, pixel2));
		drawOn(diffLabeling, fill);

		fill.clear();
		fill.add(new ValuePair<>(label, pixel2));
		removeFrom(diffLabeling, fill);

		final LabelingDifference<LabelingType<Diff<Label>>> diff = diffLabeling.getDiff();
		final DiffLabeling<LabelingType<Label>, LabelingType<Diff<Label>>> recreated = DiffLabeling.fromDiff(diff);
		final RandomAccess<Set<Label>> ra = recreated.randomAccess();
		ra.setPosition(pixel1);
		assertEquals(1, ra.get().size());
		assertTrue(ra.get().contains(label));
		ra.setPosition(pixel2);
		assertEquals(0, ra.get().size());
		assertTrue(!ra.get().contains(label));
		ra.setPosition(pixel3);
		assertEquals(0, ra.get().size());
	}

	@Test
	public void testEditLabelingCreate() {
		final String label1 = "foo";
		final String label2 = "bar";
		final int[] pixel1 = { 1, 1 };
		final int[] pixel2 = { 1, 2 };

		final List<Pair<String, int[]>> fill = new ArrayList<>();
		fill.add(new ValuePair<>(label1, pixel1));
		fill.add(new ValuePair<>(label1, pixel2));
		fill.add(new ValuePair<>(label2, pixel2));
		final ImgLabeling<String, IntType> labeling = setupSourceLabeling(fill);

		final DiffLabeling<Set<Label>, LabelingType<Diff<Label>>> diff = DiffLabeling
				.fromSource(Sets.newHashSet(label1, label2), labeling);
		assertEquals(2, diff.getLabels().size());
		final Label fooLabel = diff.getLabel(label1);
		final Label barLabel = diff.getLabel(label2);
		assertEquals(label1, fooLabel.name());
		assertTrue(diff.getLabels().contains(fooLabel));
		assertEquals(label2, barLabel.name());
		assertTrue(diff.getLabels().contains(barLabel));

		final RandomAccess<Set<Label>> ra = diff.randomAccess();
		ra.setPosition(pixel1);
		assertEquals(1, ra.get().size());
		assertTrue(ra.get().contains(fooLabel));

		ra.setPosition(pixel2);
		assertEquals(2, ra.get().size());
		assertTrue(ra.get().contains(fooLabel));
		assertTrue(ra.get().contains(barLabel));
	}

	@Test
	public void testEditLabelingAddLabel() {
		final String label1 = "foo";
		final String label2 = "bar";
		final int[] pixel1 = { 1, 1 };
		final int[] pixel2 = { 1, 2 };

		// Create a labeling with "foo" at pixel1 and pixel2
		final List<Pair<String, int[]>> fill = new ArrayList<>();
		fill.add(new ValuePair<>(label1, pixel1));
		fill.add(new ValuePair<>(label1, pixel2));
		final ImgLabeling<String, IntType> labeling = setupSourceLabeling(fill);

		// Create diff with the prefilled labeling
		final DiffLabeling<Set<Label>, LabelingType<Diff<Label>>> diff = DiffLabeling
				.fromSource(Sets.newHashSet(label1), labeling);

		// Check that "foo" exists as label
		Label fooLabel = diff.getLabels().get(0);
		assertEquals("foo", fooLabel.name());

		// Add a "bar" label and check that it was added correctly
		Label barLabel = diff.addLabel(label2);
		assertEquals(label2, barLabel.name());
		assertEquals(2, diff.getLabels().size());
		assertTrue(diff.getLabels().contains(barLabel));

		// Add bar to first pixel and remove foo from second
		final List<Pair<Label, int[]>> fill2 = new ArrayList<>();
		fill2.add(new ValuePair<>(barLabel, pixel1));
		drawOn(diff, fill2);
		fill2.clear();
		fill2.add(new ValuePair<Label, int[]>(fooLabel, pixel2));
		removeFrom(diff, fill2);

		// Check that the diff contains the expected operations
		final LabelingDifference<LabelingType<Diff<Label>>> labDiff = diff.getDiff();
		final RandomAccess<LabelingType<Diff<Label>>> raDiff = labDiff.randomAccess();
		raDiff.setPosition(pixel1);
		assertEquals(1, raDiff.get().size());
		assertTrue(raDiff.get().contains(Diff.add(barLabel)));
		raDiff.setPosition(pixel2);
		assertEquals(1, raDiff.get().size());
		assertTrue(raDiff.get().contains(Diff.remove(fooLabel)));

		// Create a new DiffLabeling with the diff
		final DiffLabeling<Set<Label>, LabelingType<Diff<Label>>> recreated = DiffLabeling
				.fromSourceAndDiff(Sets.newHashSet(label1), labeling, labDiff);

		// Check the labels
		assertEquals(2, recreated.getLabels().size());
		fooLabel = recreated.getLabel(label1);
		assertEquals(label1, fooLabel.name());
		barLabel = recreated.getLabel(label2);
		assertEquals(label2, barLabel.name());

		final RandomAccess<Set<Label>> raRecreated = recreated.randomAccess();
		raRecreated.setPosition(pixel1);
		assertEquals(2, raRecreated.get().size());
		assertTrue(raRecreated.get().contains(fooLabel));
		assertTrue(raRecreated.get().contains(barLabel));
		raRecreated.setPosition(pixel2);
		assertEquals(0, raRecreated.get().size());
	}

	private static ImgLabeling<String, IntType> setupSourceLabeling(
			final Collection<Pair<String, int[]>> filledPixels) {
		final ImgLabeling<String, IntType> labeling = new ImgLabeling<>(ArrayImgs.ints(10, 10));
		drawOn(labeling, filledPixels);
		return labeling;
	}

	private static <T> void drawOn(RandomAccessibleInterval<? extends Set<T>> lab, Collection<Pair<T, int[]>> fill) {
		final RandomAccess<? extends Set<T>> ra = lab.randomAccess();
		for (final Pair<T, int[]> e : fill) {
			ra.setPosition(e.getB());
			ra.get().add(e.getA());
		}
	}

	private static <T> void removeFrom(RandomAccessibleInterval<? extends Set<T>> lab,
			Collection<Pair<T, int[]>> fill) {
		final RandomAccess<? extends Set<T>> ra = lab.randomAccess();
		for (final Pair<T, int[]> e : fill) {
			ra.setPosition(e.getB());
			ra.get().remove(e.getA());
		}
	}
}
