package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class SegmentationModel
{

	private final ImageLabelingModel imageLabelingModel;
	private final Holder<NamedSegmenter> segmenter;
	private List< NamedSegmenter > segmenters = new ArrayList<>();

	private final RandomAccessibleInterval< ? > compatibleImage;
	private final CellGrid grid;


	public SegmentationModel( RandomAccessibleInterval< ? > compatibleImage, ImageLabelingModel imageLabelingModel, Segmenter segmenter )
	{
		this.imageLabelingModel = imageLabelingModel;
		NamedSegmenter namedSegmenter = new NamedSegmenter( segmenter );
		this.segmenter = new DefaultHolder<>( namedSegmenter );
		this.segmenters.add( namedSegmenter );
		this.compatibleImage = compatibleImage;
		this.grid = LabkitUtils.suggestGrid( this.compatibleImage, imageLabelingModel.isTimeSeries() );
	}

	public Labeling labeling() {
		return imageLabelingModel.labeling().get();
	}

	public RandomAccessibleInterval< ? > image() {
		return compatibleImage;
	}

	public CellGrid grid() {
		return grid;
	}

	public List<NamedSegmenter> segmenters() {
		return segmenters;
	}

	public Holder<NamedSegmenter> segmenter() { return segmenter; }

	public ColorMap colorMap() {
		return imageLabelingModel.colorMapProvider().colorMap();
	}

	public AffineTransform3D labelTransformation()
	{
		return imageLabelingModel.labelTransformation();
	}
}
