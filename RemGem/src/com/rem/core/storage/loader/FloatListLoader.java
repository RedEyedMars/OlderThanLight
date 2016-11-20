package com.rem.core.storage.loader;

import java.util.Iterator;
import java.util.List;

public class FloatListLoader extends ListLoader<Float>{

	public FloatListLoader(List<Float> toLoad) {
		super(toLoad);
	}
	public FloatListLoader(List<Float> toLoad, int maxSize) {
		super(toLoad, maxSize);
	}
	@Override
	public Float loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {
		return floats.next();
	}

}
