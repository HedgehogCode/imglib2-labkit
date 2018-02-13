package net.imglib2.labkit.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;

public class ColoredLabelsModel
{

	private final ImageLabelingModel model;

	private final Notifier< Runnable > listeners = new Notifier<>();

	public ColoredLabelsModel(ImageLabelingModel model) {
		this.model = model;
		model.labeling().notifier().add( l -> notifyListeners() );
		model.selectedLabel().notifier().add( s -> notifyListeners() );
	}

	private void notifyListeners()
	{
		listeners.forEach( Runnable::run );
	}

	// TODO: use a List instead of a Map to keep the Order
	public Map<String, ARGBType > items() {
		Map<String, ARGBType> result = new HashMap<>();
		ColorMap colors = model.colorMapProvider().colorMap();
		List<String> labels = model.labeling().get().getLabels();
		labels.forEach( label -> result.put( label, colors.getColor( label ) ) );
		return result;
	}

	public String selected() {
		return model.selectedLabel().get();
	}

	public void setSelected(String value) {
		model.selectedLabel().set( value );
	}

	public Notifier<Runnable> listeners() {
		return listeners;
	}

	public void addLabel() {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		String label = suggestName(labeling.getLabels());
		if(label == null)
			return;
		labeling.addLabel(label);
		holder.notifier().forEach(l -> l.accept(labeling));
	}

	public void removeLabel(String label) {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		holder.get().removeLabel( label );
		holder.notifier().forEach( l -> l.accept( labeling ) );
	}

	public void renameLabel(String label, String newLabel) {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		holder.get().renameLabel( label, newLabel );
		holder.notifier().forEach( l -> l.accept( labeling ) );
	}

	public void setColor(String label, ARGBType newColor) {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		model.colorMapProvider().colorMap().setColor(label, newColor);
		holder.notifier().forEach( l -> l.accept( labeling ) );
	}

	private String suggestName(List<String> labels) {
		for (int i = 1; i < 10000; i++) {
			String label = "Label " + i;
			if (!labels.contains(label))
				return label;
		}
		return null;
	}

	public void localizeLabel( final String label ) {
		final Interval labelBox = getBoundingBox( model.labeling().get().iterableRegions().get( label ) );
		if ( labelBox != null )
			model.transformationModel().transformToShowInterval( labelBox );
	}

	private static Interval getBoundingBox( IterableRegion< BitType > region )
	{
		int numDimensions = region.numDimensions();
		Cursor<?> cursor = region.cursor();
		if ( ! cursor.hasNext() )
			return null;
		long[] min = new long[ numDimensions ];
		long[] max = new long[ numDimensions ];
		cursor.fwd();
		cursor.localize( min );
		cursor.localize( max );
		while( cursor.hasNext() )	{
			cursor.fwd();
			for( int i = 0; i < numDimensions; i++){
				int pos = cursor.getIntPosition( i );
				min[ i ] = Math.min( min[ i ], pos);
				max[ i ] = Math.max( max[ i ], pos);
			}
		}
		return new FinalInterval( min, max );
	}
}
