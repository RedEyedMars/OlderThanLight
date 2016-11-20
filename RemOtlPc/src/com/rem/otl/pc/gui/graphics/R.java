package com.rem.otl.pc.gui.graphics;

import java.io.File;
import java.io.InputStream;


public class R {


	public static InputStream getResource(String name){
		return R.class.getClassLoader().getResourceAsStream("com/rem/otl/pc/gui/graphics/"+name.replace(File.separator, "/"));
	}

}
