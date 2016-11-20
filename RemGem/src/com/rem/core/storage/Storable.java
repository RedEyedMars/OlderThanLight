package com.rem.core.storage;

import com.rem.core.storage.loader.Loader;
import com.rem.core.storage.saver.Saver;

public interface Storable {
	
	public Loader getLoader();
	public Saver getSaver();
}
