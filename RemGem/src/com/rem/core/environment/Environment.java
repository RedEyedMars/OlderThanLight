package com.rem.core.environment;


import com.rem.core.Action;
import com.rem.core.gui.Updatable;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.storage.Storable;

public abstract class Environment extends GraphicEntity implements Storable, Updatable {

	
	protected String name;
	protected String fileName;
	public abstract Object getEntity(int id);
	public abstract void addEntity(int id, Object object);
	public abstract void onCreate();
	public abstract void restart(Action<Object> onComplete);
	public Environment(String textureName) {
		super(textureName);
	}
	public Environment(String textureName, int layer) {
		super(textureName, layer);
	}
	public String getName(){
		return name;
	}
	public String getFileName() {
		return fileName;
	}
	public void setNameAndFileName(String name, String filename) {
		this.name = name;
		this.fileName = filename;
	}

}
