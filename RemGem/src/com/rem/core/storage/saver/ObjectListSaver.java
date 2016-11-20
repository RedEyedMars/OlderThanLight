package com.rem.core.storage.saver;

import java.util.List;

public class ObjectListSaver<T extends Object> implements Saver {

	private List<T> saveList;
	private boolean saveSize;
	public ObjectListSaver(List<T> saveList){
		this(saveList,false);
	}
	public ObjectListSaver(List<T> saveList, boolean saveSize){
		this.saveList = saveList;
		this.saveSize = saveSize;
	}
	@Override
	public void saveTo(List<Object> toSave) {
		if(saveSize){
			toSave.add(saveList.size());
		}
		for(T element:saveList){
			toSave.add(element);
		}
	}

}
