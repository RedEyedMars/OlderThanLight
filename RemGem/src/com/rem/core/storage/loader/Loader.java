package com.rem.core.storage.loader;

import java.util.Iterator;

public interface Loader {

	public abstract void load(
			Iterator<Integer> ints, 
			Iterator<Float> floats, 
			Iterator<String> strings);
}
