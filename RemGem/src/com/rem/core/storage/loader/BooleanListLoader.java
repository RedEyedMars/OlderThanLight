package com.rem.core.storage.loader;

import java.util.Iterator;
import java.util.List;

public class BooleanListLoader extends ListLoader<Boolean>{

	public BooleanListLoader(List<Boolean> toLoad) {
		super(toLoad);
	}
	public BooleanListLoader(List<Boolean> toLoad, int maxSize) {
		super(toLoad, maxSize);
	}
	@Override
	public Boolean loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {
		return ints.next()==0;
	}

}
