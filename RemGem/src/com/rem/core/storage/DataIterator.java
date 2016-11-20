package com.rem.core.storage;

import java.util.Iterator;

import com.rem.core.Hub;

public class DataIterator  {
	private int maxIntegers;
	private int integerIndex = 3;
	private int floatIndex = 0;
	private int stringIndex = 0;
	private Object[] data;

	private Iterator<Integer> integerIterator = new Iterator<Integer>(){
		@Override
		public boolean hasNext() {
			return integerIndex<maxIntegers;
		}
		@Override
		public Integer next() {
			return nextInteger();
		}
		@Override
		public void remove() {
			
		}			
	};
	private Iterator<Float> floatIterator = new Iterator<Float>(){
		@Override
		public boolean hasNext() {
			return floatIndex+1<stringIndex;
		}

		@Override
		public Float next() {
			return nextFloat();
		}

		@Override
		public void remove() {
			
		}
	};
	private Iterator<String> stringIterator = new Iterator<String>(){
		@Override
		public boolean hasNext() {
			return stringIndex<data.length;
		}

		@Override
		public String next() {
			return nextString();
		}

		@Override
		public void remove() {
			
		}
	};


	public Integer nextInteger(){
		if(Storage.debug_load)Hub.log.bufferDebug("Map.MapLoader.nextInteger", (Integer)data[integerIndex]+",");
		return (Integer)data[integerIndex++];
	}

	public Float nextFloat(){
		if(Storage.debug_load)Hub.log.bufferDebug("Map.MapLoader.nextInteger", (Float)data[floatIndex]+",");
		return (Float)data[floatIndex++];
	}

	public String nextString(){
		return (String)data[stringIndex++];
	}

	public DataIterator(Object[] loaded){
		this.data = loaded;
		integerIndex = 3;
		maxIntegers = (Integer)data[0];
		floatIndex = maxIntegers;
		stringIndex = floatIndex+(Integer)data[1];
		if(Storage.debug_load)Hub.log.debug("Map.MapLoader.()", maxIntegers+","+floatIndex+","+stringIndex);
	}

	public Iterator<Integer> getIntegers(){
		return integerIterator;
	}
	public Iterator<Float> getFloats(){
		return floatIterator;
	}
	public Iterator<String> getStrings(){
		return stringIterator;
	}

}
