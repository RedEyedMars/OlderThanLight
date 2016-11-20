package com.rem.core.storage.loader;

import java.util.Iterator;
import java.util.List;

public class IntegerListLoader extends ListLoader<Integer>{

	public IntegerListLoader(List<Integer> toLoad) {
		super(toLoad);
	}
	public IntegerListLoader(List<Integer> toLoad, int maxSize) {
		super(toLoad, maxSize);
	}
	@Override
	public Integer loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {
		return ints.next();
	}

}
