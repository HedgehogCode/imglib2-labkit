package net.imglib2.atlas.control.brush;

import java.awt.Cursor;
import java.util.Iterator;

import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.ViewerPanel;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.atlas.BrushOverlay;
import net.imglib2.atlas.color.IntegerColorProvider;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * A {@link TransformEventHandler} that changes an {@link AffineTransform3D}
 * through a set of {@link Behaviour}s.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Philipp Hanslovsky
 */
public class LabelBrushController
{

	public static int BACKGROUND = -1;

	final protected ViewerPanel viewer;

	private final RandomAccessibleInterval< IntType > labels;

	private final PaintPixelsGenerator< IntType, ? extends Iterator< IntType > > pixelsGenerator;

	final protected AffineTransform3D labelTransform = new AffineTransform3D();

	final protected RealPoint labelLocation;

	final protected BrushOverlay brushOverlay;

	private final TLongIntHashMap groundTruth;

	final int brushNormalAxis;

	protected int brushRadius = 5;

	private final int nLabels;

	private int currentLabel = 0;

	public BrushOverlay getBrushOverlay()
	{
		return brushOverlay;
	}

	public int getCurrentLabel()
	{
		return currentLabel;
	}

	public void setCurrentLabel( final int label )
	{
		this.currentLabel = label;
	}

	public TLongIntHashMap getGroundTruth()
	{
		return groundTruth;
	}

