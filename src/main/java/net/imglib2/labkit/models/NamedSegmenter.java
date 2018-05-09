package net.imglib2.labkit.models;

import net.imglib2.labkit.segmentation.Segmenter;

import java.util.concurrent.atomic.AtomicInteger;

public class NamedSegmenter
{
	private static final AtomicInteger counter = new AtomicInteger();

	private final String name = "Classifier-#" + counter.incrementAndGet();
	private final Segmenter segmenter;

	public NamedSegmenter( Segmenter segmenter )
	{
		this.segmenter = segmenter;
	}

	public Segmenter get() {
		return segmenter;
	}

	public String name() {
		return name;
	}

	@Override public String toString()
	{
		return name();
	}
}
