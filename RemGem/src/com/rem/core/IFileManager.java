package com.rem.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.rem.core.storage.Resource;

public interface IFileManager {

	
	public static final int RELATIVE = 0;
	public static final int ABSOLUTE = 1;
	public static final int FROM_IMAGE_RESOURCE = 2;
	public Resource<InputStream> createInputStream(String path, int pathType);
	public Resource<OutputStream> createOutputStream(String path, int pathType);
	@SuppressWarnings("rawtypes")
	public Resource createImageResource(String name, String path);
	public boolean deleteFile(String string);
	public void createDirectory(String string);
	public File getDirectory(String path);
}