	public static TLongIntHashMap emptyGroundTruth()
	{
		return new TLongIntHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, Long.MAX_VALUE, BACKGROUND );
	}

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	public LabelBrushController(
			final ViewerPanel viewer,
			final RandomAccessibleInterval<IntType> labels,
			final PaintPixelsGenerator<IntType, ? extends Iterator<IntType>> pixelsGenerator,
			final Behaviours behaviors,
			final int brushNormalAxis,
			final int nLabels,
			final TLongIntHashMap groundTruth,
			final IntegerColorProvider colorProvider)
	{
		this.viewer = viewer;
		this.labels = labels;
		this.pixelsGenerator = pixelsGenerator;
		this.brushNormalAxis = brushNormalAxis;
		brushOverlay = new BrushOverlay( viewer, currentLabel, colorProvider );

		labelLocation = new RealPoint( 3 );

		behaviors.behaviour( new Paint(), "paint", "SPACE button1" );
		behaviors.behaviour( new Erase(), "erase", "SPACE button2", "SPACE button3" );
		behaviors.behaviour( new ChangeBrushRadius(), "change brush radius", "SPACE scroll" );
		behaviors.behaviour( new ChangeLabel(), "change label", "SPACE shift scroll" );
		behaviors.behaviour( new MoveBrush(), "move brush", "SPACE" );
		this.nLabels = nLabels;
		this.groundTruth = groundTruth;
	}

	public LabelBrushController(
			final ViewerPanel viewer,
			final RandomAccessibleInterval<IntType> labels,
			final PaintPixelsGenerator<IntType, ? extends Iterator<IntType>> pixelsGenerator,
			final Behaviours behaviors,
			final int nLabels,
			final TLongIntHashMap groundTruth,
			final IntegerColorProvider colorProvider)
	{
		this( viewer, labels, pixelsGenerator, behaviors, 2, nLabels, groundTruth, colorProvider );
	}

	private void setCoordinates( final int x, final int y )
	{
		labelLocation.setPosition( x, 0 );
		labelLocation.setPosition( y, 1 );
		labelLocation.setPosition( 0, 2 );

		viewer.displayToGlobalCoordinates( labelLocation );

		labelTransform.applyInverse( labelLocation, labelLocation );
	}

	private abstract class AbstractPaintBehavior implements DragBehaviour
	{
		protected void paint( final RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				final ExtendedRandomAccessibleInterval< IntType, RandomAccessibleInterval< IntType > > extended = Views.extendValue( labels, new IntType( BACKGROUND ) );
				final Iterator< IntType > it = pixelsGenerator.getPaintPixels( extended, coords, viewer.getState().getCurrentTimepoint(), brushRadius );
				final int v = getValue();
				synchronized ( groundTruth )
				{
					while ( it.hasNext() )
					{
						final IntType val = it.next();
						if ( Intervals.contains( labels, ( Localizable ) it ) )
						{
							val.set( v );
							final long index = IntervalIndexer.positionToIndex( ( Localizable ) it, labels );
							if ( v == BACKGROUND )
								groundTruth.remove( index );
							else
								groundTruth.put( index, v );
						}
					}
				}
			}

		}

		protected void paint( final int x, final int y )
		{
			setCoordinates( x, y );
			paint( labelLocation );
		}

		protected void paint( final int x1, final int y1, final int x2, final int y2 )
		{
			setCoordinates( x1, y1 );
			final double[] p1 = new double[ 3 ];
			final RealPoint rp1 = RealPoint.wrap( p1 );
			labelLocation.localize( p1 );

			setCoordinates( x2, y2 );
			final double[] d = new double[ 3 ];
			labelLocation.localize( d );

			LinAlgHelpers.subtract( d, p1, d );

			final double l = LinAlgHelpers.length( d );
			LinAlgHelpers.normalize( d );

			for ( int i = 1; i < l; ++i )
			{
				LinAlgHelpers.add( p1, d, p1 );
				paint( rp1 );
			}
			paint( labelLocation );
		}

		abstract protected int getValue();

		@Override
		public void init( final int x, final int y )
		{
			synchronized ( this )
			{
				oX = x;
				oY = y;
			}

			paint( x, y );

			viewer.requestRepaint();

			// System.out.println( getName() + " drag start (" + oX + ", " + oY + ")" );
		}

		@Override
		public void drag( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );

			paint( oX, oY, x, y );

			synchronized ( this )
			{
				oX = x;
				oY = y;
			}

			viewer.requestRepaint();

			// System.out.println( getName() + " drag by (" + dX + ", " + dY + ")" );
		}

		@Override
		public void end( final int x, final int y )
		{
		}
	}

	private class Paint extends AbstractPaintBehavior
	{
		@Override
		protected int getValue()
		{
			return getCurrentLabel();
		}
	}

	private class Erase extends AbstractPaintBehavior
	{
		@Override
		protected int getValue()
		{
			return BACKGROUND;
		}
	}

	private class ChangeBrushRadius implements ScrollBehaviour
	{
		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			if ( !isHorizontal )
			{
				if ( wheelRotation < 0 )
					brushRadius += 1;
				else if ( wheelRotation > 0 )
					brushRadius = Math.max( 0, brushRadius - 1 );

				brushOverlay.setRadius( brushRadius );
				// TODO request only overlays to repaint
				viewer.getDisplay().repaint();
			}
		}
	}

	private class ChangeLabel implements ScrollBehaviour
	{

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			if ( !isHorizontal )
			{
				if ( wheelRotation < 0 )
					currentLabel = Math.min( currentLabel + 1, nLabels - 1 );
				else if ( wheelRotation > 0 )
					currentLabel = Math.max( currentLabel - 1, 0 );

				brushOverlay.setLabel( currentLabel );
				// TODO request only overlays to repaint
				viewer.getDisplay().repaint();
			}
		}
	}

	private class MoveBrush implements DragBehaviour
	{

		@Override
		public void init( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );
			brushOverlay.setVisible( true );
			// TODO request only overlays to repaint
			viewer.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
			viewer.getDisplay().repaint();
		}

		@Override
		public void drag( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );
		}

		@Override
		public void end( final int x, final int y )
		{
			brushOverlay.setVisible( false );
			// TODO request only overlays to repaint
			viewer.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
			viewer.getDisplay().repaint();

		}
	}
}
