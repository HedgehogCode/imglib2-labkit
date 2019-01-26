package net.imglib2.labkit.labeling;

import java.util.function.Function;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.AbstractConvertedRandomAccess;

public class MappedRandomAccessibleInterval<T, U> extends AbstractWrappedInterval<RandomAccessibleInterval<T>>
		implements RandomAccessibleInterval<U> {

	private final Function<? super T, U> mapper;

	public MappedRandomAccessibleInterval(final RandomAccessibleInterval<T> source,
			final Function<? super T, U> mapper) {
		super(source);
		this.mapper = mapper;
	}

	@Override
	public RandomAccess<U> randomAccess() {
		return new MappedRandomAccess(sourceInterval.randomAccess());
	}

	@Override
	public RandomAccess<U> randomAccess(Interval interval) {
		return new MappedRandomAccess(sourceInterval.randomAccess(interval));
	}

	private class MappedRandomAccess extends AbstractConvertedRandomAccess<T, U> {

		public MappedRandomAccess(final RandomAccess<T> source) {
			super(source);
		}

		@Override
		public U get() {
			return mapper.apply(source.get());
		}

		@Override
		public AbstractConvertedRandomAccess<T, U> copy() {
			return new MappedRandomAccess(source.copyRandomAccess());
		}
	}
}
