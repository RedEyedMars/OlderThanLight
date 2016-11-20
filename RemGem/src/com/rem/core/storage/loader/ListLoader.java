package com.rem.core.storage.loader;

import java.util.Iterator;
import java.util.List;

public abstract class ListLoader <T extends Object> implements Loader{
	private List<T> toLoad;
	private int maxSize = Integer.MAX_VALUE;
	private int loaded = 0;
	private boolean loadMaxSize = false;

	public ListLoader(List<T> toLoad){
		this(toLoad,Integer.MAX_VALUE);
	}
	public ListLoader(List<T> toLoad, int maxSize){
		this.toLoad = toLoad;
		this.maxSize = maxSize;
		this.loadMaxSize = false;
		this.loaded = 0;
	}
	public ListLoader(List<T> toLoad, boolean loadMaxSize){
		this.toLoad = toLoad;
		this.loadMaxSize = loadMaxSize;
		this.loaded = 0;
	}

	@Override
	public void load(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {
		if(loadMaxSize){
			maxSize=ints.next();
		}
		this.loaded = 0;
		while(hasNext()){
			toLoad.add(loadObject(ints,floats,strings));
			++loaded;
		}
	}
	protected boolean hasNext(){
		return loaded<maxSize;
	}
	public abstract T loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings);
}
