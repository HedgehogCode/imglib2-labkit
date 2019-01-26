package net.imglib2.labkit.labeling;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class SetUtils {

	private SetUtils() {
		// Utility class
	}

	public static <T, U> Set<U> map(final Set<T> set, final Function<T, U> mapper) {
		return new AbstractSet<U>() {

			@Override
			public Iterator<U> iterator() {
				return map(set.iterator(), mapper);
			}

			@Override
			public int size() {
				return set.size();
			}
		};
	}

	public static <T, U> Iterator<U> map(final Iterator<T> iterator, final Function<T, U> mapper) {
		return new Iterator<U>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public U next() {
				return mapper.apply(iterator.next());
			}
		};
	}
}
