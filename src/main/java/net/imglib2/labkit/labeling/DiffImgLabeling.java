package net.imglib2.labkit.labeling;

import java.util.Iterator;

import org.python.antlr.PythonParser.subscript_return;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.roi.labeling.LabelingMapping;
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

	private static class DiffRandomAccess<T> extends Point implements RandomAccess<LabelingType<T>> {

		private static class DiffLabelingType<T> extends LabelingType<T> {

			private static class MyModCount extends ModCount {
			}

			private final LabelingType<T> source;

			private final LabelingType<Diff<T>> diff;

			protected DiffLabelingType(final LabelingType<T> source, final LabelingType<Diff<T>> diff) {
				super(source.getIndex(), source.getMapping(), new MyModCount());
				this.source = source;
				this.diff = diff;
			}

			@Override
			public void set(final LabelingType<T> c) {
				// TODO think
			}

			@Override
			public LabelingType<T> createVariable() {
				return null; // TODO think
			}

			@Override
			public LabelingType<T> copy() {
				return new DiffLabelingType<>(source.copy(), diff.copy());
			}

			@Override
			public String toString() {
				return mapping.setAtIndex(type.getInteger()).set.toString();
			}

			public LabelingMapping<T> getMapping() {
				return mapping;
			}

			public IntegerType<?> getIndex() {
				return type;
			}

			@Override
			public boolean add(final T label) {
				final int index = type.getInteger();
				final int newindex = mapping.addLabelToSetAtIndex(label, index).index;
				if (newindex == index)
					return false;
				type.setInteger(newindex);
				generation.modCount++;
				return true;
			}

			@Override
			public boolean addAll(final Collection<? extends T> c) {
				final int index = type.getInteger();
				int newindex = index;
				for (final T label : c)
					newindex = mapping.addLabelToSetAtIndex(label, newindex).index;
				if (newindex == index)
					return false;
				type.setInteger(newindex);
				generation.modCount++;
				return true;
			}

			@Override
			public void clear() {
				final int index = type.getInteger();
				final int newindex = mapping.emptySet().index;
				if (newindex != index) {
					type.setInteger(newindex);
					generation.modCount++;
				}
			}

			@Override
			public boolean contains(final Object label) {
				return mapping.setAtIndex(type.getInteger()).set.contains(label);
			}

			@Override
			public boolean containsAll(final Collection<?> labels) {
				return mapping.setAtIndex(type.getInteger()).set.containsAll(labels);
			}

			@Override
			public boolean isEmpty() {
				return mapping.setAtIndex(type.getInteger()).set.isEmpty();
			}

			/**
			 * Note: the returned iterator reflects the label set at the time this method
			 * was called. Subsequent changes to the position of the {@link LabelingType} or
			 * the label set are not reflected!
			 */
			@Override
			public Iterator<T> iterator() {
				final Iterator<T> iter = mapping.setAtIndex(type.getInteger()).set.iterator();
				return new Iterator<T>() {
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public T next() {
						return iter.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(final Object label) {
				final int index = type.getInteger();
				final int newindex = mapping.removeLabelFromSetAtIndex((T) label, index).index;
				if (newindex == index)
					return false;
				type.setInteger(newindex);
				generation.modCount++;
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean removeAll(final Collection<?> c) {
				final int index = type.getInteger();
				int newindex = index;
				for (final T label : (Collection<? extends T>) c)
					newindex = mapping.removeLabelFromSetAtIndex(label, newindex).index;
				if (newindex == index)
					return false;
				type.setInteger(newindex);
				generation.modCount++;
				return true;
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				return mapping.setAtIndex(type.getInteger()).set.size();
			}

			@Override
			public Object[] toArray() {
				return mapping.setAtIndex(type.getInteger()).set.toArray();
			}

			@Override
			public <T1> T1[] toArray(final T1[] a) {
				return mapping.setAtIndex(type.getInteger()).set.toArray(a);
			}

			@Override
			public int hashCode() {
				return mapping.setAtIndex(type.getInteger()).hashCode;
			}

			@Override
			public boolean equals(final Object obj) {
				if (obj instanceof LabelingType) {
					@SuppressWarnings("unchecked")
					final LabelingType<T> c = (LabelingType<T>) obj;
					if (c.mapping == mapping)
						return c.type.getInteger() == type.getInteger();
				}
				return mapping.setAtIndex(type.getInteger()).set.equals(obj);
			}

			/**
			 * Creates a new {@link LabelingType} based on the underlying
			 * {@link IntegerType} of the existing {@link LabelingType} with a different
			 * label type L
			 * 
			 * @param newType
			 *            the type of the labels of the created {@link LabelingType}
			 * 
			 * @return new {@link LabelingType}
			 */
			public <L> LabelingType<L> createVariable(Class<? extends L> newType) {
				return new LabelingType<L>(this.type.createVariable(), new LabelingMapping<L>(this.type),
						new ModCount());
			}

			@Override
			public boolean valueEquals(final LabelingType<T> t) {
				return equals(t);
			}
		}

		private final RandomAccess<LabelingType<T>> sourceRA;

		private final RandomAccess<Diff<T>> diffRA;

		DiffRandomAccess(final int n, final RandomAccess<LabelingType<T>> sourceRA,
				final RandomAccess<Diff<T>> diffRA) {
			super(n);
			this.sourceRA = sourceRA;
			this.diffRA = diffRA;
		}

		DiffRandomAccess(final Interval interval, final RandomAccess<LabelingType<T>> sourceRA,
				final RandomAccess<Diff<T>> diffRA) {
			super(interval.numDimensions());
			this.sourceRA = sourceRA;
			this.diffRA = diffRA;
		}

		@Override
		public LabelingType<T> get() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Sampler<LabelingType<T>> copy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RandomAccess<LabelingType<T>> copyRandomAccess() {
			// TODO Auto-generated method stub
			return null;
		}

		// ------------------ Overwrite positioning methods to position source and diff
		// random access
		@Override
		public void fwd(int d) {
			super.fwd(d);
			sourceRA.fwd(d);
			diffRA.fwd(d);
		}

		@Override
		public void bck(int d) {
			super.bck(d);
			sourceRA.bck(d);
			diffRA.bck(d);
		}

		@Override
		public void move(int distance, int d) {
			super.move(distance, d);
			sourceRA.move(distance, d);
			diffRA.move(distance, d);
		}

		@Override
		public void move(long distance, int d) {
			super.move(distance, d);
			sourceRA.move(distance, d);
			diffRA.move(distance, d);
		}

		@Override
		public void move(Localizable localizable) {
			super.move(localizable);
			sourceRA.move(localizable);
			diffRA.move(localizable);
		}

		@Override
		public void move(int[] distance) {
			super.move(distance);
			sourceRA.move(distance);
			diffRA.move(distance);
		}

		@Override
		public void move(long[] distance) {
			super.move(distance);
			sourceRA.move(distance);
			diffRA.move(distance);
		}

		@Override
		public void setPosition(Localizable localizable) {
			super.setPosition(localizable);
			sourceRA.setPosition(localizable);
			diffRA.setPosition(localizable);
		}

		@Override
		public void setPosition(int[] position) {
			super.setPosition(position);
			sourceRA.setPosition(position);
			diffRA.setPosition(position);
		}

		@Override
		public void setPosition(long[] position) {
			super.setPosition(position);
			sourceRA.setPosition(position);
			diffRA.setPosition(position);
		}

		@Override
		public void setPosition(int position, int d) {
			super.setPosition(position, d);
			sourceRA.setPosition(position, d);
			diffRA.setPosition(position, d);
		}

		@Override
		public void setPosition(long position, int d) {
			super.setPosition(position, d);
			sourceRA.setPosition(position, d);
			diffRA.setPosition(position, d);
		}

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
