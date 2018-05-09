package net.imglib2.labkit.models;

import net.imglib2.labkit.segmentation.DummySegmenter;
import net.imglib2.labkit.utils.Notifier;

import java.util.Map;
import java.util.WeakHashMap;

public class SegmentationResultsModels
{

	private final SegmentationModel segmentationModel;

	private final Map<NamedSegmenter, SegmentationResultsModel> map = new WeakHashMap<>();

	private Notifier< Runnable > listeners = new Notifier<>();

	private SegmentationResultsModel dummy;

	public SegmentationResultsModels(SegmentationModel segmentationModel) {
		this.segmentationModel = segmentationModel;
		this.dummy = new SegmentationResultsModel( this.segmentationModel, new DummySegmenter() );
		segmentationModel.segmenters().forEach( s -> map.computeIfAbsent( s, this::initResults ) );
		segmentationModel.segmenter().notifier().add( this::selectedSegmenterChanged );
	}

	private void selectedSegmenterChanged( NamedSegmenter namedSegmenter )
	{
		map.computeIfAbsent(namedSegmenter, this::initResults);
		notifyListeners();
	}

	private void notifyListeners()
	{
		listeners.forEach( Runnable::run );
	}

	private SegmentationResultsModel initResults( NamedSegmenter namedSegmenter )
	{
		SegmentationResultsModel result = new SegmentationResultsModel( segmentationModel, namedSegmenter.get() );
		result.segmentationChangedListeners().add( this::notifyListeners );
		return result;
	}

	public SegmentationResultsModel selectedResults()
	{
		NamedSegmenter selected = segmentationModel.segmenter().get();
		if(selected == null)
			throw new IllegalStateException("no classifier selected");
		SegmentationResultsModel result = map.get( selected );
		return (result == null) ? dummy : result;
	}

	public Notifier<Runnable> segmentationChangedListeners()
	{
		return listeners;
	}
}
