package com.rem.core.storage.saver;

import java.util.List;

import com.rem.core.storage.Storable;

public class StorablesListSaver implements Saver {

	private List<Storable> saveList;
	private boolean saveSize;
	public StorablesListSaver(List<Storable> saveList){
		this(saveList,false);
	}
	public StorablesListSaver(List<Storable> saveList, boolean saveSize){
		this.saveList = saveList;
		this.saveSize = saveSize;
	}
	@Override
	public void saveTo(List<Object> toSave) {
		if(saveSize){
			toSave.add(saveList.size());
		}
		for(Storable element:saveList){
			element.getSaver().saveTo(toSave);
		}
	}

}
