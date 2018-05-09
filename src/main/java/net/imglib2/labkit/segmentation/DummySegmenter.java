package net.imglib2.labkit.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DummySegmenter implements Segmenter
{
	@Override public void editSettings( JFrame dialogParent )
	{

	}

	@Override public void segment( RandomAccessibleInterval< ? > image, RandomAccessibleInterval< ? extends IntegerType< ? > > labels )
	{

	}

	@Override public void predict( RandomAccessibleInterval< ? > image, RandomAccessibleInterval< ? extends RealType< ? > > prediction )
	{

	}

	@Override public void train( List< ? extends RandomAccessibleInterval< ? > > image, List< ? extends Labeling > groundTruth )
	{

	}

	@Override public boolean isTrained()
	{
		return false;
	}

	@Override public void saveModel( String path, boolean overwrite ) throws Exception
	{

	}

	@Override public void openModel( String path ) throws Exception
	{

	}

	@Override public Notifier< Consumer< Segmenter > > listeners()
	{
		return new Notifier<>();
	}

	@Override public List< String > classNames()
	{
		return Arrays.asList("foreground", "background");
	}
}
