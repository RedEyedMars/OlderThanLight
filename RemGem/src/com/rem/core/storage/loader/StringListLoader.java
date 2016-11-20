package com.rem.core.storage.loader;

import java.util.Iterator;
import java.util.List;

public class StringListLoader extends ListLoader<String>{

	public StringListLoader(List<String> toLoad) {
		super(toLoad);
	}
	public StringListLoader(List<String> toLoad, int maxSize) {
		super(toLoad, maxSize);
	}
	@Override
	public String loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {
		return strings.next();
	}

}
