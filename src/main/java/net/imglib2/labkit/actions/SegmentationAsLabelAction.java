package net.imglib2.labkit.actions;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.models.SegmentationResultsModels;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;
import java.util.Set;

/**
 * @author Matthias Arzt
 */
public class SegmentationAsLabelAction extends AbstractFileIoAcion {

	private final Holder<Labeling> labelingHolder;
	private final SegmentationResultsModels model;

	public SegmentationAsLabelAction(Extensible extensible, SegmentationResultsModels model, Holder<Labeling> labelingHolder) {
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		this.labelingHolder = labelingHolder;
		this.model = model;
		extensible.addAction("Create Label from Segmentation...", "addSegmentationAsLabel",
				this::addSegmentationAsLabels, "");
	}

	private void addSegmentationAsLabels() {
		SegmentationResultsModel selectedResults = model.selectedResults();
		List<String> labels = selectedResults.labels();
		String selected = (String) JOptionPane.showInputDialog(null, "Select label to be added",
				"Add Segmentation as Labels ...", JOptionPane.PLAIN_MESSAGE,
				null, labels.toArray(), labels.get(labels.size() - 1));
		int index = labels.indexOf(selected);
		if(index < 0)
			return;
		addLabel(selected, index, selectedResults.segmentation() );
	}

	private void addLabel(String selected, int index, RandomAccessibleInterval<ShortType> segmentation) {
		Converter<ShortType, BitType> converter = (in, out) -> out.set(in.get() == index);
		RandomAccessibleInterval<BitType> result = Converters.convert(segmentation, converter, new BitType());
		addLabel(labelingHolder.get(), "segmented " + selected, result);
		labelingHolder.notifier().forEach(l -> l.accept(labelingHolder.get()));
	}

	// TODO move to better place
	private static void addLabel(Labeling labeling, String name, RandomAccessibleInterval<BitType> mask) {
		Cursor<BitType> cursor = Views.iterable(mask).cursor();
		labeling.addLabel(name);
		RandomAccess<Set<String>> ra = labeling.randomAccess();
		while(cursor.hasNext()) {
			boolean value = cursor.next().get();
			if(value) {
				ra.setPosition(cursor);
				ra.get().add(name);
			}
		}
	}
}
