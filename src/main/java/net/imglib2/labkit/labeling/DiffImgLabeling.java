package net.imglib2.labkit.labeling;

import java.util.Iterator;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;

/**
 * @author Matthias Arzt
 * @author Benjamin Wilhelm
 */
public class DiffImgLabeling<T, I extends IntegerType<I>>
		extends AbstractWrappedInterval<RandomAccessibleInterval<LabelingType<T>>>
		implements RandomAccessibleInterval<LabelingType<T>>, IterableInterval<LabelingType<T>> {
	// TODO implement SubIntervalIterable

	private final RandomAccessibleInterval<Diff<T>> diff;

	public DiffImgLabeling(final RandomAccessibleInterval<LabelingType<T>> source,
			final RandomAccessibleInterval<Diff<T>> diff) {
		super(source);
		this.diff = diff;
	}

	@Override
	public RandomAccess<LabelingType<T>> randomAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RandomAccess<LabelingType<T>> randomAccess(Interval interval) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LabelingType<T> firstElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object iterationOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<LabelingType<T>> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor<LabelingType<T>> cursor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor<LabelingType<T>> localizingCursor() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class Diff<T> {

		public final AddRemove action;
		public final T label;

		public Diff(AddRemove action, T label) {
			this.action = action;
			this.label = label;
		}

		public static <T> Diff<T> add(T label) {
			return new Diff(AddRemove.ADD, label);
		}

		public static <T> Diff<T> remove(T label) {
			return new Diff(AddRemove.REMOVE, label);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Diff))
				return false;
			Diff<T> that = (Diff<T>) obj;
			return (action == that.action) && label.equals(that.label);
		}

		@Override
		public int hashCode() {
			return 31 * action.hashCode() + label.hashCode();
		}
	}

	public enum AddRemove {
		ADD, REMOVE
	}
}
